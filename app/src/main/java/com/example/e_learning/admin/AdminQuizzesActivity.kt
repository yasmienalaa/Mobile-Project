package com.example.e_learning.admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.R
import com.example.e_learning.auth.Role
import com.example.e_learning.auth.SessionManager
import com.example.e_learning.data.QuizRepository
import com.example.e_learning.data.QuizSummary
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.QuizEntity

/** Admin: list / add / edit / delete the quizzes of one lesson. A lesson must always keep at least one quiz. */
class AdminQuizzesActivity : AppCompatActivity() {
    private lateinit var quizzes: QuizRepository
    private lateinit var list: LinearLayout
    private lateinit var emptyMessage: TextView
    private var courseId = INVALID_ID
    private var lessonId = INVALID_ID
    private var currentCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SessionManager(this).role != Role.ADMIN) { finish(); return }
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, INVALID_ID)
        lessonId = intent.getLongExtra(EXTRA_LESSON_ID, INVALID_ID)
        if (courseId == INVALID_ID || lessonId == INVALID_ID) { finish(); return }

        setContentView(R.layout.activity_admin_list)
        quizzes = QuizRepository(AppDatabase.getInstance(this))
        list = findViewById(R.id.adminList)
        emptyMessage = findViewById(R.id.adminEmptyMessage)
        findViewById<TextView>(R.id.adminBackButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.adminListTitle).text = "Quizzes"
        findViewById<TextView>(R.id.adminListSubtitle).text = intent.getStringExtra(EXTRA_LESSON_TITLE).orEmpty()
        findViewById<TextView>(R.id.adminAddButton).apply {
            text = "＋   Add new quiz"
            setOnClickListener { showQuizDialog(null) }
        }
        emptyMessage.text = "This lesson has no quiz yet. Every lesson needs at least one, so add it now."
    }

    override fun onResume() {
        super.onResume()
        if (lessonId != INVALID_ID && ::quizzes.isInitialized) refresh()
    }

    private fun refresh() {
        quizzes.loadQuizSummaries(lessonId) { result ->
            result.onSuccess { render(it) }.onFailure { toast("Couldn't load quizzes.") }
        }
    }

    private fun render(items: List<QuizSummary>) {
        currentCount = items.size
        list.removeAllViews()
        emptyMessage.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        items.forEach { summary ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_admin_row, list, false)
            row.findViewById<TextView>(R.id.rowTitle).text = summary.quiz.title
            val meta = row.findViewById<TextView>(R.id.rowMeta)
            val n = summary.questionCount
            if (n == 0) {
                meta.text = "⚠  No questions yet. Tap to add"
                meta.setTextColor(0xFF914B38.toInt())
            } else {
                meta.text = "$n ${if (n == 1) "question" else "questions"}"
            }
            row.setOnClickListener {
                startActivity(Intent(this, AdminQuestionsActivity::class.java)
                    .putExtra(AdminQuestionsActivity.EXTRA_QUIZ_ID, summary.quiz.quizId)
                    .putExtra(AdminQuestionsActivity.EXTRA_QUIZ_TITLE, summary.quiz.title))
            }
            row.findViewById<TextView>(R.id.rowEditButton).setOnClickListener { showQuizDialog(summary.quiz) }
            row.findViewById<TextView>(R.id.rowDeleteButton).setOnClickListener { confirmDelete(summary.quiz) }
            list.addView(row)
        }
    }

    private fun showQuizDialog(quiz: QuizEntity?) {
        val form = LayoutInflater.from(this).inflate(R.layout.dialog_quiz_form, null, false)
        val title = form.findViewById<EditText>(R.id.quizTitleInput)
        val description = form.findViewById<EditText>(R.id.quizDescriptionInput)
        title.setText(quiz?.title.orEmpty())
        description.setText(quiz?.description.orEmpty())

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (quiz == null) "Add new quiz" else "Edit quiz")
            .setView(form)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.setOnShowListener {
            val save = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            save.setOnClickListener {
                if (title.text.isBlank()) { toast("Add a quiz title."); return@setOnClickListener }
                val item = (quiz ?: QuizEntity(courseId = courseId, lessonId = lessonId, title = "")).copy(
                    title = title.text.toString().trim(),
                    description = description.text.toString().trim()
                )
                save.isEnabled = false
                quizzes.saveQuiz(item) { result ->
                    result.onSuccess { dialog.dismiss(); refresh() }
                        .onFailure { save.isEnabled = true; toast("Couldn't save quiz.") }
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(quiz: QuizEntity) {
        if (currentCount <= 1) { // friendly pre-check; the repository enforces the same rule
            AlertDialog.Builder(this)
                .setTitle("Can't delete the only quiz")
                .setMessage(QuizRepository.LAST_QUIZ_MESSAGE)
                .setPositiveButton("OK", null)
                .show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Delete quiz?")
            .setMessage("\"${quiz.title}\", its questions, and all students' results for it will be permanently deleted.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                quizzes.deleteQuiz(quiz) { result ->
                    result.onSuccess { refresh() }.onFailure { toast(it.message ?: "Couldn't delete quiz.") }
                }
            }
            .show()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_COURSE_ID = "extra_course_id"
        const val EXTRA_LESSON_ID = "extra_lesson_id"
        const val EXTRA_LESSON_TITLE = "extra_lesson_title"
        private const val INVALID_ID = -1L
    }
}
