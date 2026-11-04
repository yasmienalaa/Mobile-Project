package com.example.myapplication.dao

import androidx.room.*
import com.example.myapplication.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(progress: LessonProgressEntity)
    @Query("SELECT * FROM lesson_progress WHERE userId = :userId") fun observeByUser(userId: Long): Flow<List<LessonProgressEntity>>
    @Query("SELECT COUNT(*) FROM lesson_progress WHERE userId = :userId AND completed = 1 AND lessonId IN (SELECT lessonId FROM lessons WHERE courseId = :courseId)") fun observeCompletedCount(userId: Long, courseId: Long): Flow<Int>
}
