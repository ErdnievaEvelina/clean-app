package com.example.myapplication.presentation.screen.household

import com.example.myapplication.presentation.viewModel.household.JoinHouseholdAction
import com.example.myapplication.presentation.viewModel.household.JoinHouseholdViewModel

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalClipboardManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinHouseholdScreen(
    viewModel: JoinHouseholdViewModel,
    onBack: () -> Unit,
    onJoinSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.handleAction(JoinHouseholdAction.ErrorDismissed)
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success && uiState.joinedHouseholdId != null) {
            snackbarHostState.showSnackbar("Вы успешно вступили в хозяйство!")
            onJoinSuccess(uiState.joinedHouseholdId!!)
            viewModel.handleAction(JoinHouseholdAction.SuccessDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вступить в хозяйство") },
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
                        Text("Вступление в хозяйство...")
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Введите invite-код",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Код можно получить от создателя хозяйства",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Поле для ввода invite-кода
                    OutlinedTextField(
                        value = uiState.inviteCode,
                        onValueChange = {
                            viewModel.handleAction(JoinHouseholdAction.InviteCodeChanged(it))
                        },
                        label = { Text("Invite-код") },
                        placeholder = { Text("Например: R8UHICfn") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.error != null,
                        supportingText = {
                            if (uiState.error != null) {
                                Text(uiState.error!!)
                            }
                        }
                    )

                    // Кнопка "Вставить из буфера"
                    TextButton(
                        onClick = {
                            val pastedText = clipboardManager.getText()?.text
                            if (!pastedText.isNullOrBlank()) {
                                viewModel.handleAction(JoinHouseholdAction.InviteCodeChanged(pastedText))
                            }
                        }
                    ) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Вставить из буфера")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.handleAction(JoinHouseholdAction.JoinClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.inviteCode.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Вступить")
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Где найти invite-код?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Создатель хозяйства может поделиться кодом из деталей своего хозяйства. Код выглядит как 8 символов (например: R8UHICfn).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}