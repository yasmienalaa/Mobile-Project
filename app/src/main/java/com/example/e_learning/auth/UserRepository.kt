package com.example.e_learning.auth

import android.database.sqlite.SQLiteConstraintException
import com.example.myapplication.dao.UserDao
import com.example.myapplication.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val dao: UserDao) {

    suspend fun register(
        fullName: String, email: String, password: String,
        level: String, languages: List<String>
    ): Result<UserEntity> {
        val user = UserEntity(
            fullName = fullName.trim(),
            email = email.trim().lowercase(),
            passwordHash = PasswordHasher.hash(password),
            role = Role.STUDENT,
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

    suspend fun ensureAdminExists() {
        if (dao.findByEmail(AdminAccount.EMAIL) != null) return
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(AdminAccount.PASSWORD) }
        try {
            dao.insert(
                UserEntity(
                    fullName = AdminAccount.NAME,
                    email = AdminAccount.EMAIL,
                    passwordHash = hash,
                    role = Role.ADMIN,
                    level = Level.ADVANCED
                )
            )
        } catch (e: SQLiteConstraintException) {
            // Admin was created by another call, nothing to do
        }
    }

    fun observeUser(id: Long): Flow<UserEntity?> = dao.observeById(id)

    suspend fun updateProfile(user: UserEntity) = dao.update(user)
}