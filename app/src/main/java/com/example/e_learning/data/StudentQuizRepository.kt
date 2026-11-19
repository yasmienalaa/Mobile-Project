package com.example.e_learning.data

import android.os.Handler
import android.os.Looper
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.QuestionEntity
import com.example.e_learning.entities.QuizAnswerEntity
import com.example.e_learning.entities.QuizEntity
import com.example.e_learning.entities.QuizResultEntity
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

data class LessonQuizItem(val quiz: QuizEntity, val questionCount: Int, val best: QuizResultEntity?)

data class QuizAttemptData(val quiz: QuizEntity, val questions: List<QuestionEntity>)

data class AttemptReview(val result: QuizResultEntity, val quizTitle: String, val answers: List<QuizAnswerEntity>)

class StudentQuizRepository(private val database: AppDatabase) {
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val quizDao get() = database.quizDao()
    private val resultDao get() = database.quizResultDao()

    private fun <T> execute(onComplete: (Result<T>) -> Unit, block: suspend () -> T) {
        io.execute {
            val result = runCatching { runBlocking { block() } }
            main.post { onComplete(result) }
        }
    }

    fun loadLessonQuizzes(lessonId: Long, userId: Long, onComplete: (Result<List<LessonQuizItem>>) -> Unit) = execute(onComplete) {
        quizDao.getQuizzesByLesson(lessonId).map { quiz ->
            val attempts = resultDao.getUserResultsForQuiz(userId, quiz.quizId)
            val best = attempts.filter { it.maxScore > 0 }.maxByOrNull { it.score.toDouble() / it.maxScore }
            LessonQuizItem(quiz, quizDao.countQuestions(quiz.quizId), best)
        }
    }

    fun loadAttemptData(quizId: Long, userId: Long, onComplete: (Result<QuizAttemptData>) -> Unit) = execute(onComplete) {
        val quiz = quizDao.getQuizById(quizId) ?: error("This quiz no longer exists.")
        check(database.enrollmentDao().isEnrolled(userId, quiz.courseId)) { "Enroll in the course to take its quizzes." }
        val questions = quizDao.getQuestions(quizId)
        check(questions.isNotEmpty()) { "This quiz has no questions yet." }
        QuizAttemptData(quiz, questions)
    }


    fun submitAttempt(
        userId: Long,
        quiz: QuizEntity,
        questions: List<QuestionEntity>,
        selected: Map<Long, String>,
        onComplete: (Result<Long>) -> Unit
    ) = execute(onComplete) {
        check(database.enrollmentDao().isEnrolled(userId, quiz.courseId)) { "Enroll in the course to take its quizzes." }
        val answers = questions.map { q ->
            val choice = selected[q.questionId]?.takeIf { it in OPTIONS }
            QuizAnswerEntity(
                resultId = 0L, questionId = q.questionId, questionText = q.questionText,
                optionA = q.optionA, optionB = q.optionB, optionC = q.optionC, optionD = q.optionD,
                selectedOption = choice, correctOption = q.correctOption,
                pointsAwarded = if (choice == q.correctOption) q.points else 0,
                maxPoints = q.points, position = q.position
            )
        }
        val result = QuizResultEntity(
            userId = userId, quizId = quiz.quizId,
            score = answers.sumOf { it.pointsAwarded }, maxScore = answers.sumOf { it.maxPoints }
        )
        resultDao.saveAttempt(result, answers)
    }

    fun loadAttempt(resultId: Long, userId: Long, onComplete: (Result<AttemptReview>) -> Unit) = execute(onComplete) {
        val result = resultDao.findById(resultId) ?: error("Result not found.")
        check(result.userId == userId) { "Result not found." }
        AttemptReview(
            result = result,
            quizTitle = quizDao.getQuizById(result.quizId)?.title ?: "Quiz",
            answers = resultDao.getAnswersForAttempt(resultId)
        )
    }

    private companion object {
        val OPTIONS = listOf("A", "B", "C", "D")
    }
}
