package com.example.e_learning.auth

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.e_learning.R
import com.google.android.material.transition.MaterialFadeThrough

fun Fragment.goHome(role: String) {
    val destination =
        if (role == Role.ADMIN) R.id.adminHomeFragment else R.id.studentHomeFragment
    findNavController().navigate(destination, null, navOptions {
        popUpTo(R.id.loginFragment) { inclusive = true }
    })
}

fun Fragment.goToLogin() {
    val nav = findNavController()
    nav.navigate(R.id.loginFragment, null, navOptions {
        popUpTo(nav.graph.id) { inclusive = true }
    })
}

fun String.pretty(): String = lowercase().replaceFirstChar { it.uppercase() }

fun Fragment.applyFadeThrough() {
    enterTransition = MaterialFadeThrough()
    exitTransition = MaterialFadeThrough()
    reenterTransition = MaterialFadeThrough()
    returnTransition = MaterialFadeThrough()
}