package com.example.myapplication.presentation

import android.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
            uiState.isChangingEmail -> {
                ChangeEmailDialog(
                    newEmail = uiState.newEmail,
                    password = uiState.password,
                    onNewEmailChange = { viewModel.handleAction(ProfileAction.NewEmailChanged(it)) },
                    onPasswordChange = { viewModel.handleAction(ProfileAction.PasswordChanged(it)) },
                    onConfirm = { viewModel.handleAction(ProfileAction.ConfirmEmailChange) },
                    onCancel = { viewModel.handleAction(ProfileAction.CancelEmailChange) }
                )
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
                if (uiState.isEditing) {
                    EditProfileContent(
                        name = uiState.name,
                        onNameChange = { viewModel.handleAction(ProfileAction.NameChanged(it)) },
                        onSave = { viewModel.handleAction(ProfileAction.SaveClicked) },
                        onCancel = { viewModel.handleAction(ProfileAction.EditClicked) }
                    )
                } else {
                    ProfileContent(
                        user = uiState.user,
                        onEdit = { viewModel.handleAction(ProfileAction.EditClicked) },
                        onChangeEmail = { viewModel.handleAction(ProfileAction.ChangeEmailClicked)},
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
    onChangeEmail: () -> Unit,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user?.name ?: "Имя не указано",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать имя")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = user?.email ?: "Email не указан",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onChangeEmail) {
                        Icon(Icons.Default.Edit, contentDescription = "Сменить email")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "ID: ${user?.id ?: "Неизвестно"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /*Button(
            onClick = onEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Редактировать профиль")
        }*/

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

            /*OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email (не редактируется)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )*/

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
@Composable
fun ChangeEmailDialog(
    newEmail: String,
    password: String,
    onNewEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
){
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Сменить email") },
        text = {
            Column {
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = onNewEmailChange,
                    label = { Text("Новый email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("Подтвердите пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Для безопасности требуется подтверждение пароля",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = newEmail.isNotBlank() && password.isNotBlank()
            ) {
                Text("Сменить")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Отмена")
            }
        }
    )

}