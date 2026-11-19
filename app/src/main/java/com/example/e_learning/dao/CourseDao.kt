package com.example.e_learning.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.e_learning.entities.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity): Int

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("SELECT * FROM courses ORDER BY title")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE courseId = :id LIMIT 1")
    suspend fun getById(id: Long): CourseEntity?

    @Query("SELECT * FROM courses WHERE title = :title LIMIT 1")
    suspend fun getByTitle(title: String): CourseEntity?

    @Query("SELECT courses.* FROM courses INNER JOIN enrollments ON enrollments.courseId = courses.courseId WHERE enrollments.userId = :userId ORDER BY courses.title")
    fun observeEnrolledByUser(userId: Long): Flow<List<CourseEntity>>

    @Query("SELECT courses.* FROM courses INNER JOIN enrollments ON enrollments.courseId = courses.courseId WHERE enrollments.userId = :userId ORDER BY courses.title")
    suspend fun getEnrolledByUser(userId: Long): List<CourseEntity>

    @Query("SELECT COUNT(*) FROM courses")
    fun observeCount(): Flow<Int>
}
