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
}
