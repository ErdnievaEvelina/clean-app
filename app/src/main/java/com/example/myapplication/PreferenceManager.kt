package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    companion object {
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_FIREBASE_UID = "firebase_uid"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }
    fun saveUserData(
        idToken: String,
        firebaseUid: String,
        userId: String,
        email: String,
        name: String
    ) {
        prefs.edit().apply {
            putString(KEY_ID_TOKEN, idToken)
            putString(KEY_FIREBASE_UID, firebaseUid)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_NAME, name)
            apply()
        }
    }
    fun getIdToken(): String? = prefs.getString(KEY_ID_TOKEN, null)

    // Получаем firebaseUid
    fun getFirebaseUid(): String? = prefs.getString(KEY_FIREBASE_UID, null)

    // Получаем userId из вашей БД
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    // Проверяем, залогинен ли пользователь
    fun isLoggedIn(): Boolean = !getIdToken().isNullOrEmpty()

    // Очищаем данные при выходе
    fun clear() {
        prefs.edit { clear() }
    }

    // Получаем все данные пользователя
    fun getUserData(): Map<String, String?> = mapOf(
        "idToken" to getIdToken(),
        "firebaseUid" to getFirebaseUid(),
        "userId" to getUserId(),
        "email" to prefs.getString(KEY_USER_EMAIL, null),
        "name" to prefs.getString(KEY_USER_NAME, null)
    )
}