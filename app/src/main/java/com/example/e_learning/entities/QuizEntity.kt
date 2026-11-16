package com.example.e_learning.entities

import androidx.room.*

@Entity(tableName = "quizzes", foreignKeys = [ForeignKey(entity = CourseEntity::class, parentColumns = ["courseId"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)], indices = [Index("courseId")])
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val quizId: Long = 0,
    val courseId: Long,
    val title: String,
    val description: String = ""
)
