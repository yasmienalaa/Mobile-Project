package com.example.e_learning.auth

import android.content.Intent
import androidx.fragment.app.Fragment
import com.example.e_learning.HomeActivity
import com.example.e_learning.MainActivity
import com.google.android.material.transition.MaterialFadeThrough

fun Fragment.goHome(role: String) {
    val context = requireContext()
    val intent = Intent(context, HomeActivity::class.java).apply {
        putExtra("IS_ADMIN", role == Role.ADMIN)
    }
    startActivity(intent)
    requireActivity().finish()
}

fun Fragment.goToLogin() {
    val context = requireContext()
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    requireActivity().finish()
}

fun String.pretty(): String = lowercase().replaceFirstChar { it.uppercase() }

fun Fragment.applyFadeThrough() {
    enterTransition = MaterialFadeThrough()
    exitTransition = MaterialFadeThrough()
    reenterTransition = MaterialFadeThrough()
    returnTransition = MaterialFadeThrough()
}

