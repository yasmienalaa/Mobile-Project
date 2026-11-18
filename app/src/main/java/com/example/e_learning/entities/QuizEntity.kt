package com.example.e_learning.entities

import androidx.room.*


@Entity(
    tableName = "quizzes",
    foreignKeys = [
        ForeignKey(entity = CourseEntity::class, parentColumns = ["courseId"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = LessonEntity::class, parentColumns = ["lessonId"], childColumns = ["lessonId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("courseId"), Index("lessonId")]
)

data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val quizId: Long = 0,
    val courseId: Long,
    val lessonId: Long,
    val title: String,
    val description: String = ""
)