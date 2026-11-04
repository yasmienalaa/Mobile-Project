package com.example.myapplication.dao

import androidx.room.*
import com.example.myapplication.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(user: UserEntity): Long
    @Update suspend fun update(user: UserEntity)
    @Delete suspend fun delete(user: UserEntity)
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1") suspend fun findByEmail(email: String): UserEntity?
    @Query("SELECT * FROM users WHERE userId = :id LIMIT 1") fun observeById(id: Long): Flow<UserEntity?>
    @Query("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'") fun observeStudentCount(): Flow<Int>
}
