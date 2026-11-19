package com.example.e_learning.dao

import androidx.room.*
import com.example.e_learning.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun enroll(enrollment: EnrollmentEntity): Long
    @Delete suspend fun remove(enrollment: EnrollmentEntity)
    @Query("SELECT * FROM enrollments WHERE userId = :userId") fun observeByUser(userId: Long): Flow<List<EnrollmentEntity>>
    @Query("SELECT * FROM enrollments WHERE userId = :userId") suspend fun getEnrollmentsForUser(userId: Long): List<EnrollmentEntity>
    @Query("SELECT EXISTS(SELECT 1 FROM enrollments WHERE userId = :userId AND courseId = :courseId)") suspend fun isEnrolled(userId: Long, courseId: Long): Boolean
    @Query("SELECT COUNT(*) FROM enrollments WHERE courseId = :courseId") fun observeStudentCountForCourse(courseId: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM enrollments WHERE userId = :userId") suspend fun getEnrollmentCountForUser(userId: Long): Int
}


