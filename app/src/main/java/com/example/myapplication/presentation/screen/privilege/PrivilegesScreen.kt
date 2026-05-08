package com.example.myapplication.presentation.screen.privilege

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.privilege.PrivilegeUiModel
import com.example.myapplication.data.model.privilege.map.PrivilegeFilterType
import com.example.myapplication.presentation.viewModel.privelege.PrivilegesAction
import com.example.myapplication.presentation.viewModel.privelege.PrivilegesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivilegesScreen(
    viewModel: PrivilegesViewModel,
    onBack: () -> Unit,
    onPrivilegeClick: (PrivilegeUiModel) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = viewModel.getCurrentUserId()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.handleAction(PrivilegesAction.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Привилегии") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.handleAction(PrivilegesAction.ShowCreateDialog) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Создать привилегию")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            ScrollableTabRow(
                selectedTabIndex = when (uiState.filter) {
                    PrivilegeFilterType.ALL -> 0
                    PrivilegeFilterType.AVAILABLE -> 1
                    PrivilegeFilterType.MY -> 2
                },
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = uiState.filter == PrivilegeFilterType.ALL,
                    onClick = { viewModel.handleAction(PrivilegesAction.FilterChanged(PrivilegeFilterType.ALL)) },
                    text = { Text("Все") }
                )
                Tab(
                    selected = uiState.filter == PrivilegeFilterType.AVAILABLE,
                    onClick = { viewModel.handleAction(PrivilegesAction.FilterChanged(PrivilegeFilterType.AVAILABLE)) },
                    text = { Text("Доступные") }
                )
                Tab(
                    selected = uiState.filter == PrivilegeFilterType.MY,
                    onClick = { viewModel.handleAction(PrivilegesAction.FilterChanged(PrivilegeFilterType.MY)) },
                    text = { Text("Мои") }
                )
            }

            // Список привилегий
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.privileges.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            text = "Нет привилегий",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (uiState.filter) {
                                PrivilegeFilterType.ALL -> "Создайте первую привилегию"
                                PrivilegeFilterType.AVAILABLE -> "Нет доступных привилегий"
                                PrivilegeFilterType.MY -> "У вас пока нет купленных привилегий"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.privileges) { privilege ->
                        PrivilegeCard(
                            privilege = privilege,
                            currentUserId = currentUserId,
                            onCardClick = { onPrivilegeClick(privilege) },
                            onBuy = { viewModel.handleAction(PrivilegesAction.BuyPrivilege(privilege.id)) },
                            onDelete = { viewModel.handleAction(PrivilegesAction.DeletePrivilege(privilege.id)) }
                        )
                    }
                }
            }
        }

        if (uiState.showCreateDialog) {
            CreatePrivilegeDialog(
                title = uiState.newPrivilegeTitle,
                description = uiState.newPrivilegeDescription,
                cost = uiState.newPrivilegeCost,
                onTitleChange = { viewModel.handleAction(PrivilegesAction.NewTitleChanged(it)) },
                onDescriptionChange = { viewModel.handleAction(PrivilegesAction.NewDescriptionChanged(it)) },
                onCostChange = { viewModel.handleAction(PrivilegesAction.NewCostChanged(it)) },
                onCreate = {
                    val cost = uiState.newPrivilegeCost.toIntOrNull()
                    if (cost != null && cost in 5..500) {
                        viewModel.handleAction(
                            PrivilegesAction.CreatePrivilege(
                                title = uiState.newPrivilegeTitle,
                                description = uiState.newPrivilegeDescription,
                                cost = cost
                            )
                        )
                    } else {
                        viewModel.handleAction(PrivilegesAction.ErrorDismissed)
                    }
                },
                onDismiss = { viewModel.handleAction(PrivilegesAction.DismissCreateDialog) }
            )
        }

        if (uiState.isCreating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Создание привилегии...")
                }
            }
        }
    }
}

@Composable
fun PrivilegeCard(
    privilege: PrivilegeUiModel,
    currentUserId: String,
    onCardClick: () -> Unit,
    onBuy: () -> Unit,
    onDelete: () -> Unit
) {
    val isCreator = privilege.createdBy == currentUserId
    val isBoughtByMe = privilege.boughtBy == currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !privilege.isAvailable -> MaterialTheme.colorScheme.tertiaryContainer
                isCreator -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = privilege.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when {
                        !privilege.isAvailable -> MaterialTheme.colorScheme.tertiaryContainer
                        isCreator -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = when {
                            !privilege.isAvailable -> "Куплена"
                            isCreator -> "Создана"
                            else -> "Доступна"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (!privilege.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = privilege.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${privilege.cost} ₽",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (privilege.createdByName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Создал: ${privilege.createdByName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (privilege.boughtBy != null && privilege.boughtByName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Куплена: ${privilege.boughtByName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (privilege.isAvailable && !isCreator) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBuy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Купить за ${privilege.cost} ₽")
                }
            }

            if (isCreator && privilege.isAvailable) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDelete
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
    }
}

@Composable
fun CreatePrivilegeDialog(
    title: String,
    description: String,
    cost: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать привилегию") },
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
                onClick = onCreate,
                enabled = title.isNotBlank() &&
                        title.length in 2..120 &&
                        cost.toIntOrNull() in 5..500
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}