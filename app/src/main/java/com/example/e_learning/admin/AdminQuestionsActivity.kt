package com.example.e_learning.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.R
import com.example.e_learning.auth.Role
import com.example.e_learning.auth.SessionManager
import com.example.e_learning.data.QuizRepository
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.QuestionEntity

/** Admin: list / add / edit / delete the multiple-choice questions of  quiz. */
class AdminQuestionsActivity : AppCompatActivity() {
    private lateinit var quizzes: QuizRepository
    private lateinit var list: LinearLayout
    private lateinit var emptyMessage: TextView
    private var quizId = INVALID_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SessionManager(this).role != Role.ADMIN) { finish(); return }
        quizId = intent.getLongExtra(EXTRA_QUIZ_ID, INVALID_ID)
        if (quizId == INVALID_ID) { finish(); return }

        setContentView(R.layout.activity_admin_list)
        quizzes = QuizRepository(AppDatabase.getInstance(this))
        list = findViewById(R.id.adminList)
        emptyMessage = findViewById(R.id.adminEmptyMessage)
        findViewById<TextView>(R.id.adminBackButton).setOnClickListener { finish() }
        findViewById<TextView>(R.id.adminListTitle).text = "Questions"
        findViewById<TextView>(R.id.adminListSubtitle).text = intent.getStringExtra(EXTRA_QUIZ_TITLE).orEmpty()
        findViewById<TextView>(R.id.adminAddButton).apply {
            text = "＋   Add new question"
            setOnClickListener { showQuestionDialog(null) }
        }
        emptyMessage.text = "No questions yet. Add the first one."
    }

    override fun onResume() {
        super.onResume()
        if (quizId != INVALID_ID && ::quizzes.isInitialized) refresh()
    }

    private fun refresh() {
        quizzes.loadQuestions(quizId) { result ->
            result.onSuccess { render(it) }.onFailure { toast("Couldn't load questions.") }
        }
    }

    private fun render(questions: List<QuestionEntity>) {
        list.removeAllViews()
        emptyMessage.visibility = if (questions.isEmpty()) View.VISIBLE else View.GONE
        questions.forEachIndexed { index, q ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_admin_row, list, false)
            row.findViewById<TextView>(R.id.rowTitle).text = "${index + 1}. ${q.questionText}"
            row.findViewById<TextView>(R.id.rowMeta).text =
                "Correct: ${q.correctOption}  ·  ${q.points} ${if (q.points == 1) "point" else "points"}"
            row.setOnClickListener { showQuestionDialog(q) }
            row.findViewById<TextView>(R.id.rowEditButton).setOnClickListener { showQuestionDialog(q) }
            row.findViewById<TextView>(R.id.rowDeleteButton).setOnClickListener { confirmDelete(q) }
            list.addView(row)
        }
    }

    private fun showQuestionDialog(question: QuestionEntity?) {
        val form = LayoutInflater.from(this).inflate(R.layout.dialog_question_form, null, false)
        val text = form.findViewById<EditText>(R.id.questionTextInput)
        val a = form.findViewById<EditText>(R.id.optionAInput)
        val b = form.findViewById<EditText>(R.id.optionBInput)
        val c = form.findViewById<EditText>(R.id.optionCInput)
        val d = form.findViewById<EditText>(R.id.optionDInput)
        val correct = form.findViewById<RadioGroup>(R.id.correctOptionGroup)
        val points = form.findViewById<EditText>(R.id.pointsInput)

        question?.let {
            text.setText(it.questionText); a.setText(it.optionA); b.setText(it.optionB)
            c.setText(it.optionC); d.setText(it.optionD); points.setText(it.points.toString())
            correct.check(when (it.correctOption) {
                "A" -> R.id.correctA; "B" -> R.id.correctB; "C" -> R.id.correctC; else -> R.id.correctD
            })
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (question == null) "Add new question" else "Edit question")
            .setView(form)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.setOnShowListener {
            val save = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            save.setOnClickListener {
                val options = listOf(a, b, c, d).map { it.text.toString().trim() }
                if (text.text.isBlank() || options.any { it.isEmpty() }) {
                    toast("Fill in the question and all four options."); return@setOnClickListener
                }
                val correctOption = when (correct.checkedRadioButtonId) {
                    R.id.correctA -> "A"; R.id.correctB -> "B"; R.id.correctC -> "C"; R.id.correctD -> "D"
                    else -> { toast("Choose the correct answer."); return@setOnClickListener }
                }
                val pts = points.text.toString().trim().ifEmpty { "1" }.toIntOrNull()
                if (pts == null || pts < 1) { toast("Points must be 1 or more."); return@setOnClickListener }

                val item = QuestionEntity(
                    questionId = question?.questionId ?: 0,
                    quizId = quizId,
                    questionText = text.text.toString().trim(),
                    optionA = options[0], optionB = options[1], optionC = options[2], optionD = options[3],
                    correctOption = correctOption,
                    points = pts,
                    position = question?.position ?: 0 // new questions get the next position in the repository
                )
                save.isEnabled = false
                quizzes.saveQuestion(item) { result ->
                    result.onSuccess { dialog.dismiss(); refresh() }
                        .onFailure { save.isEnabled = true; toast(it.message ?: "Couldn't save question.") }
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(question: QuestionEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete question?")
            .setMessage("Past student attempts keep their own copy of this question.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                quizzes.deleteQuestion(question) { result ->
                    result.onSuccess { refresh() }.onFailure { toast("Couldn't delete question.") }
                }
            }
            .show()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_QUIZ_ID = "extra_quiz_id"
        const val EXTRA_QUIZ_TITLE = "extra_quiz_title"
        private const val INVALID_ID = -1L
    }
}
