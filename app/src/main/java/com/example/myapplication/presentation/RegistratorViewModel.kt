package com.example.myapplication.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.state.RegistrationState
import com.example.myapplication.data.AuthRepositoryImpl
import com.example.myapplication.state.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistratorViewModel(
    private val authRepositoryImpl: AuthRepositoryImpl
): ViewModel() {
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Initial)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Initial)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    // Поля формы
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    // Сообщения об ошибках для полей
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()


    // Валидация имени при изменении
    fun onNameChange(newName: String) {
        _name.value = newName
        _nameError.value = when {
            newName.isBlank() -> "Имя не может быть пустым"
            newName.length < 2 -> "Имя должно содержать минимум 2 символа"
            newName.length > 30 -> "Имя должно содержать максимум 30 символов"
            else -> null
        }
    }
    // Валидация email при изменении
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _emailError.value = when {
            newEmail.isBlank() -> "Email не может быть пустым"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() ->
                "Введите корректный email"
            else -> null
        }
    }

    // Валидация пароля при изменении
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = when {
            newPassword.isBlank() -> "Пароль не может быть пустым"
            newPassword.length < 6 -> "Пароль должен содержать минимум 6 символов"
            else -> null
        }
        // Проверяем совпадение с подтверждением пароля
        if (_confirmPassword.value.isNotBlank()) {
            validateConfirmPassword()
        }
    }
    // Валидация подтверждения пароля
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        validateConfirmPassword()
    }

    private fun validateConfirmPassword() {
        _confirmPasswordError.value = when {
            _confirmPassword.value.isBlank() -> "Подтвердите пароль"
            _password.value != _confirmPassword.value -> "Пароли не совпадают"
            else -> null
        }
    }
    fun register() {
        viewModelScope.launch {
            authRepositoryImpl.registerUser(
                email = _email.value,
                password = _password.value,
                name = _name.value
            ).collect { state ->
                _registrationState.value = state
            }
        }
    }
    fun login() {
        viewModelScope.launch {
            authRepositoryImpl.loginUser(
                email = _email.value,
                password = _password.value
            ).collect { state ->
                _loginState.value = state
            }
        }
    }

    // Сброс состояния
    fun resetState() {
        _registrationState.value = RegistrationState.Initial
        _loginState.value = LoginState.Initial
    }

    // Очистка формы
    fun clearForm() {
        _name.value = ""
        _email.value = ""
        _password.value = ""
        _confirmPassword.value = ""
        _nameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null
    }
}