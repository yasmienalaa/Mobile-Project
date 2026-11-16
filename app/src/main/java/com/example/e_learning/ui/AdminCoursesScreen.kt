package com.example.e_learning.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.e_learning.R
import com.example.e_learning.entities.CourseEntity

class AdminCoursesScreen(
    private val context: Context,
    private val onAddCourse: () -> Unit,
    private val onEditCourse: (CourseEntity) -> Unit,
    private val onDeleteCourse: (CourseEntity) -> Unit
) {
    private val root = LayoutInflater.from(context).inflate(R.layout.screen_admin_courses, null, false)
    private val courseList = root.findViewById<LinearLayout>(R.id.courseList)

    fun createView(): View {
        root.findViewById<TextView>(R.id.addCourseButton).setOnClickListener { onAddCourse() }
        return root
    }

    fun render(courses: List<CourseEntity>) {
        courseList.removeAllViews()
        if (courses.isEmpty()) {
            val empty = LayoutInflater.from(context)
                .inflate(R.layout.view_course_empty, courseList, false)
            empty.findViewById<TextView>(R.id.emptyTitle).text = "A little room for new ideas"
            empty.findViewById<TextView>(R.id.emptyDescription).text = "Add your first course to start the library."
            courseList.addView(empty)
            return
        }
        courses.forEach { course ->
            courseList.addView(CourseCards.admin(context, courseList, course,
                onEdit = { onEditCourse(course) },
                onDelete = { onDeleteCourse(course) }))
        }
    }
}
