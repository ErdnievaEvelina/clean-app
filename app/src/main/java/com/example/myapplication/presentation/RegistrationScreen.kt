package com.example.myapplication.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.state.RegistrationState
import com.example.myapplication.data.model.UserResponse


@Composable
fun RegistrationScreen(
    viewModel: RegistratorViewModel,
    navController: NavController,
    onRegistrationSuccess: (UserResponse) -> Unit,
) {

    var nameText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    LaunchedEffect(nameText) {
        viewModel.onNameChange(nameText)
    }
    LaunchedEffect(emailText) {
        viewModel.onEmailChange(emailText)
    }
    LaunchedEffect(passwordText) {
        viewModel.onPasswordChange(passwordText)
    }
    LaunchedEffect(confirmPasswordText) {
        viewModel.onConfirmPasswordChange(confirmPasswordText)
    }
    val registrationState by viewModel.registrationState.collectAsState()
    val nameError by viewModel.nameError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // Обработка успешной регистрации
    LaunchedEffect(registrationState) {
        when (val state = registrationState) {
            is RegistrationState.Success -> {
                snackbarHostState.showSnackbar("Регистрация успешна!")
                onRegistrationSuccess(state.user)
                viewModel.resetState()
            }
            is RegistrationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            else -> {}
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (registrationState is RegistrationState.Loading ||
            registrationState is RegistrationState.FirebaseCreating ||
            registrationState is RegistrationState.BackendRegistering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (registrationState) {
                            is RegistrationState.FirebaseCreating ->
                                "Регистрация в Firebase..."
                            is RegistrationState.BackendRegistering ->
                                "Сохранение в базе данных..."
                            else -> "Загрузка..."
                        }
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {
            Text(
                text = "Создать аккаунт",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            OutlinedTextField(
                value = emailText,
                onValueChange = { emailText = it },
                label ={Text("Email")},
                isError = emailError != null,
            )
            OutlinedTextField(
                value = nameText,
                onValueChange = { nameText = it },
                label = {Text("Name")},
                isError = nameError != null
            )
            OutlinedTextField(
                value = passwordText,
                onValueChange = { passwordText = it },
                label = {Text("Password")},
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null
            )
            OutlinedTextField(
                value = confirmPasswordText,
                onValueChange = { confirmPasswordText = it },
                label = {Text("Подтвердите пароль")},
                visualTransformation = PasswordVisualTransformation(),
                isError = confirmPasswordError != null
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.register()
                    // ДОБАВЛЯЕМ ПРЯМУЮ ПЕРЕДАЧУ ЗНАЧЕНИЙ
                    viewModel.onNameChange(nameText)
                    viewModel.onEmailChange(emailText)
                    viewModel.onPasswordChange(passwordText)
                    viewModel.onConfirmPasswordChange(confirmPasswordText)
                    /*navController.navigate("profile")*/},
                modifier = Modifier.fillMaxWidth(),
                enabled =
                        registrationState !is RegistrationState.Loading &&
                        registrationState !is RegistrationState.FirebaseCreating &&
                        registrationState !is RegistrationState.BackendRegistering
            ) {
                Text("Зарегистрироваться")
            }

            Spacer(modifier=Modifier.height(24.dp))
            TextButton(onClick ={
                navController.navigate("login")
            }) {
                Text("Уже есть аккаунт? Войдите")
            }
        }

    }


}