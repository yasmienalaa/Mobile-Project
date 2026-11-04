package com.example.myapplication.entities

import androidx.room.*

@Entity(tableName = "enrollments", primaryKeys = ["userId","courseId"], foreignKeys = [
    ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = CourseEntity::class, parentColumns = ["courseId"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)
], indices = [Index("userId"), Index("courseId")])
data class EnrollmentEntity(
    val userId: Long,
    val courseId: Long,
    val enrolledAt: Long = System.currentTimeMillis()
)
