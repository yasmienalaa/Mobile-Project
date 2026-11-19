package com.example.e_learning

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.data.CourseRepository
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.LessonEntity

class LessonDetailsActivity : AppCompatActivity() {
    private lateinit var repository: CourseRepository
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

    companion object {
        const val EXTRA_LESSON_ID = "extra_lesson_id"
        private const val INVALID_ID = -1L
    }
}