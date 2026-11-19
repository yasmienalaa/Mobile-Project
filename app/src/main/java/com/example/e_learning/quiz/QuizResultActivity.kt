package com.example.e_learning.quiz

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.R
import com.example.e_learning.auth.Role
import com.example.e_learning.auth.SessionManager
import com.example.e_learning.data.AttemptReview
import com.example.e_learning.data.StudentQuizRepository
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.QuizAnswerEntity

class QuizResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(this)
        val userId = session.userId
        val resultId = intent.getLongExtra(EXTRA_RESULT_ID, -1L)
        if (userId == null || session.role != Role.STUDENT || resultId == -1L) { finish(); return }

        setContentView(R.layout.activity_quiz_result)
        findViewById<TextView>(R.id.resultBackButton).setOnClickListener { finish() }
        StudentQuizRepository(AppDatabase.getInstance(this)).loadAttempt(resultId, userId) { result ->
            result.onSuccess { show(it) }.onFailure {
                Toast.makeText(this, "Couldn't load the result.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun show(review: AttemptReview) {
        val r = review.result
        findViewById<TextView>(R.id.resultQuizTitle).text = review.quizTitle
        findViewById<TextView>(R.id.resultScore).text = "${r.score} / ${r.maxScore}"
        val percent = if (r.maxScore > 0) r.score * 100 / r.maxScore else 0
        findViewById<TextView>(R.id.resultPercent).text = "$percent%"
        findViewById<TextView>(R.id.resultMessage).text = when {
            percent >= 80 -> "Great work!"
            percent >= 50 -> "Good effort. Review the answers below."
            else -> "Keep practicing. Review the answers below."
        }
        findViewById<TextView>(R.id.retakeButton).setOnClickListener {
            startActivity(Intent(this, QuizActivity::class.java).putExtra(QuizActivity.EXTRA_QUIZ_ID, r.quizId))
            finish()
        }

        val list = findViewById<android.widget.LinearLayout>(R.id.reviewList)
        review.answers.forEachIndexed { i, a ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_quiz_review, list, false)
            row.findViewById<TextView>(R.id.reviewQuestion).text = "${i + 1}. ${a.questionText}"

            val yourAnswer = row.findViewById<TextView>(R.id.reviewYourAnswer)
            val correct = a.selectedOption == a.correctOption
            yourAnswer.text = when {
                a.selectedOption == null -> "✗  Not answered"
                correct -> "✓  Your answer: ${a.selectedOption}. ${a.optionText(a.selectedOption)}"
                else -> "✗  Your answer: ${a.selectedOption}. ${a.optionText(a.selectedOption)}"
            }
            yourAnswer.setTextColor(if (correct) 0xFF275346.toInt() else 0xFF914B38.toInt())

            val correctView = row.findViewById<TextView>(R.id.reviewCorrectAnswer)
            if (!correct) {
                correctView.text = "Correct answer: ${a.correctOption}. ${a.optionText(a.correctOption)}"
                correctView.visibility = View.VISIBLE
            }
            row.findViewById<TextView>(R.id.reviewPoints).text = "${a.pointsAwarded} / ${a.maxPoints} ${if (a.maxPoints == 1) "point" else "points"}"
            list.addView(row)
        }
    }

    private fun QuizAnswerEntity.optionText(letter: String?): String = when (letter) {
        "A" -> optionA
        "B" -> optionB
        "C" -> optionC
        "D" -> optionD
        else -> ""
    }

    companion object {
        const val EXTRA_RESULT_ID = "extra_result_id"
    }
}
