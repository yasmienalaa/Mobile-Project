package com.example.myapplication.dao

import androidx.room.*
import com.example.myapplication.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface CourseDao {
    @Insert suspend fun insert(course: CourseEntity): Long
    @Update suspend fun update(course: CourseEntity)
    @Delete suspend fun delete(course: CourseEntity)
    @Query("SELECT * FROM courses ORDER BY title") fun observeAll(): Flow<List<CourseEntity>>
    @Query("SELECT * FROM courses WHERE courseId = :id LIMIT 1") suspend fun getById(id: Long): CourseEntity?
    @Query("SELECT COUNT(*) FROM courses") fun observeCount(): Flow<Int>
}
