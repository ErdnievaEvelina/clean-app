package com.example.myapplication.presentation


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.UserResponse
import com.example.myapplication.presentation.screen.HouseholdDetailAction
import com.example.myapplication.presentation.screen.HouseholdDetailViewModel
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdDetailScreen(
    viewModel: HouseholdDetailViewModel,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(uiState.success) {
        if (uiState.success && uiState.household == null) {
            snackbarHostState.showSnackbar("Хозяйство удалено")
            onDeleted()
        } else if (uiState.success) {
            snackbarHostState.showSnackbar("Хозяйство обновлено")
            viewModel.handleAction(HouseholdDetailAction.SuccessDismissed)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.handleAction(HouseholdDetailAction.ErrorDismissed)
        }
    }
    val currentUserId = viewModel.getCurrentUserId()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Хозяйство") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!uiState.isEditing && uiState.household != null) {
                        if (uiState.isCurrentUserCreator) {
                            IconButton(
                                onClick = { viewModel.handleAction(HouseholdDetailAction.EditClicked) }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                            }
                            IconButton(
                                onClick = { viewModel.handleAction(HouseholdDetailAction.DeleteClicked) }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Удалить",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.handleAction(HouseholdDetailAction.LeaveClicked) }
                            ) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = "Выйти",
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
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.household != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Информация о хозяйстве
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (uiState.isEditing) {
                                    OutlinedTextField(
                                        value = uiState.name,
                                        onValueChange = {
                                            viewModel.handleAction(HouseholdDetailAction.NameChanged(it))
                                        },
                                        label = { Text("Название") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Button(
                                        onClick = { viewModel.handleAction(HouseholdDetailAction.SaveClicked) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Сохранить")
                                    }
                                } else {
                                    DetailRow("Название", uiState.household!!.name)
                                    DetailRow("Invite код", uiState.household!!.inviteCode, true)
                                    DetailRow("Создано", uiState.household!!.createdAt.take(10))
                                    DetailRow("Статус", if (uiState.household!!.isActive) "Активно" else "Неактивно")
                                }
                            }
                        }
                    }

                    item {
                        // Invite код для копирования
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
                                Column {
                                    Text(
                                        text = "Пригласительный код",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = uiState.household!!.inviteCode,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(onClick = { clipboardManager.setText(AnnotatedString(uiState.household!!.inviteCode))
                                }) {
                                    Icon(
                                        Icons.Default.Done,
                                        contentDescription = "Копировать"
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Участники (${uiState.members.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(uiState.members) { member ->
                        MemberCard(
                            member = member,
                            isCreator = member.id == uiState.household!!.createdByUser,
                            canRemove = uiState.isCurrentUserCreator && member.id != currentUserId,
                            onRemove = {
                                viewModel.handleAction(
                                    HouseholdDetailAction.RemoveMember(member.id)
                                )
                            }
                        )
                    }
                }
            }

            if (uiState.showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.handleAction(HouseholdDetailAction.DismissDeleteDialog)
                    },
                    title = { Text("Удалить хозяйство") },
                    text = { Text("Вы уверены? Это действие необратимо.") },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.handleAction(HouseholdDetailAction.ConfirmDelete) }
                        ) {
                            Text("Удалить", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.handleAction(HouseholdDetailAction.DismissDeleteDialog) }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }

            if (uiState.showLeaveDialog) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.handleAction(HouseholdDetailAction.DismissLeaveDialog)
                    },
                    title = { Text("Выйти из хозяйства") },
                    text = { Text("Вы уверены, что хотите выйти?") },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.handleAction(HouseholdDetailAction.ConfirmLeave) }
                        ) {
                            Text("Выйти", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.handleAction(HouseholdDetailAction.DismissLeaveDialog) }
                        ) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isCode: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCode) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (isCode) androidx.compose.ui.text.font.FontFamily.Monospace else null
        )
    }
}

@Composable
fun MemberCard(
    member: UserResponse,
    isCreator: Boolean,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = member.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (isCreator) {
                        Text(
                            text = "Создатель",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}