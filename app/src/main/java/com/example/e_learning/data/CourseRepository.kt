package com.example.e_learning.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.CourseEntity
import com.example.e_learning.entities.EnrollmentEntity
import com.example.e_learning.entities.LessonEntity
import com.example.e_learning.entities.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class CourseRepository(context: Context, private val database: AppDatabase) {
    private val appContext = context.applicationContext
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val preferences = appContext.getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE)

    fun setActiveUserId(userId: Long) {
        preferences.edit().putLong(ACTIVE_USER_ID, userId).apply()
    }

    fun loadCurrentStudent(onComplete: (Result<UserEntity>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    val savedId = preferences.getLong(ACTIVE_USER_ID, INVALID_USER_ID)
                    require(savedId != INVALID_USER_ID) { "No signed-in student session found." }
                    database.userDao().findById(savedId)
                        ?.takeIf { it.role.equals("STUDENT", ignoreCase = true) }
                        ?: error("The signed-in account is not a student.")
                }
            }
            result.onSuccess { student -> preferences.edit().putLong(ACTIVE_USER_ID, student.userId).apply() }
            main.post { onComplete(result) }
        }
    }

    fun loadCourses(onComplete: (Result<List<CourseEntity>>) -> Unit) {
        io.execute {
            val result = runCatching { runBlocking { database.courseDao().observeAll().first() } }
            main.post { onComplete(result) }
        }
    }

    fun loadEnrolledCourses(userId: Long, onComplete: (Result<List<CourseEntity>>) -> Unit) {
        io.execute {
            val result = runCatching { runBlocking { database.courseDao().observeEnrolledByUser(userId).first() } }
            main.post { onComplete(result) }
        }
    }

    fun loadCourse(courseId: Long, onComplete: (Result<CourseEntity?>) -> Unit) {
        io.execute {
            val result = runCatching { runBlocking { database.courseDao().getById(courseId) } }
            main.post { onComplete(result) }
        }
    }

    fun loadLessonsForCourse(courseId: Long, onComplete: (Result<List<LessonEntity>>) -> Unit) {
        io.execute {
            val result = runCatching { runBlocking { database.lessonDao().observeByCourse(courseId).first() } }
            main.post { onComplete(result) }
        }
    }

    fun loadLesson(lessonId: Long, onComplete: (Result<LessonEntity?>) -> Unit) {
        io.execute {
            val result = runCatching { runBlocking { database.lessonDao().getById(lessonId) } }
            main.post { onComplete(result) }
        }
    }
    fun saveCourse(course: CourseEntity, onComplete: (Result<Unit>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    if (course.courseId == 0L) database.courseDao().insert(course)
                    else check(database.courseDao().update(course) == 1) {
                        "The course could not be found for editing."
                    }
                }
            }.map { Unit }
            main.post { onComplete(result) }
        }
    }

    fun updateCourse(course: CourseEntity, onComplete: (Result<Unit>) -> Unit) {
        io.execute {
            val result = runCatching {
                require(course.courseId > 0L) { "An existing course ID is required for editing." }
                runBlocking {
                    check(database.courseDao().update(course) == 1) {
                        "The course could not be found for editing."
                    }
                }
            }.map { Unit }
            main.post { onComplete(result) }
        }
    }

    fun isEnrolled(userId: Long, courseId: Long, onComplete: (Result<Boolean>) -> Unit) {
        io.execute {
            val result = runCatching { runBlocking { database.enrollmentDao().isEnrolled(userId, courseId) } }
            main.post { onComplete(result) }
        }
    }

    fun enrollStudent(userId: Long, courseId: Long, onComplete: (Result<Boolean>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    database.enrollmentDao().enroll(EnrollmentEntity(userId = userId, courseId = courseId)) != -1L
                }
            }
            main.post { onComplete(result) }
        }
    }

    fun deleteCourse(course: CourseEntity, onComplete: (Result<Unit>) -> Unit) {
        io.execute {
            val result = runCatching { runBlocking { database.courseDao().delete(course) } }
            main.post { onComplete(result) }
        }
    }

    companion object {
        const val SESSION_PREFERENCES = "student_session"
        const val ACTIVE_USER_ID = "current_user_id"
        private const val INVALID_USER_ID = -1L
    }
}



