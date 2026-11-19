package com.example.e_learning.data

import com.example.e_learning.entities.CourseEntity

data class CourseProgress(
    val course: CourseEntity,
    val totalLessons: Int,
    val completedLessons: Int,
    val progressPercentage: Int,
    val lastOpenedLessonTitle: String? = null
)
