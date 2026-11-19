package com.example.e_learning

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.data.CourseRepository
import com.example.e_learning.data.LessonQuizItem
import com.example.e_learning.data.StudentQuizRepository
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.LessonEntity
import com.example.e_learning.quiz.QuizActivity

class LessonDetailsActivity : AppCompatActivity() {
    private lateinit var repository: CourseRepository
    private lateinit var quizRepository: StudentQuizRepository

    private var studentId: Long = -1L
    private var lessons: List<LessonEntity> = emptyList()
    private var currentIndex = 0
    private var currentLesson: LessonEntity? = null
    private var updatingCheckbox = false
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var completedCheckbox: CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson_details)
        repository = CourseRepository(applicationContext, AppDatabase.getInstance(this))
        quizRepository = StudentQuizRepository(AppDatabase.getInstance(this))

        findViewById<TextView>(R.id.lessonBackButton)
            .setOnClickListener {
                finish()
            }
        previousButton = findViewById(R.id.previousLessonButton)
        nextButton = findViewById(R.id.nextLessonButton)
        completedCheckbox = findViewById(R.id.lessonCompletedCheckbox)
        previousButton.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                showCurrentLesson()
            }
        }
        nextButton.setOnClickListener {
            if (currentIndex < lessons.size - 1) {
                currentIndex++
                showCurrentLesson()
            }
        }
        completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (updatingCheckbox) return@setOnCheckedChangeListener
            val lesson = currentLesson ?: return@setOnCheckedChangeListener
            completedCheckbox.isEnabled = false
            repository.setLessonCompleted(studentId, lesson.lessonId, isChecked) { result ->
                if (currentLesson?.lessonId != lesson.lessonId) {
                    return@setLessonCompleted
                }
                result.onSuccess {
                    completedCheckbox.isEnabled = true
                    val message = if (isChecked) {
                        "Lesson marked as completed."
                    } else {
                        "Lesson marked as incomplete."
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
                result.onFailure {
                    updatingCheckbox = true
                    completedCheckbox.isChecked = !isChecked
                    updatingCheckbox = false
                    completedCheckbox.isEnabled = true
                    Toast.makeText(this, "Couldn't save lesson progress.", Toast.LENGTH_LONG).show()
                }
            }

        }
        val lessonId = intent.getLongExtra(EXTRA_LESSON_ID, INVALID_ID)
        if (lessonId == INVALID_ID) {
            finish()
            return
        }
        loadStudentAndLesson(lessonId)
    }
    private fun loadStudentAndLesson(lessonId: Long) {
        repository.loadCurrentStudent { result ->
            result.onSuccess { student ->
                studentId = student.userId
                repository.loadLesson(lessonId) { lessonResult ->
                    lessonResult.onSuccess { lesson ->
                        if (lesson == null) {
                            Toast.makeText(this, "Lesson not found.", Toast.LENGTH_SHORT).show()
                            finish()
                            return@onSuccess
                        }
                        loadCourseLessons(lesson)
                    }
                    lessonResult.onFailure {
                        Toast.makeText(this, "Couldn't load lesson details.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
            result.onFailure {
                Toast.makeText(this, "Couldn't identify the signed-in student.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
    private fun loadCourseLessons(selectedLesson: LessonEntity) {
        repository.loadLessonsForCourse(selectedLesson.courseId) { result ->
            result.onSuccess { loadedLessons ->
                lessons = loadedLessons
                currentIndex = lessons.indexOfFirst {
                    it.lessonId == selectedLesson.lessonId
                }
                if (currentIndex == -1) {
                    Toast.makeText(this, "Lesson doesn't belong to this course.", Toast.LENGTH_LONG).show()
                    finish()
                    return@onSuccess
                }
                showCurrentLesson()
            }
            result.onFailure {
                Toast.makeText(this, "Couldn't load course lessons.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showCurrentLesson() {
        if (lessons.isEmpty()) return
        val lesson = lessons[currentIndex]
        currentLesson = lesson
        loadQuizzes(lesson)

        findViewById<TextView>(R.id.lessonTitle).text = lesson.title
        findViewById<TextView>(R.id.lessonType).text =
            if (lesson.contentType.equals("VIDEO", true)) {
                "Video lesson" }
            else { "Text lesson" }
        val contentView = findViewById<TextView>(R.id.lessonContent)
        val videoUrlView = findViewById<TextView>(R.id.lessonVideoUrl)
        val emptyMessage = findViewById<TextView>(R.id.lessonEmptyMessage)
        contentView.text = ""
        videoUrlView.text = ""
        contentView.visibility = View.GONE
        videoUrlView.visibility = View.GONE
        emptyMessage.visibility = View.GONE
        lesson.textContent?.takeIf { it.isNotBlank() }?.let { content ->
            contentView.text = content
            contentView.visibility = View.VISIBLE
        }
        lesson.videoUrl?.takeIf { it.isNotBlank() }?.let { url ->
            videoUrlView.text = url
            videoUrlView.visibility = View.VISIBLE
        }
        if (contentView.visibility != View.VISIBLE && videoUrlView.visibility != View.VISIBLE) {
            emptyMessage.visibility = View.VISIBLE
        }
        previousButton.isEnabled = currentIndex > 0
        nextButton.isEnabled = currentIndex < lessons.size - 1
        updatingCheckbox = true
        completedCheckbox.isChecked = false
        updatingCheckbox = false
        completedCheckbox.isEnabled = false

        repository.recordLessonOpened(studentId, lesson.lessonId) { result ->
            if (currentLesson?.lessonId != lesson.lessonId) {
                return@recordLessonOpened
            }
            result.onSuccess { progress ->
                updatingCheckbox = true
                completedCheckbox.isChecked = progress.completed
                updatingCheckbox = false
                completedCheckbox.isEnabled = true
            }
            result.onFailure {
                completedCheckbox.isEnabled = false
                Toast.makeText(this, "Couldn't load lesson progress.", Toast.LENGTH_LONG).show()
           }
        }

    }

    override fun onResume() {
        super.onResume()
        // Coming back from a quiz: refresh the "best score" of the lesson on screen.
        currentLesson?.let { loadQuizzes(it) }
    }

    private fun loadQuizzes(lesson: LessonEntity) {
        findViewById<LinearLayout>(R.id.lessonQuizList).removeAllViews()
        findViewById<View>(R.id.noQuizMessage).visibility = View.GONE
        quizRepository.loadLessonQuizzes(lesson.lessonId, studentId) { result ->
            if (currentLesson?.lessonId != lesson.lessonId) return@loadLessonQuizzes // student already moved to another lesson
            result.onSuccess { renderQuizzes(it) }
                .onFailure { Toast.makeText(this, "Couldn't load quizzes.", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun renderQuizzes(items: List<LessonQuizItem>) {
        val list = findViewById<LinearLayout>(R.id.lessonQuizList)
        list.removeAllViews()
        findViewById<View>(R.id.noQuizMessage).visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        items.forEach { item ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_lesson_quiz, list, false)
            row.findViewById<TextView>(R.id.quizItemTitle).text = item.quiz.title
            val n = item.questionCount
            val meta = row.findViewById<TextView>(R.id.quizItemMeta)
            if (n == 0) {
                meta.text = "Not ready yet. No questions added"
                row.isClickable = false
                row.isFocusable = false
                row.alpha = 0.76f
                row.findViewById<View>(R.id.quizItemChevron).visibility = View.GONE
            } else {
                val best = item.best?.takeIf { it.maxScore > 0 }
                meta.text = "$n ${if (n == 1) "question" else "questions"}" +
                    (best?.let { "  ·  Best: ${it.score}/${it.maxScore} (${it.score * 100 / it.maxScore}%)" } ?: "")
                row.setOnClickListener {
                    startActivity(Intent(this, QuizActivity::class.java)
                        .putExtra(QuizActivity.EXTRA_QUIZ_ID, item.quiz.quizId))
                }
            }
            list.addView(row)
        }

    }

    companion object {
        const val EXTRA_LESSON_ID = "extra_lesson_id"
        private const val INVALID_ID = -1L
    }
}