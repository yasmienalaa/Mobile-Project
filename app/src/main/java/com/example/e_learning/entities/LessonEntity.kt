package com.example.e_learning.entities

import androidx.room.*

@Entity(tableName = "lessons", foreignKeys = [ForeignKey(entity = CourseEntity::class, parentColumns = ["courseId"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)], indices = [Index("courseId"), Index(value = ["courseId","position"], unique = true)])
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val lessonId: Long = 0,
    val courseId: Long,
    val title: String,
    val contentType: String = "TEXT", // TEXT or VIDEO
    val textContent: String? = null,
    val videoUrl: String? = null,
    val position: Int
)
