package com.example.myapplication.dao

import androidx.room.*
import com.example.myapplication.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun enroll(enrollment: EnrollmentEntity): Long
    @Delete suspend fun remove(enrollment: EnrollmentEntity)
    @Query("SELECT * FROM enrollments WHERE userId = :userId") fun observeByUser(userId: Long): Flow<List<EnrollmentEntity>>
    @Query("SELECT COUNT(*) FROM enrollments WHERE courseId = :courseId") fun observeStudentCountForCourse(courseId: Long): Flow<Int>
}
