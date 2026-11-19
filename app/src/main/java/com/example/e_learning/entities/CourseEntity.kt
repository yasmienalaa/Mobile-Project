package com.example.e_learning.entities

import androidx.room.*

@Entity(tableName = "courses", indices = [Index("language")])
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val courseId: Long = 0,
    val title: String,
    val description: String,
    val language: String,
    val level: String = "BEGINNER",
    val createdAt: Long = System.currentTimeMillis()
)
