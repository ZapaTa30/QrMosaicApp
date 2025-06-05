package com.example.qrmosaicapp.auth

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun saveUser(email: String, password: String) {
        prefs.edit().apply {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.contains("email")
    }

    fun getUser(): Pair<String?, String?> {
        return Pair(prefs.getString("email", null), prefs.getString("password", null))
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    // Sync user to server (dummy, implement server-side later)
    fun syncUserIfNeeded() {
        // TODO: If internet available, sync with server
    }
}
