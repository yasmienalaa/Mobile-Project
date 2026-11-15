package com.example.e_learning.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.room.withTransaction
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/** Loads the checked-in demo_data.json fixture into Room without duplicating existing rows. */
class DemoDataSeeder(private val context: Context, private val database: AppDatabase) {
    private val main = Handler(Looper.getMainLooper())

    fun seedIfNeeded(onComplete: (Result<Unit>) -> Unit) {
        Thread {
            val result = runCatching { runBlocking { seedDatabase() } }
            main.post { onComplete(result) }
        }.start()
    }

    private suspend fun seedDatabase() {
        val fixtureText = context.assets.open(FIXTURE_FILE).bufferedReader().use { it.readText() }
        val fixture = JSONObject(fixtureText)
        database.withTransaction {
            val users = ensureUsers(fixture.getJSONArray("users"))
            val courses = ensureCourses(fixture.getJSONArray("courses"))
            val lessons = ensureLessons(fixture.getJSONArray("courses"), courses)
            val quizzes = ensureQuizzes(fixture.getJSONArray("courses"), courses)
            val questions = ensureQuestions(fixture.getJSONArray("courses"), quizzes)
            ensureEnrollments(fixture.getJSONArray("enrollments"), users, courses)
            ensureProgress(fixture.getJSONArray("progress"), users, lessons)
            ensureQuizResults(fixture.getJSONArray("quizResults"), users, quizzes, questions)
        }
    }

