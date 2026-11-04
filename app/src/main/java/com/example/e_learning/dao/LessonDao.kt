package com.example.myapplication.dao

import androidx.room.*
import com.example.myapplication.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface LessonDao {
    @Insert suspend fun insert(lesson: LessonEntity): Long
    @Update suspend fun update(lesson: LessonEntity)
    @Delete suspend fun delete(lesson: LessonEntity)
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position") fun observeByCourse(courseId: Long): Flow<List<LessonEntity>>
    @Query("SELECT * FROM lessons WHERE lessonId = :id LIMIT 1") suspend fun getById(id: Long): LessonEntity?
    @Query("SELECT COUNT(*) FROM lessons") fun observeCount(): Flow<Int>
}
