package com.example.e_learning.auth

import com.example.myapplication.entities.UserEntity
import org.json.JSONArray

object Role {
    const val STUDENT = "STUDENT"
    const val ADMIN = "ADMIN"
}

object Level {
    const val BEGINNER = "BEGINNER"
    const val INTERMEDIATE = "INTERMEDIATE"
    const val ADVANCED = "ADVANCED"
}

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(val user: UserEntity) : AuthState
    data class Error(val message: String) : AuthState
}

fun List<String>.toLanguagesJson(): String = JSONArray(this).toString()

fun UserEntity.languageList(): List<String> = runCatching {
    val arr = JSONArray(preferredLanguages)
    List(arr.length()) { arr.getString(it) }
}.getOrDefault(emptyList())