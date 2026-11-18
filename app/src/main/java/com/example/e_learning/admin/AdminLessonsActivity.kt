package com.example.e_learning.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.R
import com.example.e_learning.auth.Role
import com.example.e_learning.auth.SessionManager
import com.example.e_learning.data.LessonQuizSummary
import com.example.e_learning.data.QuizRepository
import com.example.e_learning.database.AppDatabase

/** Admin: the lessons of one course, each showing how many quizzes it has. Tap a lesson to manage its quizzes. */
class AdminLessonsActivity : AppCompatActivity() {
    private lateinit var quizzes: QuizRepository
    private lateinit var list: LinearLayout
    private lateinit var emptyMessage: TextView
    private var courseId = INVALID_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SessionManager(this).role != Role.ADMIN) { finish(); return }
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, INVALID_ID)
        if (courseId == INVALID_ID) { finish(); return }

        setContentView(R.layout.activity_admin_list)
        quizzes = QuizRepository(AppDatabase.getInstance(this))
        list = findViewById(R.id.adminList)
        emptyMessage = findViewById(R.id.adminEmptyMessage)
        findViewById<TextView>(R.id.adminBackButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.adminListTitle).text = "Lesson quizzes"
        findViewById<TextView>(R.id.adminListSubtitle).text =
            intent.getStringExtra(EXTRA_COURSE_TITLE).orEmpty() + "  ·  every lesson needs at least one quiz"
        findViewById<View>(R.id.adminAddButton).visibility = View.GONE // lessons themselves are managed elsewhere
        emptyMessage.text = "This course has no lessons yet. Add lessons first, then give each one a quiz."
    }

    override fun onResume() {
        super.onResume()
        if (courseId != INVALID_ID && ::quizzes.isInitialized) refresh()
    }

    private fun refresh() {
        quizzes.loadLessonSummaries(courseId) { result ->
            result.onSuccess { render(it) }.onFailure { toast("Couldn't load lessons.") }
        }
    }

    private fun render(items: List<LessonQuizSummary>) {
        list.removeAllViews()
        emptyMessage.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        items.forEach { summary ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_admin_row, list, false)
            row.findViewById<TextView>(R.id.rowTitle).text = "${summary.lesson.position}. ${summary.lesson.title}"
            val meta = row.findViewById<TextView>(R.id.rowMeta)
            if (summary.quizCount == 0) {
                meta.text = "⚠  No quiz yet. Add one"
                meta.setTextColor(0xFF914B38.toInt())
            } else {
                meta.text = "${summary.quizCount} ${if (summary.quizCount == 1) "quiz" else "quizzes"}"
            }
            row.findViewById<View>(R.id.rowEditButton).visibility = View.GONE
            row.findViewById<View>(R.id.rowDeleteButton).visibility = View.GONE
            row.setOnClickListener {
                startActivity(Intent(this, AdminQuizzesActivity::class.java)
                    .putExtra(AdminQuizzesActivity.EXTRA_COURSE_ID, courseId)
                    .putExtra(AdminQuizzesActivity.EXTRA_LESSON_ID, summary.lesson.lessonId)
                    .putExtra(AdminQuizzesActivity.EXTRA_LESSON_TITLE, summary.lesson.title))
            }
            list.addView(row)
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_COURSE_ID = "extra_course_id"
        const val EXTRA_COURSE_TITLE = "extra_course_title"
        private const val INVALID_ID = -1L
    }
}
