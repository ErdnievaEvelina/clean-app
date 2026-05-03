package com.example.myapplication.presentation

import com.example.myapplication.presentation.screen.HouseholdViewModel
import com.example.myapplication.presentation.screen.HouseholdsAction
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.data.model.HouseholdWithUserInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdsScreen(
    viewModel: HouseholdViewModel,
    onHouseholdClick: (HouseholdWithUserInfo) -> Unit,
    onJoinClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.handleAction(HouseholdsAction.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои хозяйства") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Кнопка для вступления в хозяйство
                    IconButton(
                        onClick = onJoinClick  // Добавьте этот параметр в функцию
                    ) {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Вступить",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.handleAction(HouseholdsAction.ShowCreateDialog) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Создать хозяйство")
            }
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.households.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет хозяйств",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нажмите на кнопку + чтобы создать",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.households) { householdWithInfo ->
                            HouseholdCard(
                                householdWithInfo = householdWithInfo,
                                onClick = { onHouseholdClick(householdWithInfo) }
                            )
                        }
                    }
                }
            }

            if (uiState.showCreateDialog) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.handleAction(HouseholdsAction.DismissCreateDialog)
                    },
                    title = { Text("Создать хозяйство") },
                    text = {
                        OutlinedTextField(
                            value = uiState.newHouseholdName,
                            onValueChange = {
                                viewModel.handleAction(HouseholdsAction.NewNameChanged(it))
                            },
                            label = { Text("Название") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.handleAction(
                                    HouseholdsAction.CreateHousehold(uiState.newHouseholdName)
                                )
                            },
                            enabled = uiState.newHouseholdName.isNotBlank()
                        ) {
                            Text("Создать")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                viewModel.handleAction(HouseholdsAction.DismissCreateDialog)
                            }
                        ) {
                            Text("Отмена")
                        }
                    }
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
                        Text("Создание...")
                    }
                }
            }
        }
    }
}

@Composable
fun HouseholdCard(
    householdWithInfo: HouseholdWithUserInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = householdWithInfo.household.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Код: ${householdWithInfo.household.inviteCode}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (householdWithInfo.userHousehold.isUserActive)
                            MaterialTheme.colorScheme.tertiaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (householdWithInfo.userHousehold.isUserActive) "Активно" else "Неактивно",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                if (householdWithInfo.userHousehold.balance != 0) {
                    Text(
                        text = "Баланс: ${householdWithInfo.userHousehold.balance} ₽",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Подробнее",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}