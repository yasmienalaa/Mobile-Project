package com.example.e_learning.entities

import androidx.room.*

@Entity(tableName = "questions", foreignKeys = [ForeignKey(entity = QuizEntity::class, parentColumns = ["quizId"], childColumns = ["quizId"], onDelete = ForeignKey.CASCADE)], indices = [Index("quizId")])
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val questionId: Long = 0,
    val quizId: Long,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: String, // A/B/C/D; validate on save.
    val points: Int = 1,
    val position: Int
)
