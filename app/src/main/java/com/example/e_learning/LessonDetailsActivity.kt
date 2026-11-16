package com.example.e_learning

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.data.CourseRepository
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.LessonEntity

class LessonDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson_details)
        findViewById<TextView>(R.id.lessonBackButton).setOnClickListener { finish() }

        val lessonId = intent.getLongExtra(EXTRA_LESSON_ID, INVALID_ID)
        if (lessonId == INVALID_ID) {
            finish()
            return
        }
        val repository = CourseRepository(applicationContext, AppDatabase.getInstance(this))
        repository.loadLesson(lessonId) { result ->
            result.onSuccess { lesson ->
                if (lesson == null) {
                    Toast.makeText(this, "Lesson not found.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    showLesson(lesson)
                }
            }.onFailure {
                Toast.makeText(this, "Couldn't load lesson details.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showLesson(lesson: LessonEntity) {
        findViewById<TextView>(R.id.lessonTitle).text = lesson.title
        findViewById<TextView>(R.id.lessonType).text =
            if (lesson.contentType.equals("VIDEO", true)) "Video lesson" else "Text lesson"
        val contentView = findViewById<TextView>(R.id.lessonContent)
        val videoUrlView = findViewById<TextView>(R.id.lessonVideoUrl)
        val emptyMessage = findViewById<TextView>(R.id.lessonEmptyMessage)
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
    }

    companion object {
        const val EXTRA_LESSON_ID = "extra_lesson_id"
        private const val INVALID_ID = -1L
    }
}