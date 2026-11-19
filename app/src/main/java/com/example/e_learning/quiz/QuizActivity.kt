package com.example.e_learning.quiz

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.R
import com.example.e_learning.auth.Role
import com.example.e_learning.auth.SessionManager
import com.example.e_learning.data.QuizAttemptData
import com.example.e_learning.data.StudentQuizRepository
import com.example.e_learning.database.AppDatabase

class QuizActivity : AppCompatActivity() {
    private lateinit var repository: StudentQuizRepository
    private lateinit var optionContainer: LinearLayout
    private lateinit var counter: TextView
    private lateinit var questionText: TextView
    private lateinit var prevButton: TextView
    private lateinit var nextButton: TextView

    private var userId = -1L
    private var data: QuizAttemptData? = null
    private val selected = LinkedHashMap<Long, String>() // questionId -> "A".."D"
    private var index = 0
    private var submitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(this)
        val uid = session.userId
        val quizId = intent.getLongExtra(EXTRA_QUIZ_ID, -1L)
        if (uid == null || session.role != Role.STUDENT || quizId == -1L) { finish(); return }
        userId = uid

        setContentView(R.layout.activity_quiz)
        optionContainer = findViewById(R.id.optionContainer)
        counter = findViewById(R.id.quizCounter)
        questionText = findViewById(R.id.questionText)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)
        repository = StudentQuizRepository(AppDatabase.getInstance(this))

        // Survive rotation: restore the answers and the current question.
        savedInstanceState?.let { state ->
            index = state.getInt(STATE_INDEX, 0)
            val ids = state.getLongArray(STATE_IDS) ?: LongArray(0)
            val options = state.getStringArray(STATE_OPTIONS) ?: emptyArray()
            ids.zip(options).forEach { (id, option) -> selected[id] = option }
        }

        findViewById<TextView>(R.id.quizCloseButton).setOnClickListener { confirmLeave() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = confirmLeave()
        })
        prevButton.setOnClickListener { if (index > 0) { index--; render() } }
        nextButton.setOnClickListener { onNext() }
        nextButton.isEnabled = false

        repository.loadAttemptData(quizId, userId) { result ->
            result.onSuccess { loaded ->
                data = loaded
                findViewById<TextView>(R.id.quizTitle).text = loaded.quiz.title
                index = index.coerceIn(0, loaded.questions.lastIndex)
                nextButton.isEnabled = true
                render()
            }.onFailure {
                toast(it.message ?: "Couldn't load the quiz.")
                finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_INDEX, index)
        outState.putLongArray(STATE_IDS, selected.keys.toLongArray())
        outState.putStringArray(STATE_OPTIONS, selected.values.toTypedArray())
    }

    private fun render() {
        val d = data ?: return
        val question = d.questions[index]
        counter.text = "Question ${index + 1} of ${d.questions.size}"
        questionText.text = question.questionText
        prevButton.visibility = if (index > 0) View.VISIBLE else View.INVISIBLE
        nextButton.text = if (index == d.questions.lastIndex) "Submit" else "Next"

        optionContainer.removeAllViews()
        val options = listOf("A" to question.optionA, "B" to question.optionB, "C" to question.optionC, "D" to question.optionD)
        options.forEach { (letter, text) ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_quiz_option, optionContainer, false)
            val letterView = row.findViewById<TextView>(R.id.optionLetter)
            val textView = row.findViewById<TextView>(R.id.optionText)
            letterView.text = letter
            textView.text = text
            val isSelected = selected[question.questionId] == letter
            if (isSelected) {
                row.setBackgroundResource(R.drawable.bg_primary_action)
                letterView.setTextColor(getColor(android.R.color.white))
                textView.setTextColor(getColor(android.R.color.white))
            }
            row.contentDescription = "Option $letter: $text${if (isSelected) ", selected" else ""}"
            row.setOnClickListener { selected[question.questionId] = letter; render() }
            optionContainer.addView(row)
        }
    }

    private fun onNext() {
        val d = data ?: return
        if (index < d.questions.lastIndex) { index++; render(); return }
        val unanswered = d.questions.count { it.questionId !in selected }
        if (unanswered == 0) { submit(); return }
        AlertDialog.Builder(this)
            .setTitle("Submit with unanswered questions?")
            .setMessage("$unanswered ${if (unanswered == 1) "question is" else "questions are"} still unanswered and will count as wrong.")
            .setNegativeButton("Keep answering", null)
            .setPositiveButton("Submit") { _, _ -> submit() }
            .show()
    }

    private fun submit() {
        val d = data ?: return
        if (submitting) return
        submitting = true
        nextButton.isEnabled = false
        repository.submitAttempt(userId, d.quiz, d.questions, selected) { result ->
            result.onSuccess { resultId ->
                startActivity(Intent(this, QuizResultActivity::class.java)
                    .putExtra(QuizResultActivity.EXTRA_RESULT_ID, resultId))
                finish()
            }.onFailure {
                submitting = false
                nextButton.isEnabled = true
                toast(it.message ?: "Couldn't submit your answers. Please try again.")
            }
        }
    }

    private fun confirmLeave() {
        if (selected.isEmpty() || submitting) { finish(); return }
        AlertDialog.Builder(this)
            .setTitle("Leave the quiz?")
            .setMessage("Your answers so far will be lost.")
            .setNegativeButton("Stay", null)
            .setPositiveButton("Leave") { _, _ -> finish() }
            .show()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    companion object {
        const val EXTRA_QUIZ_ID = "extra_quiz_id"
        private const val STATE_INDEX = "state_index"
        private const val STATE_IDS = "state_ids"
        private const val STATE_OPTIONS = "state_options"
    }
}
