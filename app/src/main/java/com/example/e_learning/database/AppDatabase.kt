package com.example.e_learning.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.e_learning.dao.*
import com.example.e_learning.entities.*

@Database(
    entities = [UserEntity::class, CourseEntity::class, LessonEntity::class,
        EnrollmentEntity::class, LessonProgressEntity::class, QuizEntity::class,
        QuestionEntity::class, QuizResultEntity::class, QuizAnswerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun progressDao(): ProgressDao
    abstract fun quizDao(): QuizDao
    abstract fun quizResultDao(): QuizResultDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "elearning_db")
                .build().also { INSTANCE = it }
        }
    }
}
