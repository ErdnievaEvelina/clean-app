package com.example.myapplication.presentation.screen.task


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
import com.example.myapplication.data.model.task.TaskFilterType
import com.example.myapplication.data.model.task.TaskStatus
import com.example.myapplication.data.model.task.TaskUiModel
import com.example.myapplication.presentation.viewModel.task.TaskViewModel
import com.example.myapplication.presentation.viewModel.task.TasksAction



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit,
    onTaskClick: (TaskUiModel) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.handleAction(TasksAction.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Задачи") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка создания задачи
                    IconButton(
                        onClick = { viewModel.handleAction(TasksAction.ShowCreateDialog) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Создать задачу")
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
            // Фильтры
            ScrollableTabRow(
                selectedTabIndex = when (uiState.filter) {
                    TaskFilterType.ALL -> 0
                    TaskFilterType.FREE -> 1
                    TaskFilterType.MY -> 2
                    TaskFilterType.COMPLETED -> 3
                },
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = uiState.filter == TaskFilterType.ALL,
                    onClick = { viewModel.handleAction(TasksAction.FilterChanged(TaskFilterType.ALL)) },
                    text = { Text("Все") }
                )
                Tab(
                    selected = uiState.filter == TaskFilterType.FREE,
                    onClick = { viewModel.handleAction(TasksAction.FilterChanged(TaskFilterType.FREE)) },
                    text = { Text("Свободные") }
                )
                Tab(
                    selected = uiState.filter == TaskFilterType.MY,
                    onClick = { viewModel.handleAction(TasksAction.FilterChanged(TaskFilterType.MY)) },
                    text = { Text("Мои") }
                )
                Tab(
                    selected = uiState.filter == TaskFilterType.COMPLETED,
                    onClick = { viewModel.handleAction(TasksAction.FilterChanged(TaskFilterType.COMPLETED)) },
                    text = { Text("Выполненные") }
                )
            }

            // Список задач
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет задач",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Создайте первую задачу или измените фильтр",
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
                    items(uiState.tasks) { task ->
                        TaskCard(
                            task = task,
                            currentUserId = viewModel.getCurrentUserId(),
                            onCardClick = { onTaskClick(task) },
                            onAssign = { viewModel.handleAction(TasksAction.AssignTask(task.id)) },
                            onUnassign = { viewModel.handleAction(TasksAction.UnassignTask(task.id)) },
                            onComplete = { viewModel.handleAction(TasksAction.CompleteTask(task.id)) },
                            onDelete = { viewModel.handleAction(TasksAction.DeleteTask(task.id)) }
                        )
                    }
                }
            }
        }

        // Диалог создания задачи
        if (uiState.showCreateDialog) {
            CreateTaskDialog(
                title = uiState.newTaskTitle,
                description = uiState.newTaskDescription,
                reward = uiState.newTaskReward,
                onTitleChange = { viewModel.handleAction(TasksAction.NewTitleChanged(it)) },
                onDescriptionChange = { viewModel.handleAction(TasksAction.NewDescriptionChanged(it)) },
                onRewardChange = { viewModel.handleAction(TasksAction.NewRewardChanged(it)) },
                onCreate = {
                    val reward = uiState.newTaskReward.toIntOrNull() ?: 0
                    viewModel.handleAction(
                        TasksAction.CreateTask(
                            title = uiState.newTaskTitle,
                            description = uiState.newTaskDescription,
                            reward = reward
                        )
                    )
                },
                onDismiss = { viewModel.handleAction(TasksAction.DismissCreateDialog) }
            )
        }

        // Индикатор создания
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
                    Text("Создание задачи...")
                }
            }
        }
    }
}

// presentation/task/TasksScreen.kt - обновите TaskCard
@Composable
fun TaskCard(
    task: TaskUiModel,
    currentUserId: String,
    onCardClick: () -> Unit,
    onAssign: () -> Unit,
    onUnassign: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val isAssignedToMe = task.assignedToUserId == currentUserId
    val isCreator = task.createdByUserName != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                TaskStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                TaskStatus.ASSIGNED -> MaterialTheme.colorScheme.secondaryContainer
                TaskStatus.OPEN -> MaterialTheme.colorScheme.surface
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
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (task.status) {
                        TaskStatus.OPEN -> MaterialTheme.colorScheme.primaryContainer
                        TaskStatus.ASSIGNED -> MaterialTheme.colorScheme.secondaryContainer
                        TaskStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                ) {
                    Text(
                        text = when (task.status) {
                            TaskStatus.OPEN -> "Свободна"
                            TaskStatus.ASSIGNED -> "Забронирована"
                            TaskStatus.COMPLETED -> "Выполнена"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Описание
            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
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
                if (task.reward > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Text(
                            text = "${task.reward} ₽",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

            }

            if (task.assignedToUserId != null && task.assignedToUserName != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Назначена: ${task.assignedToUserName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (task.createdByUserName != null && task.status != TaskStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {

                    Text(
                        text = "Создал: ${task.createdByUserName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (task.status != TaskStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (task.status) {
                        TaskStatus.OPEN -> {
                            Button(
                                onClick = onAssign,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {

                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Взять")
                            }
                        }
                        TaskStatus.ASSIGNED -> {
                            if (isAssignedToMe) {
                                OutlinedButton(
                                    onClick = onUnassign,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Отказаться")
                                }
                                Button(
                                    onClick = onComplete,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Выполнить")
                                }
                            } else {
                                Text(
                                    text = "Забронирована другим пользователем",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        else -> {}
                    }


                    if (task.status == TaskStatus.OPEN) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                // Задача выполнена
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "✓ Выполнена",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun CreateTaskDialog(
    title: String,
    description: String,
    reward: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRewardChange: (String) -> Unit,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать задачу") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reward,
                    onValueChange = onRewardChange,
                    label = { Text("Награда (₽)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

            }
        },
        confirmButton = {
            TextButton(
                onClick = onCreate,
                enabled = title.isNotBlank()
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

