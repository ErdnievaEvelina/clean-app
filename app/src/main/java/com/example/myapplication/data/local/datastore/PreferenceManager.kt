package com.example.myapplication.data.local.datastore

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    companion object {
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_FIREBASE_UID = "firebase_uid"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_CURRENT_HOUSEHOLD_ID = "current_household_id"
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

    fun getUserData(): Map<String, String?> = mapOf(
        "idToken" to getIdToken(),
        "firebaseUid" to getFirebaseUid(),
        "userId" to getUserId(),
        "email" to prefs.getString(KEY_USER_EMAIL, null),
        "name" to prefs.getString(KEY_USER_NAME, null)
    )
    fun saveCurrentHouseholdId(householdId: String) {
        Log.d("PreferenceManager", "saveCurrentHouseholdId: $householdId")
        prefs.edit { putString(KEY_CURRENT_HOUSEHOLD_ID, householdId) }
    }

    fun getCurrentHouseholdId(): String? {
        return prefs.getString(KEY_CURRENT_HOUSEHOLD_ID, null)
    }

    @SuppressLint("UseKtx")
    fun clearCurrentHouseholdId() {
        prefs.edit { remove(KEY_CURRENT_HOUSEHOLD_ID) }
    }
}