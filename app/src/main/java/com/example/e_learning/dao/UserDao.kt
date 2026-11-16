package com.example.e_learning.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.e_learning.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Update
    suspend fun update(user: UserEntity)

    @Delete
    suspend fun delete(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun findById(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE upper(role) = 'STUDENT' ORDER BY userId LIMIT 1")
    suspend fun findFirstStudent(): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :id LIMIT 1")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")
    fun observeStudentCount(): Flow<Int>
}