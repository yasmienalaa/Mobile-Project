package com.example.e_learning.dao

import androidx.room.*
import com.example.e_learning.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class QuizResultDao {
    @Insert
    abstract suspend fun save(result: QuizResultEntity): Long

    @Insert
    abstract suspend fun saveAnswers(answers: List<QuizAnswerEntity>)

    @Transaction
    open suspend fun saveAttempt(result: QuizResultEntity, answers: List<QuizAnswerEntity>): Long {
        require(result.resultId == 0L) { "A new attempt must not have an existing resultId" }
        require(answers.isNotEmpty()) { "An attempt must include its question snapshots" }
        require(answers.map { it.questionId }.distinct().size == answers.size) { "Duplicate questionId" }
        require(answers.all { it.selectedOption == null || it.selectedOption in listOf("A", "B", "C", "D") })
        require(answers.all { it.correctOption in listOf("A", "B", "C", "D") })
        val id = save(result)
        saveAnswers(answers.map { it.copy(answerId = 0L, resultId = id) })
        return id
    }

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY submittedAt DESC")
    abstract fun observeUserResults(userId: Long): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE quizId = :quizId ORDER BY submittedAt DESC")
    abstract fun observeQuizResults(quizId: Long): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results WHERE resultId = :resultId LIMIT 1")
    abstract suspend fun findById(resultId: Long): QuizResultEntity?

    @Query("SELECT * FROM quiz_answers WHERE resultId = :resultId ORDER BY position")
    abstract fun observeAnswersForAttempt(resultId: Long): Flow<List<QuizAnswerEntity>>

    @Query("SELECT * FROM quiz_answers WHERE resultId = :resultId ORDER BY position")
    abstract suspend fun getAnswersForAttempt(resultId: Long): List<QuizAnswerEntity>


    @Query("""
    SELECT * FROM quiz_results
    WHERE userId = :userId AND quizId = :quizId
    ORDER BY submittedAt DESC""")
    abstract suspend fun getUserResultsForQuiz(
        userId: Long,
        quizId: Long
    ): List<QuizResultEntity>

    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY submittedAt DESC")
    abstract suspend fun getUserResults(userId: Long): List<QuizResultEntity>
}
