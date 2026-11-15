package com.example.e_learning.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.e_learning.R
import com.example.e_learning.entities.UserEntity

class ProfileScreen(private val context: Context) {
    private val root = LayoutInflater.from(context).inflate(R.layout.screen_profile, null, false)

    fun createView(): View = root

    fun render(user: UserEntity?) {
        root.findViewById<TextView>(R.id.profileName).text = user?.fullName ?: "Student profile"
        root.findViewById<TextView>(R.id.profileEmail).text = user?.email ?: "No student account is connected."
        root.findViewById<TextView>(R.id.profileRole).text = user?.role?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Student"
        root.findViewById<TextView>(R.id.profileLevel).text = "Level: ${user?.level?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Not set"}"
        root.findViewById<TextView>(R.id.profileLanguages).text = "Preferred languages: ${formatLanguages(user?.preferredLanguages)}"
    }

    private fun formatLanguages(value: String?): String {
        if (value.isNullOrBlank() || value == "[]") return "Not set"
        return value.removePrefix("[").removeSuffix("]").replace("\"", "").replace(",", ", ")
    }
}