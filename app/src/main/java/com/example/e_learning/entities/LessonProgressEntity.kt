package com.example.e_learning.entities

import androidx.room.*

@Entity(tableName = "lesson_progress", primaryKeys = ["userId","lessonId"], foreignKeys = [
    ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = LessonEntity::class, parentColumns = ["lessonId"], childColumns = ["lessonId"], onDelete = ForeignKey.CASCADE)
], indices = [Index("userId"), Index("lessonId")])
data class LessonProgressEntity(
    val userId: Long,
    val lessonId: Long,
    val completed: Boolean = false,
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
