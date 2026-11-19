package com.example.e_learning.data

import com.example.e_learning.entities.QuizResultEntity

data class QuizResultSummary(
    val quizTitle: String,
    val courseTitle: String,
    val score: Int,
    val maxScore: Int,
    val percentage: Int
)

data class StudentProgressSummary(
    val studentName: String,
    val totalEnrolledCourses: Int,
    val totalCompletedLessons: Int,
    val averageQuizScorePercentage: Int,
    val coursesProgress: List<CourseProgress>,
    val recentQuizResults: List<QuizResultSummary> = emptyList()
)