    private suspend fun ensureUsers(records: JSONArray): Map<String, UserEntity> {
        val result = linkedMapOf<String, UserEntity>()
        for (index in 0 until records.length()) {
            val record = records.getJSONObject(index)
            val email = record.getString("email")
            val user = database.userDao().findByEmail(email) ?: run {
                val languages = record.getJSONArray("preferredLanguages").asStringList()
                    .joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"")
                val id = database.userDao().insert(
                    UserEntity(
                        fullName = record.getString("fullName"),
                        email = email,
                        passwordHash = sha256(record.getString("password")),
                        role = record.getString("role"),
                        level = record.getString("level"),
                        preferredLanguages = languages
                    )
                )
                database.userDao().findById(id) ?: error("Demo user was not saved: $email")
            }
            result[record.getString("key")] = user
        }
        return result
    }

    private suspend fun ensureCourses(records: JSONArray): Map<String, CourseEntity> {
        val result = linkedMapOf<String, CourseEntity>()
        for (index in 0 until records.length()) {
            val record = records.getJSONObject(index)
            val title = record.getString("title")
            val course = database.courseDao().getByTitle(title) ?: run {
                val id = database.courseDao().insert(
                    CourseEntity(
                        title = title,
                        description = record.getString("description"),
                        language = record.getString("language"),
                        level = record.getString("level")
                    )
                )
                database.courseDao().getById(id) ?: error("Demo course was not saved: $title")
            }
            result[record.getString("key")] = course
        }
        return result
    }

    private suspend fun ensureLessons(
        courseRecords: JSONArray,
        courses: Map<String, CourseEntity>
    ): Map<String, List<LessonEntity>> {
        val result = linkedMapOf<String, List<LessonEntity>>()
        for (courseIndex in 0 until courseRecords.length()) {
            val record = courseRecords.getJSONObject(courseIndex)
            val key = record.getString("key")
            val course = courses.getValue(key)
            val lessonRecords = record.getJSONArray("lessons")
            val existing = database.lessonDao().observeByCourse(course.courseId).first().associateBy { it.position }.toMutableMap()
            for (position in 0 until lessonRecords.length()) {
                if (existing.containsKey(position)) continue
                val lessonRecord = lessonRecords.getJSONObject(position)
                val videoUrl = if (lessonRecord.isNull("videoUrl")) null else lessonRecord.optString("videoUrl")
                val lesson = LessonEntity(
                    courseId = course.courseId,
                    title = lessonRecord.getString("title"),
                    contentType = lessonRecord.getString("contentType"),
                    textContent = lessonRecord.getString("content"),
                    videoUrl = videoUrl,
                    position = position
                )
                val id = database.lessonDao().insert(lesson)
                existing[position] = lesson.copy(lessonId = id)
            }
            result[key] = existing.values.sortedBy { it.position }
        }
        return result
    }

    private suspend fun ensureQuizzes(
        courseRecords: JSONArray,
        courses: Map<String, CourseEntity>
    ): Map<String, QuizEntity> {
        val result = linkedMapOf<String, QuizEntity>()
        for (index in 0 until courseRecords.length()) {
            val record = courseRecords.getJSONObject(index)
            val key = record.getString("key")
            val quizRecord = record.getJSONObject("quiz")
            val course = courses.getValue(key)
            val quiz = database.quizDao().observeByCourse(course.courseId).first()
                .firstOrNull { it.title == quizRecord.getString("title") }
                ?: run {
                    val id = database.quizDao().insertQuiz(
                        QuizEntity(
                            courseId = course.courseId,
                            title = quizRecord.getString("title"),
                            description = quizRecord.getString("description")
                        )
                    )
                    QuizEntity(id, course.courseId, quizRecord.getString("title"), quizRecord.getString("description"))
                }
            result[key] = quiz
        }
        return result
    }

    private suspend fun ensureQuestions(
        courseRecords: JSONArray,
        quizzes: Map<String, QuizEntity>
    ): Map<String, List<QuestionEntity>> {
        val result = linkedMapOf<String, List<QuestionEntity>>()
        for (courseIndex in 0 until courseRecords.length()) {
            val courseRecord = courseRecords.getJSONObject(courseIndex)
            val key = courseRecord.getString("key")
            val quiz = quizzes.getValue(key)
            val records = courseRecord.getJSONObject("quiz").getJSONArray("questions")
            val existing = database.quizDao().observeQuestions(quiz.quizId).first().associateBy { it.position }.toMutableMap()
            for (position in 0 until records.length()) {
                if (existing.containsKey(position)) continue
                val record = records.getJSONObject(position)
                val question = QuestionEntity(
                    quizId = quiz.quizId,
                    questionText = record.getString("questionText"),
                    optionA = record.getString("optionA"),
                    optionB = record.getString("optionB"),
                    optionC = record.getString("optionC"),
                    optionD = record.getString("optionD"),
                    correctOption = record.getString("correctOption"),
                    points = record.getInt("points"),
                    position = position
                )
                val id = database.quizDao().insertQuestion(question)
                existing[position] = question.copy(questionId = id)
            }
            result[key] = existing.values.sortedBy { it.position }
        }
        return result
    }

    private suspend fun ensureEnrollments(
        records: JSONArray,
        users: Map<String, UserEntity>,
        courses: Map<String, CourseEntity>
    ) {
        for (index in 0 until records.length()) {
            val record = records.getJSONObject(index)
            val student = users.getValue(record.getString("student"))
            val course = courses.getValue(record.getString("course"))
            val enrolled = database.enrollmentDao().observeByUser(student.userId).first()
                .any { it.courseId == course.courseId }
            if (!enrolled) database.enrollmentDao().enroll(EnrollmentEntity(student.userId, course.courseId))
        }
    }

    private suspend fun ensureProgress(
        records: JSONArray,
        users: Map<String, UserEntity>,
        lessons: Map<String, List<LessonEntity>>
    ) {
        for (index in 0 until records.length()) {
            val record = records.getJSONObject(index)
            val student = users.getValue(record.getString("student"))
            val lesson = lessons.getValue(record.getString("course")).getOrNull(record.getInt("lessonPosition")) ?: continue
            val completed = record.getBoolean("completed")
            val existing = database.progressDao().observeByUser(student.userId).first()
                .firstOrNull { it.lessonId == lesson.lessonId }
            if (existing == null) {
                database.progressDao().save(
                    LessonProgressEntity(
                        userId = student.userId,
                        lessonId = lesson.lessonId,
                        completed = completed,
                        completedAt = if (completed) System.currentTimeMillis() else null
                    )
                )
            }
        }
    }

    private suspend fun ensureQuizResults(
        records: JSONArray,
        users: Map<String, UserEntity>,
        quizzes: Map<String, QuizEntity>,
        questions: Map<String, List<QuestionEntity>>
    ) {
        for (index in 0 until records.length()) {
            val record = records.getJSONObject(index)
            val student = users.getValue(record.getString("student"))
            val key = record.getString("course")
            val quiz = quizzes.getValue(key)
            val existing = database.quizResultDao().observeUserResults(student.userId).first()
                .any { it.quizId == quiz.quizId }
            if (existing) continue
            val answerRecords = record.getJSONArray("answers")
            val answerSnapshots = questions.getValue(key).mapIndexed { position, question ->
                val selected = answerRecords.optString(position).takeIf { it in listOf("A", "B", "C", "D") }
                QuizAnswerEntity(
                    resultId = 0,
                    questionId = question.questionId,
                    questionText = question.questionText,
                    optionA = question.optionA,
                    optionB = question.optionB,
                    optionC = question.optionC,
                    optionD = question.optionD,
                    selectedOption = selected,
                    correctOption = question.correctOption,
                    pointsAwarded = if (selected == question.correctOption) question.points else 0,
                    maxPoints = question.points,
                    position = position
                )
            }
            database.quizResultDao().saveAttempt(
                QuizResultEntity(
                    userId = student.userId,
                    quizId = quiz.quizId,
                    score = record.getInt("score"),
                    maxScore = record.getInt("maxScore")
                ),
                answerSnapshots
            )
        }
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private fun JSONArray.asStringList(): List<String> =
        (0 until length()).mapNotNull { index -> optString(index).takeIf(String::isNotBlank) }

    companion object {
        private const val FIXTURE_FILE = "demo_data.json"
    }
}