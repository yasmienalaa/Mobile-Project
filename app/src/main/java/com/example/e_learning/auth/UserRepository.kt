package com.example.e_learning.auth

import android.database.sqlite.SQLiteConstraintException
import com.example.myapplication.dao.UserDao
import com.example.myapplication.entities.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {

    suspend fun register(
        fullName: String, email: String, password: String,
        role: String, level: String, languages: List<String>
    ): Result<UserEntity> {
        val user = UserEntity(
            fullName = fullName.trim(),
            email = email.trim().lowercase(),
            passwordHash = PasswordHasher.hash(password),
            role = role,
            level = level,
            preferredLanguages = languages.toLanguagesJson()
        )
        return try {
            Result.success(user.copy(userId = dao.insert(user)))
        } catch (e: SQLiteConstraintException) {
            Result.failure(Exception("This email is already registered"))
        }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = dao.findByEmail(email.trim().lowercase())
        return if (user != null && PasswordHasher.verify(password, user.passwordHash))
            Result.success(user)
        else
            Result.failure(Exception("Invalid email or password"))
    }

    fun observeUser(id: Long): Flow<UserEntity?> = dao.observeById(id)

    suspend fun updateProfile(user: UserEntity) = dao.update(user)
}