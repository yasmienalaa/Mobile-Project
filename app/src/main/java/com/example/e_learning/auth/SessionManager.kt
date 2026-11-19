package com.example.e_learning.auth

import android.content.Context
import com.example.e_learning.entities.UserEntity

class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("session", Context.MODE_PRIVATE)

    val userId: Long?
        get() = prefs.getLong(KEY_ID, NO_USER).takeIf { it != NO_USER }

    val role: String?
        get() = prefs.getString(KEY_ROLE, null)

    fun save(user: UserEntity) {
        prefs.edit().putLong(KEY_ID, user.userId).putString(KEY_ROLE, user.role).apply()
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_ID = "user_id"
        const val KEY_ROLE = "role"
        const val NO_USER = -1L
    }
}