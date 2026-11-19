package com.example.e_learning.data

import android.os.Handler
import android.os.Looper
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.CourseEntity
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class ProgressRepository(private val database: AppDatabase) {
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    private fun <T> execute(onComplete: (Result<T>) -> Unit, block: suspend () -> T) {
        io.execute {
            val result = runCatching { runBlocking { block() } }
            main.post { onComplete(result) }
        }
    }

    fun loadStudentSummary(userId: Long, onComplete: (Result<StudentProgressSummary>) -> Unit) = execute(onComplete) {
        val user = database.userDao().findById(userId) ?: error("User not found.")
        val enrolledCourses = database.courseDao().getEnrolledByUser(userId)
        
        val coursesProgressList = enrolledCourses.map { course ->
            calculateCourseProgress(userId, course)
        }

        val totalCompletedLessons = database.progressDao().getCompletedCountForUser(userId)
        val quizResults = database.quizResultDao().getUserResults(userId)
        
        val recentQuizSummaries = quizResults.take(5).map { result ->
            val quiz = database.quizDao().getQuizById(result.quizId)
            val course = quiz?.let { database.courseDao().getById(it.courseId) }
            val percentage = if (result.maxScore > 0) (result.score * 100) / result.maxScore else 0
            QuizResultSummary(
                quizTitle = quiz?.title ?: "Quiz #${result.quizId}",
                courseTitle = course?.title ?: "Course",
                score = result.score,
                maxScore = result.maxScore,
                percentage = percentage
            )
        }

        val avgQuizScore = if (recentQuizSummaries.isNotEmpty()) {
            recentQuizSummaries.map { it.percentage }.average().toInt()
        } else {
            0
        }

        StudentProgressSummary(
            studentName = user.fullName,
            totalEnrolledCourses = enrolledCourses.size,
            totalCompletedLessons = totalCompletedLessons,
            averageQuizScorePercentage = avgQuizScore,
            coursesProgress = coursesProgressList,
            recentQuizResults = recentQuizSummaries
        )
    }

    fun loadCourseProgress(userId: Long, courseId: Long, onComplete: (Result<CourseProgress>) -> Unit) = execute(onComplete) {
        val course = database.courseDao().getById(courseId) ?: error("Course not found.")
        calculateCourseProgress(userId, course)
    }

    fun loadEnrolledCoursesProgress(userId: Long, onComplete: (Result<List<CourseProgress>>) -> Unit) = execute(onComplete) {
        val enrolledCourses = database.courseDao().getEnrolledByUser(userId)
        enrolledCourses.map { calculateCourseProgress(userId, it) }
    }

    private suspend fun calculateCourseProgress(userId: Long, course: CourseEntity): CourseProgress {
        val lessons = database.lessonDao().getLessonsForCourse(course.courseId)
        val totalLessons = lessons.size
        val completedLessons = database.progressDao().getCompletedCountForCourse(userId, course.courseId)
        val percentage = if (totalLessons > 0) (completedLessons * 100) / totalLessons else 0

        val lastOpened = database.progressDao().getLastOpenedProgressForCourse(userId, course.courseId)
        val lastOpenedTitle = lastOpened?.let { progress ->
            lessons.find { it.lessonId == progress.lessonId }?.title
        }

        return CourseProgress(
            course = course,
            totalLessons = totalLessons,
            completedLessons = completedLessons,
            progressPercentage = percentage,
            lastOpenedLessonTitle = lastOpenedTitle
        )
    }
}
