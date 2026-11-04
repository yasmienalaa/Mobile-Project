package com.example.myapplication.dao

import androidx.room.*
import com.example.myapplication.entities.*
import kotlinx.coroutines.flow.Flow

@Dao interface QuizDao {
    @Insert suspend fun insertQuiz(quiz: QuizEntity): Long
    @Update suspend fun updateQuiz(quiz: QuizEntity)
    @Delete suspend fun deleteQuiz(quiz: QuizEntity)
    @Query("SELECT * FROM quizzes WHERE courseId = :courseId") fun observeByCourse(courseId: Long): Flow<List<QuizEntity>>
    @Insert suspend fun insertQuestion(question: QuestionEntity): Long
    @Update suspend fun updateQuestion(question: QuestionEntity)
    @Delete suspend fun deleteQuestion(question: QuestionEntity)
    @Query("SELECT * FROM questions WHERE quizId = :quizId ORDER BY position") fun observeQuestions(quizId: Long): Flow<List<QuestionEntity>>
}
