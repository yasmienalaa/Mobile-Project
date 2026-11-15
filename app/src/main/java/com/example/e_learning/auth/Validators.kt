package com.example.e_learning.auth

import android.util.Patterns

object Validators {
    fun nameError(name: String): String? =
        if (name.isBlank()) "Name is required" else null

    fun emailError(email: String): String? =
        if (Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) null else "Enter a valid email"

    fun passwordError(password: String): String? =
        if (password.length >= 6) null else "At least 6 characters"

    fun confirmError(password: String, confirm: String): String? =
        if (password == confirm) null else "Passwords don't match"
}