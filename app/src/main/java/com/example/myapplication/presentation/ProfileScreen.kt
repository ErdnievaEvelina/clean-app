package com.example.myapplication.presentation

import androidx.compose.runtime.Composable
import com.example.myapplication.data.model.UserResponse


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    userData: UserResponse?  // 👈 ПАРАМЕТР ДЛЯ ПОЛУЧЕНИЯ ДАННЫХ
) {
    // Логируем полученные данные
    Log.d("ProfileScreen", "Получены данные: ${userData?.email}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Профиль пользователя",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Отображаем данные пользователя
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Имя: ${userData?.name ?: "Не указано"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Email: ${userData?.email ?: "Не указано"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ID: ${userData?.id ?: "Не указано"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Если пользователь не авторизован, показываем сообщение
        if (userData == null) {
            Text(
                text = "Войдите в систему",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}