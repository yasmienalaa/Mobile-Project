package com.example.e_learning.dao

import androidx.room.*
import com.example.e_learning.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(progress: LessonProgressEntity)

    @Query(" SELECT * FROM lesson_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    suspend fun getForLesson(userId: Long, lessonId: Long): LessonProgressEntity?

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId")
    fun observeByUser(userId: Long): Flow<List<LessonProgressEntity>>
    @Query("SELECT COUNT(*) FROM lesson_progress WHERE userId = :userId AND completed = 1 AND lessonId IN (SELECT lessonId FROM lessons WHERE courseId = :courseId)")
    fun observeCompletedCount(userId: Long, courseId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE userId = :userId AND completed = 1 AND lessonId IN (SELECT lessonId FROM lessons WHERE courseId = :courseId)")
    suspend fun getCompletedCountForCourse(userId: Long, courseId: Long): Int

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE userId = :userId AND completed = 1")
    suspend fun getCompletedCountForUser(userId: Long): Int

    @Query("SELECT lessonId FROM lesson_progress WHERE userId = :userId AND completed = 1 AND lessonId IN (SELECT lessonId FROM lessons WHERE courseId = :courseId)")
    suspend fun getCompletedLessonIdsForCourse(userId: Long, courseId: Long): List<Long>

    @Query("SELECT * FROM lesson_progress WHERE userId = :userId AND lessonId IN (SELECT lessonId FROM lessons WHERE courseId = :courseId) ORDER BY lastOpenedAt DESC LIMIT 1")
    suspend fun getLastOpenedProgressForCourse(userId: Long, courseId: Long): LessonProgressEntity?
}
