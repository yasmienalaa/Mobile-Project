package com.example.e_learning.entities

import androidx.room.*

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val role: String = "STUDENT",
    val level: String = "BEGINNER",
    val preferredLanguages: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)
