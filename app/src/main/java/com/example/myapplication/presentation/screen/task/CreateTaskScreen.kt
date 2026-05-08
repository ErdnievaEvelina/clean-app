package com.example.myapplication.presentation.screen.task


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.presentation.viewModel.task.CreateTaskAction
import com.example.myapplication.presentation.viewModel.task.CreateTaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    viewModel: CreateTaskViewModel,
    onBack: () -> Unit,
    onTaskCreated: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.handleAction(CreateTaskAction.ErrorDismissed)
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success && uiState.createdTaskId != null) {
            snackbarHostState.showSnackbar("Задача создана!")
            onTaskCreated(uiState.createdTaskId!!)
            viewModel.handleAction(CreateTaskAction.SuccessDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создать задачу") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Создание задачи...")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Новая задача",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.handleAction(CreateTaskAction.TitleChanged(it)) },
                        label = { Text("Название *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.error != null && uiState.title.isBlank()
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.handleAction(CreateTaskAction.DescriptionChanged(it)) },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    OutlinedTextField(
                        value = uiState.reward,
                        onValueChange = { viewModel.handleAction(CreateTaskAction.RewardChanged(it)) },
                        label = { Text("Награда (₽)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )


                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.handleAction(CreateTaskAction.CreateClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.title.isNotBlank()
                    ) {
                        Text("Создать задачу")
                    }
                }
            }
        }
    }
}