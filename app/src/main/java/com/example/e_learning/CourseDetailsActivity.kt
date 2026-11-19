package com.example.e_learning

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.data.CourseRepository
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.CourseEntity
import com.example.e_learning.entities.LessonEntity
import com.example.e_learning.entities.UserEntity

class CourseDetailsActivity : AppCompatActivity() {
    private lateinit var repository: CourseRepository
    private lateinit var lessonList: LinearLayout
    private lateinit var noLessonsMessage: TextView
    private lateinit var enrollButton: TextView
    private var courseId: Long = INVALID_ID
    private var student: UserEntity? = null
    private var enrolled = false
    private var lessons: List<LessonEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_details)
        findViewById<TextView>(R.id.courseBackButton).setOnClickListener { finish() }
        lessonList = findViewById(R.id.lessonList)
        noLessonsMessage = findViewById(R.id.noLessonsMessage)
        enrollButton = findViewById(R.id.courseEnrollButton)
        repository = CourseRepository(applicationContext, AppDatabase.getInstance(this))
        courseId = intent.getLongExtra(EXTRA_COURSE_ID, INVALID_ID)
        if (courseId == INVALID_ID) { finish(); return }

        enrollButton.isEnabled = false
        enrollButton.setOnClickListener { enrollInCourse() }
        loadCourse(courseId)
        loadLessons(courseId)
        repository.loadCurrentStudent { result ->
            result.onSuccess { currentStudent ->
                student = currentStudent
                repository.isEnrolled(currentStudent.userId, courseId) { enrollmentResult ->
                    enrollmentResult.onSuccess { enrolledNow ->
                        enrolled = enrolledNow
                        renderEnrollmentState()
                        renderLessons()
                    }.onFailure { showLoadError() }
                }
            }.onFailure { showLoadError() }
        }
    }

    private fun loadCourse(id: Long) {
        repository.loadCourse(id) { result ->
            result.onSuccess { course ->
                if (course == null) {
                    Toast.makeText(this, "Course not found.", Toast.LENGTH_SHORT).show()
                    finish()
                } else bindCourse(course)
            }.onFailure { showLoadError(); finish() }
        }
    }

    private fun bindCourse(course: CourseEntity) {
        findViewById<TextView>(R.id.courseTitle).text = course.title
        findViewById<TextView>(R.id.courseMeta).text =
            "${course.language}  ·  ${course.level.lowercase().replaceFirstChar { it.uppercase() }}"
        findViewById<TextView>(R.id.courseDescription).text = course.description
    }

    private fun loadLessons(id: Long) {
        repository.loadLessonsForCourse(id) { result ->
            result.onSuccess { lessons = it; renderLessons() }
                .onFailure { Toast.makeText(this, "Couldn't load course lessons.", Toast.LENGTH_LONG).show() }
        }
    }

    private fun renderEnrollmentState() {
        enrollButton.text = if (enrolled) "✓  Enrolled" else "Enroll in course"
        enrollButton.isEnabled = !enrolled && student != null
        enrollButton.alpha = if (enrollButton.isEnabled) 1f else 0.72f
    }

    private fun enrollInCourse() {
        val currentStudent = student ?: return
        enrollButton.isEnabled = false
        repository.enrollStudent(currentStudent.userId, courseId) { result ->
            result.onSuccess {
                enrolled = true
                renderEnrollmentState()
                renderLessons()
                Toast.makeText(this, "You're enrolled. Lessons are now unlocked.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                enrollButton.isEnabled = true
                Toast.makeText(this, "Couldn't enroll. Please try again.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun renderLessons() {
        lessonList.removeAllViews()
        noLessonsMessage.visibility = if (lessons.isEmpty()) View.VISIBLE else View.GONE
        lessons.forEachIndexed { index, lesson ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_course_lesson, lessonList, false)
            row.findViewById<TextView>(R.id.lessonPosition).text = (index + 1).toString().padStart(2, '0')
            row.findViewById<TextView>(R.id.lessonItemTitle).text = lesson.title
            val lessonType = row.findViewById<TextView>(R.id.lessonItemType)
            if (enrolled) {
                lessonType.text = if (lesson.contentType.equals("VIDEO", true)) "Video lesson" else "Text lesson"
                row.setOnClickListener {
                    startActivity(Intent(this, LessonDetailsActivity::class.java)
                        .putExtra(LessonDetailsActivity.EXTRA_LESSON_ID, lesson.lessonId))
                }
            } else {
                lessonType.visibility = View.GONE
                row.isClickable = false
                row.isFocusable = false
                row.alpha = 0.76f
            }
            lessonList.addView(row)
        }
    }

    private fun showLoadError() {
        Toast.makeText(this, "Couldn't load course details.", Toast.LENGTH_LONG).show()
    }

    companion object {
        const val EXTRA_COURSE_ID = "extra_course_id"
        private const val INVALID_ID = -1L
    }
}


