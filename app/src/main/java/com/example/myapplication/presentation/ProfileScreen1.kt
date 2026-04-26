package com.example.myapplication.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.User
import com.example.myapplication.presentation.screen.ProfileAction
import com.example.myapplication.presentation.screen.ProfileViewModel

@Composable
fun ProfileScreen1(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onProfileDeleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.handleAction(ProfileAction.LoadProfile)
    }

    // Отслеживаем успешное удаление
    LaunchedEffect(uiState.success) {
        if (uiState.success && uiState.user == null) {
            onProfileDeleted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { viewModel.handleAction(ProfileAction.ErrorDismissed) }) {
                    Text("Закрыть")
                }
            }
            else -> {
                // Отображаем данные профиля
                if (uiState.isEditing) {
                    EditProfileContent(
                        name = uiState.name,
                        email = uiState.email,
                        onNameChange = { viewModel.handleAction(ProfileAction.NameChanged(it)) },
                        onSave = { viewModel.handleAction(ProfileAction.SaveClicked) },
                        onCancel = { viewModel.handleAction(ProfileAction.EditClicked) }
                    )
                } else {
                    ProfileContent(
                        user = uiState.user,
                        onEdit = { viewModel.handleAction(ProfileAction.EditClicked) },
                        onSyncEmail = { viewModel.handleAction(ProfileAction.SyncEmail) },
                        onDelete = { viewModel.handleAction(ProfileAction.DeleteClicked) },
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: User?,
    onEdit: () -> Unit,
    onSyncEmail: () -> Unit,
    onDelete: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.name ?: "Имя не указано",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = user?.email ?: "Email не указан",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ID: ${user?.id ?: "Неизвестно"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Редактировать профиль")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSyncEmail,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Синхронизировать email с Firebase")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Выйти")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                // Показываем диалог подтверждения
                onDelete()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Удалить аккаунт")
        }
    }
}

@Composable
fun EditProfileContent(
    name: String,
    email: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email (не редактируется)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить")
                }

                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена")
                }
            }
        }
    }
}