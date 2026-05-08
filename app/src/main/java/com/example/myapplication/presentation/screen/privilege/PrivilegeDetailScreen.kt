package com.example.myapplication.presentation.screen.privilege

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.privilege.PrivilegeUiModel
import com.example.myapplication.presentation.viewModel.privelege.PrivilegeDetailAction
import com.example.myapplication.presentation.viewModel.privelege.PrivilegeDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivilegeDetailScreen(
    viewModel: PrivilegeDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentUserId = viewModel.getCurrentUserId()

    // Обработка ошибок
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            scope.launch {
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.handleAction(PrivilegeDetailAction.ErrorDismissed)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.privilege?.title ?: "Детали привилегии")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.handleAction(PrivilegeDetailAction.NavigateBack)
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    uiState.privilege?.let { privilege ->
                        // Кнопка редактирования (только для создателя и если привилегия доступна)
                        if (privilege.createdBy == currentUserId && privilege.isAvailable) {
                            IconButton(
                                onClick = {
                                    viewModel.handleAction(PrivilegeDetailAction.ShowEditDialog)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Редактировать"
                                )
                            }
                        }

                        // Кнопка удаления (только для создателя и если привилегия доступна)
                        if (privilege.createdBy == currentUserId && privilege.isAvailable) {
                            IconButton(
                                onClick = {
                                    viewModel.handleAction(PrivilegeDetailAction.ShowDeleteConfirmation)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null && uiState.privilege == null -> {
                    ErrorScreen(
                        error = uiState.error,
                        onRetry = { viewModel.handleAction(PrivilegeDetailAction.LoadPrivilege) }
                    )
                }

                uiState.privilege != null -> {
                    PrivilegeDetailContent(
                        privilege = uiState.privilege!!,
                        currentUserId = currentUserId,
                        isBuying = uiState.isBuying,
                        onBuy = { viewModel.handleAction(PrivilegeDetailAction.BuyPrivilege) }
                    )
                }
            }
        }

        // Диалог подтверждения удаления
        if (uiState.showDeleteConfirmation) {
            DeleteConfirmationDialog(
                onConfirm = { viewModel.handleAction(PrivilegeDetailAction.DeletePrivilege) },
                onDismiss = { viewModel.handleAction(PrivilegeDetailAction.DismissDeleteConfirmation) }
            )
        }

        // Диалог редактирования
        if (uiState.showEditDialog) {
            EditPrivilegeDialog(
                title = uiState.editTitle,
                description = uiState.editDescription,
                cost = uiState.editCost,
                onTitleChange = { viewModel.handleAction(PrivilegeDetailAction.EditTitleChanged(it)) },
                onDescriptionChange = { viewModel.handleAction(PrivilegeDetailAction.EditDescriptionChanged(it)) },
                onCostChange = { viewModel.handleAction(PrivilegeDetailAction.EditCostChanged(it)) },
                onUpdate = {
                    val cost = uiState.editCost.toIntOrNull()
                    if (cost != null && cost in 5..500) {
                        viewModel.handleAction(
                            PrivilegeDetailAction.UpdatePrivilege(
                                title = uiState.editTitle,
                                description = uiState.editDescription,
                                cost = cost
                            )
                        )
                    } else {
                        viewModel.handleAction(PrivilegeDetailAction.ErrorDismissed)
                        scope.launch {
                            snackbarHostState.showSnackbar("Стоимость должна быть от 5 до 500")
                        }
                    }
                },
                onDismiss = { viewModel.handleAction(PrivilegeDetailAction.DismissEditDialog) }
            )
        }

        // Затемнение при загрузке (удаление/редактирование/покупка)
        if (uiState.isDeleting || uiState.isEditing || uiState.isBuying) {
            LoadingOverlay(
                message = when {
                    uiState.isDeleting -> "Удаление привилегии..."
                    uiState.isEditing -> "Сохранение изменений..."
                    else -> "Покупка привилегии..."
                }
            )
        }
    }
}

@Composable
fun ErrorScreen(
    error: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ошибка загрузки",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = error ?: "Неизвестная ошибка",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Повторить")
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить привилегию") },
        text = { Text("Вы уверены, что хотите удалить эту привилегию? Это действие нельзя отменить.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun LoadingOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(message)
        }
    }
}

@Composable
fun EditPrivilegeDialog(
    title: String,
    description: String,
    cost: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать привилегию") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("2-120 символов") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Необязательно, до 2000 символов") }
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = onCostChange,
                    label = { Text("Стоимость *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    supportingText = { Text("5-500") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onUpdate,
                enabled = title.isNotBlank() &&
                        title.length in 2..120 &&
                        cost.toIntOrNull() in 5..500
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun PrivilegeDetailContent(
    privilege: PrivilegeUiModel,
    currentUserId: String,
    isBuying: Boolean,
    onBuy: () -> Unit
) {
    val isCreator = privilege.createdBy == currentUserId
    val isBoughtByMe = privilege.boughtBy == currentUserId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Карточка статуса
        StatusCard(
            isAvailable = privilege.isAvailable,
            isCreator = isCreator,
            isBoughtByMe = isBoughtByMe
        )

        // Описание
        DescriptionCard(description = privilege.description)

        // Информация о создателе
        InfoRow(
            icon = Icons.Default.Person,
            label = "Создатель",
            value = privilege.createdByName ?: privilege.createdBy.take(8)
        )

        // Информация о покупателе
        if (privilege.boughtBy != null) {
            InfoRow(
                icon = Icons.Default.ShoppingCart,
                label = "Куплена",
                value = privilege.boughtByName ?: privilege.boughtBy.take(8)
            )
        }

        // Карточка стоимости
        CostCard(cost = privilege.cost)

        // Кнопка покупки
        if (privilege.isAvailable && !isCreator) {
            Button(
                onClick = onBuy,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isBuying
            ) {
                if (isBuying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Купить за ${privilege.cost} ₽")
            }
        }

        // Сообщение если привилегия уже куплена
        if (!privilege.isAvailable && isBoughtByMe) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Вы уже приобрели эту привилегию")
                }
            }
        }

        // Сообщение если привилегия куплена другим
        if (!privilege.isAvailable && !isBoughtByMe && privilege.boughtBy != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Эта привилегия уже куплена")
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    isAvailable: Boolean,
    isCreator: Boolean,
    isBoughtByMe: Boolean
) {
    val (statusText, statusColor) = when {
        !isAvailable && isBoughtByMe -> "Куплена вами" to MaterialTheme.colorScheme.tertiaryContainer
        !isAvailable -> "Куплена" to MaterialTheme.colorScheme.surfaceVariant
        isCreator -> "Ваша привилегия" to MaterialTheme.colorScheme.secondaryContainer
        else -> "Доступна для покупки" to MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when {
                    !isAvailable && isBoughtByMe -> Icons.Default.CheckCircle
                    !isAvailable -> Icons.Default.Lock
                    isCreator -> Icons.Default.Edit
                    else -> Icons.Default.ShoppingCart
                },
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DescriptionCard(description: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Описание",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description?.takeIf { it.isNotBlank() } ?: "Нет описания",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CostCard(cost: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Стоимость",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$cost ₽",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}