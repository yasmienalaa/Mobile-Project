package com.example.e_learning.dao

import androidx.room.*
import com.example.e_learning.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface QuizDao {
    @Insert suspend fun insertQuiz(quiz: QuizEntity): Long
    @Update suspend fun updateQuiz(quiz: QuizEntity)
    @Delete suspend fun deleteQuiz(quiz: QuizEntity)
    @Query("SELECT * FROM quizzes WHERE courseId = :courseId") fun observeByCourse(courseId: Long): Flow<List<QuizEntity>>
    @Insert suspend fun insertQuestion(question: QuestionEntity): Long
    @Insert
    suspend fun insertQuestions(
        questions: List<QuestionEntity>
    ): List<Long>

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: Long)

    @Update suspend fun updateQuestion(question: QuestionEntity)
    @Delete suspend fun deleteQuestion(question: QuestionEntity)
    @Query("SELECT * FROM questions WHERE quizId = :quizId ORDER BY position") fun observeQuestions(quizId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId LIMIT 1") suspend fun getQuizById(quizId: Long): QuizEntity?
    @Query("SELECT * FROM quizzes WHERE lessonId = :lessonId ORDER BY quizId") suspend fun getQuizzesByLesson(lessonId: Long): List<QuizEntity>
    @Query("SELECT COUNT(*) FROM quizzes WHERE lessonId = :lessonId") suspend fun countQuizzesForLesson(lessonId: Long): Int
    @Query("SELECT * FROM questions WHERE quizId = :quizId ORDER BY position") suspend fun getQuestions(quizId: Long): List<QuestionEntity>
    @Query("SELECT COUNT(*) FROM questions WHERE quizId = :quizId") suspend fun countQuestions(quizId: Long): Int
    @Query("SELECT COALESCE(MAX(position), 0) + 1 FROM questions WHERE quizId = :quizId") suspend fun nextQuestionPosition(quizId: Long): Int
}
