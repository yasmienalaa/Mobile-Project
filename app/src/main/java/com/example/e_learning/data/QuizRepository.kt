package com.example.e_learning.data

import android.os.Handler
import android.os.Looper
import androidx.room.withTransaction
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.LessonEntity
import com.example.e_learning.entities.QuestionEntity
import com.example.e_learning.entities.QuizEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

data class LessonQuizSummary(val lesson: LessonEntity, val quizCount: Int)
data class QuizSummary(val quiz: QuizEntity, val questionCount: Int)

class QuizRepository(private val database: AppDatabase) {
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val dao get() = database.quizDao()

    private fun <T> execute(onComplete: (Result<T>) -> Unit, block: suspend () -> T) {
        io.execute {
            val result = runCatching { runBlocking { block() } }
            main.post { onComplete(result) }
        }
    }

    // ---- Lessons (with their quiz count) ----
    fun loadLessonSummaries(courseId: Long, onComplete: (Result<List<LessonQuizSummary>>) -> Unit) = execute(onComplete) {
        database.lessonDao().observeByCourse(courseId).first()
            .map { LessonQuizSummary(it, dao.countQuizzesForLesson(it.lessonId)) }
    }

    /**
     * The only safe way to create a lesson: the lesson and its first quiz are saved together,
     * so a lesson can never exist without a quiz. Use this from the future "add lesson" screen.
     */
    fun createLessonWithQuiz(lesson: LessonEntity, firstQuizTitle: String, onComplete: (Result<Long>) -> Unit) = execute(onComplete) {
        require(firstQuizTitle.isNotBlank()) { "A lesson needs at least one quiz." }
        database.withTransaction {
            val lessonId = database.lessonDao().insert(lesson)
            dao.insertQuiz(QuizEntity(courseId = lesson.courseId, lessonId = lessonId, title = firstQuizTitle.trim()))
            lessonId
        }
    }

    // ---- Quizzes ----
    fun loadQuizSummaries(lessonId: Long, onComplete: (Result<List<QuizSummary>>) -> Unit) = execute(onComplete) {
        dao.getQuizzesByLesson(lessonId).map { QuizSummary(it, dao.countQuestions(it.quizId)) }
    }

    fun saveQuiz(quiz: QuizEntity, onComplete: (Result<Unit>) -> Unit) = execute(onComplete) {
        require(quiz.title.isNotBlank()) { "Quiz title is required." }
        require(quiz.lessonId > 0 && quiz.courseId > 0) { "A quiz must belong to a lesson." }
        if (quiz.quizId == 0L) dao.insertQuiz(quiz) else dao.updateQuiz(quiz)
        Unit
    }

    /** Refuses to delete the last quiz of a lesson. */
    fun deleteQuiz(quiz: QuizEntity, onComplete: (Result<Unit>) -> Unit) = execute(onComplete) {
        database.withTransaction {
            check(dao.countQuizzesForLesson(quiz.lessonId) > 1) { LAST_QUIZ_MESSAGE }
            dao.deleteQuiz(quiz)
        }
    }

    // ---- Questions ----
    fun loadQuestions(quizId: Long, onComplete: (Result<List<QuestionEntity>>) -> Unit) = execute(onComplete) {
        dao.getQuestions(quizId)
    }

    fun saveQuestion(question: QuestionEntity, onComplete: (Result<Unit>) -> Unit) = execute(onComplete) {
        validate(question)
        if (question.questionId == 0L) {
            dao.insertQuestion(question.copy(position = dao.nextQuestionPosition(question.quizId)))
        } else {
            dao.updateQuestion(question)
        }
        Unit
    }

    fun deleteQuestion(question: QuestionEntity, onComplete: (Result<Unit>) -> Unit) = execute(onComplete) {
        dao.deleteQuestion(question)
    }


    fun saveQuizWithQuestions(
        quiz: QuizEntity,
        questions: List<QuestionEntity>,
        onComplete: (Result<Unit>) -> Unit
    ) = execute(onComplete) {

        require(quiz.title.isNotBlank()) {
            "Quiz title is required."
        }

        require(quiz.lessonId > 0 && quiz.courseId > 0) {
            "A quiz must belong to a lesson."
        }

        require(questions.isNotEmpty()) {
            "Add at least one question."
        }

        questions.forEach { question ->
            validate(question)
        }

        database.withTransaction {

            val savedQuizId = if (quiz.quizId == 0L) {
                dao.insertQuiz(quiz)
            } else {
                dao.updateQuiz(quiz)
                quiz.quizId
            }

            dao.deleteQuestionsForQuiz(savedQuizId)

            val preparedQuestions = questions.mapIndexed { index, question ->
                question.copy(
                    questionId = 0L,
                    quizId = savedQuizId,
                    position = index + 1
                )
            }

            dao.insertQuestions(preparedQuestions)
        }

        Unit
    }

    private fun validate(q: QuestionEntity) {
        require(q.questionText.isNotBlank()) { "Question text is required." }
        require(listOf(q.optionA, q.optionB, q.optionC, q.optionD).all { it.isNotBlank() }) { "All four options are required." }
        require(q.correctOption in listOf("A", "B", "C", "D")) { "Correct option must be A, B, C or D." }
        require(q.points >= 1) { "Points must be at least 1." }
    }

    companion object {
        const val LAST_QUIZ_MESSAGE = "Each lesson needs at least one quiz. Add another quiz before deleting this one."
    }
}
