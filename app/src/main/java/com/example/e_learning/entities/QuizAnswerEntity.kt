package com.example.e_learning.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "quiz_answers",
    foreignKeys = [
        ForeignKey(
            entity = QuizResultEntity::class,
            parentColumns = ["resultId"],
            childColumns = ["resultId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("resultId"), Index(value = ["resultId", "questionId"], unique = true)]
)
data class QuizAnswerEntity(
    @PrimaryKey(autoGenerate = true) val answerId: Long = 0,
    val resultId: Long,
    val questionId: Long,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val selectedOption: String?,
    val correctOption: String,
    val pointsAwarded: Int,
    val maxPoints: Int,
    val position: Int
)
