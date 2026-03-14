package com.barrita.android.mainapp.app.data

import android.content.Context
import android.content.SharedPreferences
import com.barrita.android.mainapp.app.data.dto.UserData

object SessionManager {

    private const val PREFS_NAME = "barrita_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, accessToken: String, refreshToken: String, user: UserData?) {
        getPrefs(context).edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            user?.let {
                putString(KEY_USER_ID, it.id)
                putString(KEY_USER_EMAIL, it.email)
                putString(KEY_USER_NAME, it.fullName)
            }
            apply()
        }
    }

    fun getAccessToken(context: Context): String? {
        return getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        return getPrefs(context).getString(KEY_REFRESH_TOKEN, null)
    }

    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }

    fun getUserEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_NAME, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getAccessToken(context) != null
    }

    fun updateTokens(context: Context, accessToken: String, refreshToken: String) {
        getPrefs(context).edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
