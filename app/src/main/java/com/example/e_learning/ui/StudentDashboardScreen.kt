
package com.example.e_learning.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.e_learning.R
import com.example.e_learning.data.CourseProgress
import com.example.e_learning.data.QuizResultSummary
import com.example.e_learning.data.StudentProgressSummary
import com.example.e_learning.entities.CourseEntity

class StudentDashboardScreen(
    private val context: Context,
    private val onCourseSelected: (CourseEntity) -> Unit
) {
    private val root = LayoutInflater.from(context).inflate(R.layout.screen_student_dashboard, null, false)
    private val welcomeTitle = root.findViewById<TextView>(R.id.dashboardWelcomeTitle)
    private val welcomeSubtitle = root.findViewById<TextView>(R.id.dashboardWelcomeSubtitle)
    private val statEnrolled = root.findViewById<TextView>(R.id.statEnrolledCount)
    private val statCompletedLessons = root.findViewById<TextView>(R.id.statCompletedLessonsCount)
    private val statQuizAvg = root.findViewById<TextView>(R.id.statQuizAvgScore)
    private val courseListContainer = root.findViewById<LinearLayout>(R.id.dashboardCourseList)
    private val quizListContainer = root.findViewById<LinearLayout>(R.id.dashboardQuizList)
    private val quizSectionTitle = root.findViewById<TextView>(R.id.quizSectionTitle)
    private val emptyView = root.findViewById<LinearLayout>(R.id.dashboardEmptyView)

    fun createView(): View = root

    fun render(summary: StudentProgressSummary) {
        welcomeTitle.text = "Welcome, ${summary.studentName}"
        welcomeSubtitle.text = "Track your learning progress and quiz performance below."

        statEnrolled.text = summary.totalEnrolledCourses.toString()
        statCompletedLessons.text = summary.totalCompletedLessons.toString()
        statQuizAvg.text = "${summary.averageQuizScorePercentage}%"

        renderCoursesProgress(summary.coursesProgress)
        renderQuizResults(summary.recentQuizResults)

        if (summary.coursesProgress.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    private fun renderCoursesProgress(coursesProgress: List<CourseProgress>) {
        courseListContainer.removeAllViews()
        coursesProgress.forEach { item ->
            val card = LayoutInflater.from(context).inflate(R.layout.item_student_progress_course, courseListContainer, false)
            
            card.findViewById<TextView>(R.id.courseIcon).text = item.course.language.take(2).uppercase()
            card.findViewById<TextView>(R.id.courseTitle).text = item.course.title
            card.findViewById<TextView>(R.id.courseMeta).text = "${item.course.language}  ·  ${item.course.level}"
            card.findViewById<TextView>(R.id.progressPercentageText).text = "${item.progressPercentage}%"

            val progressBar = card.findViewById<ProgressBar>(R.id.courseProgressBar)
            progressBar.max = 100
            progressBar.progress = item.progressPercentage

            card.findViewById<TextView>(R.id.lessonsCountText).text = "${item.completedLessons}/${item.totalLessons} completed"

            val lastOpenedView = card.findViewById<TextView>(R.id.lastOpenedLessonText)
            if (!item.lastOpenedLessonTitle.isNullOrBlank()) {
                lastOpenedView.text = "Resume: ${item.lastOpenedLessonTitle}"
                lastOpenedView.visibility = View.VISIBLE
            } else {
                lastOpenedView.visibility = View.GONE
            }

            card.setOnClickListener { onCourseSelected(item.course) }
            courseListContainer.addView(card)
        }
    }

    private fun renderQuizResults(quizzes: List<QuizResultSummary>) {
        quizListContainer.removeAllViews()
        if (quizzes.isEmpty()) {
            quizSectionTitle.visibility = View.GONE
            quizListContainer.visibility = View.GONE
            return
        }
        quizSectionTitle.visibility = View.VISIBLE
        quizListContainer.visibility = View.VISIBLE

        quizzes.forEach { quiz ->
            val row = LayoutInflater.from(context).inflate(R.layout.item_lesson_quiz, quizListContainer, false)
            row.findViewById<TextView>(R.id.quizItemTitle).text = quiz.quizTitle
            row.findViewById<TextView>(R.id.quizItemMeta).text = "${quiz.courseTitle}  ·  Score: ${quiz.score}/${quiz.maxScore} (${quiz.percentage}%)"
            row.findViewById<View>(R.id.quizItemChevron).visibility = View.GONE
            quizListContainer.addView(row)
        }
    }
}
