package com.example.tradconnect_analytic.datos

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String, role: String) {
        prefs.edit()
            .putString("ACCESS_TOKEN", accessToken)
            .putString("REFRESH_TOKEN", refreshToken)
            .putString("USER_ROLE", role)
            .apply()
    }

    fun getToken(): String? = prefs.getString("ACCESS_TOKEN", null)

    fun getRole(): String? = prefs.getString("USER_ROLE", null)


    fun clear() {
        prefs.edit().clear().apply()
    }
}