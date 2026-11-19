package com.example.e_learning.data
import androidx.room.withTransaction
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.CourseEntity
import com.example.e_learning.entities.EnrollmentEntity
import com.example.e_learning.entities.LessonEntity
import com.example.e_learning.entities.LessonProgressEntity
import com.example.e_learning.entities.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

data class AdminStats(
    val totalCourses: Int,
    val totalLessons: Int,
    val totalStudents: Int
)

data class StudentSummary(
    val userId: Long,
    val fullName: String,
    val email: String,
    val level: String,
    val enrolledCoursesCount: Int
)

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
                    val savedStudent = if (savedId == INVALID_USER_ID) null else database.userDao().findById(savedId)
                    savedStudent?.takeIf { it.role.equals("STUDENT", ignoreCase = true) }
                        ?: database.userDao().findFirstStudent()
                }
            }
            result.onSuccess { student ->
                if (student != null) preferences.edit().putLong(ACTIVE_USER_ID, student.userId).apply()
            }
            main.post { onComplete(result.mapCatching { it ?: error("No student profile found.") }) }
        }
    }

    fun loadCurrentAdmin(onComplete: (Result<UserEntity>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    database.userDao().findFirstAdmin() ?: createDemoAdmin()
                }
            }
            main.post { onComplete(result) }
        }
    }

    fun validateAdminRole(userId: Long, onComplete: (Result<Boolean>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    val user = database.userDao().findById(userId)
                    user?.role?.equals("ADMIN", ignoreCase = true) == true
                }
            }
            main.post { onComplete(result) }
        }
    }

    fun loadCourses(onComplete: (Result<List<CourseEntity>>) -> Unit) {
        io.execute {
            val result = runCatching { runBlocking { database.courseDao().observeAll().first() } }
            main.post { onComplete(result) }
        }
    }

    fun loadAdminStats(onComplete: (Result<AdminStats>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    val courses = database.courseDao().observeCount().first()
                    val lessons = database.lessonDao().observeCount().first()
                    val students = database.userDao().observeStudentCount().first()
                    AdminStats(courses, lessons, students)
                }
            }
            main.post { onComplete(result) }
        }
    }

    fun loadStudentsSummary(onComplete: (Result<List<StudentSummary>>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    val students = database.userDao().getAllStudents()
                    students.map { student ->
                        val count = database.enrollmentDao().getEnrollmentCountForUser(student.userId)
                        StudentSummary(
                            userId = student.userId,
                            fullName = student.fullName,
                            email = student.email,
                            level = student.level,
                            enrolledCoursesCount = count
                        )
                    }
                }
            }
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

    fun insertLesson(lesson: LessonEntity, onComplete: (Result<LessonEntity>) -> Unit) {
        io.execute {
            val result = runCatching {
                val newId = runBlocking {
                    database.lessonDao().insert(lesson) }
                lesson.copy(lessonId = newId)
            }
            main.post { onComplete(result) }
        }
    }

    fun updateLesson(lesson: LessonEntity, onComplete: (Result<Unit>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    database.lessonDao().update(lesson)
                }
            }
            main.post { onComplete(result) }
        }
    }

    fun deleteLesson(lesson: LessonEntity, onComplete: (Result<Unit>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    database.lessonDao().delete(lesson)
                }
            }
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

    fun createCourseWithLessons(course: CourseEntity, lessons: List<LessonEntity>, onComplete: (Result<Unit>) -> Unit) {
        io.execute {
            val result = runCatching {
                require(lessons.isNotEmpty()) {
                    "A course must contain at least one lesson." }
                require(course.courseId == 0L) {
                    "This operation is only for creating new courses." }
                runBlocking {
                    database.withTransaction {
                        val newCourseId = database.courseDao().insert(course)
                        lessons.forEachIndexed { index, lesson ->
                            val lessonToSave = lesson.copy(
                                courseId = newCourseId,
                                position = index + 1)
                            database.lessonDao().insert(lessonToSave)
                        }
                    }
                }
            }.map { Unit }
            main.post {
                onComplete(result)
            }
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

    fun recordLessonOpened(userId: Long, lessonId: Long, onComplete: (Result<LessonProgressEntity>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    val oldProgress = database.progressDao().getForLesson(userId, lessonId)
                    val updatedProgress = oldProgress?.copy(lastOpenedAt = System.currentTimeMillis()) ?: LessonProgressEntity(
                        userId = userId,
                        lessonId = lessonId,
                        completed = false,
                        lastOpenedAt = System.currentTimeMillis()
                    )
                    database.progressDao().save(updatedProgress)
                    updatedProgress
                }
            }

            main.post { onComplete(result) }
        }
    }

    fun setLessonCompleted(userId: Long, lessonId: Long, completed: Boolean, onComplete: (Result<Unit>) -> Unit) {
        io.execute {
            val result = runCatching {
                runBlocking {
                    val oldProgress = database.progressDao().getForLesson(userId, lessonId)
                    val updatedProgress = if (oldProgress != null) {
                        oldProgress.copy(
                            completed = completed,
                            completedAt = if (completed) {
                                System.currentTimeMillis()
                            } else { null }
                        )
                    } else {
                        LessonProgressEntity(
                            userId = userId,
                            lessonId = lessonId,
                            completed = completed,
                            completedAt = if (completed) {
                                System.currentTimeMillis()
                            } else { null }
                        )
                    }
                    database.progressDao().save(updatedProgress)
                }
            }
            main.post { onComplete(result) }
        }
    }

    private suspend fun createDemoAdmin(): UserEntity {
        val id = database.userDao().insert(
            UserEntity(
                fullName = "System Admin",
                email = "admin@local.elearning",
                passwordHash = "",
                role = "ADMIN"
            )
        )
        return database.userDao().findById(id) ?: error("Couldn't create the local admin profile.")
    }

    companion object {
        const val SESSION_PREFERENCES = "student_session"
        const val ACTIVE_USER_ID = "current_user_id"
        private const val INVALID_USER_ID = -1L
    }
}
