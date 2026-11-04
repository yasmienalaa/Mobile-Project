package com.example.myapplication.entities

import androidx.room.*

@Entity(tableName = "quiz_results", foreignKeys = [
    ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = QuizEntity::class, parentColumns = ["quizId"], childColumns = ["quizId"], onDelete = ForeignKey.CASCADE)
], indices = [Index("userId"), Index("quizId")])
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val resultId: Long = 0,
    val userId: Long,
    val quizId: Long,
    val score: Int,
    val maxScore: Int,
    val submittedAt: Long = System.currentTimeMillis()
)
