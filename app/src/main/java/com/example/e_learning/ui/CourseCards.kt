package com.example.e_learning.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.e_learning.R
import com.example.e_learning.entities.CourseEntity

object CourseCards {
    fun student(context: Context, parent: ViewGroup, course: CourseEntity, enrolled: Boolean, onClick: () -> Unit): View {
        val card = LayoutInflater.from(context).inflate(R.layout.item_course_student, parent, false)
        card.findViewById<TextView>(R.id.courseIcon).text = when (course.language.lowercase()) {
            "kotlin" -> "K"
            "python" -> "Py"
            "java" -> "J"
            else -> "</>"
        }
        card.findViewById<TextView>(R.id.courseTitle).text = course.title
        card.findViewById<TextView>(R.id.courseMeta).text = "${course.language}  ·  ${course.level.prettyLevel()}"
        card.findViewById<TextView>(R.id.courseDescription).text = course.description
        card.findViewById<TextView>(R.id.enrollmentStatus).visibility = if (enrolled) View.VISIBLE else View.GONE
        card.contentDescription = "${course.title}, ${course.language}, ${if (enrolled) "enrolled" else "not enrolled"}"
        card.setOnClickListener { onClick() }
        return card
    }

    fun admin(
        context: Context,
        parent: ViewGroup,
        course: CourseEntity,
        lessonCount: Int = 0,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        onQuizzes: () -> Unit
    ): View {
        val card = LayoutInflater.from(context).inflate(R.layout.item_course_admin, parent, false)
        card.findViewById<TextView>(R.id.courseTitle).text = course.title
        val lessonsText = "$lessonCount ${if (lessonCount == 1) "Lesson" else "Lessons"}"
        card.findViewById<TextView>(R.id.courseMeta).text = "${course.language}  ·  ${course.level.prettyLevel()}  ·  $lessonsText"
        card.findViewById<TextView>(R.id.courseDescription).text = course.description
        card.findViewById<TextView>(R.id.editCourseButton).setOnClickListener { onEdit() }
        card.findViewById<TextView>(R.id.deleteCourseButton).setOnClickListener { onDelete() }
        card.findViewById<TextView>(R.id.manageQuizzesButton).setOnClickListener { onQuizzes() }
        return card
    }

    private fun String.prettyLevel() = lowercase().replaceFirstChar { it.uppercase() }
}


