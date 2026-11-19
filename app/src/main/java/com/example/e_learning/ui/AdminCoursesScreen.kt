package com.example.e_learning.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.e_learning.R
import com.example.e_learning.data.AdminStats
import com.example.e_learning.data.CourseWithLessonCount
import com.example.e_learning.entities.CourseEntity

class AdminCoursesScreen(
    private val context: Context,
    private val onAddCourse: () -> Unit,
    private val onEditCourse: (CourseEntity) -> Unit,
    private val onDeleteCourse: (CourseEntity) -> Unit,
    private val onViewStudents: (() -> Unit)? = null,
    private val onManageLessons: (() -> Unit)? = null,
    private val onManageQuizzes: ((CourseEntity) -> Unit)? = null
) {
    private val root = LayoutInflater.from(context).inflate(R.layout.screen_admin_courses, null, false)
    private val courseList = root.findViewById<LinearLayout>(R.id.courseList)
    private val statCoursesCount = root.findViewById<TextView>(R.id.statCoursesCount)
    private val statLessonsCount = root.findViewById<TextView>(R.id.statLessonsCount)
    private val statStudentsCount = root.findViewById<TextView>(R.id.statStudentsCount)

    fun createView(): View {
        root.findViewById<TextView>(R.id.addCourseButton).setOnClickListener { onAddCourse() }
        root.findViewById<TextView>(R.id.viewStudentsShortcut)?.setOnClickListener { onViewStudents?.invoke() }
        root.findViewById<TextView>(R.id.manageLessonsShortcut)?.setOnClickListener { onManageLessons?.invoke() }
        root.findViewById<TextView>(R.id.manageQuizzesShortcut)?.setOnClickListener { onManageLessons?.invoke() }
        return root
    }

    fun renderStats(stats: AdminStats) {
        statCoursesCount?.text = stats.totalCourses.toString()
        statLessonsCount?.text = stats.totalLessons.toString()
        statStudentsCount?.text = stats.totalStudents.toString()
    }

    fun render(coursesWithCount: List<CourseWithLessonCount>) {
        courseList.removeAllViews()
        if (coursesWithCount.isEmpty()) {
            val empty = LayoutInflater.from(context)
                .inflate(R.layout.view_course_empty, courseList, false)
            empty.findViewById<TextView>(R.id.emptyTitle).text = "A little room for new ideas"
            empty.findViewById<TextView>(R.id.emptyDescription).text = "Add your first course to start the library."
            courseList.addView(empty)
            return
        }
        coursesWithCount.forEach { item ->
            courseList.addView(CourseCards.admin(context, courseList, item.course, lessonCount = item.lessonCount,
                onEdit = { onEditCourse(item.course) },
                onDelete = { onDeleteCourse(item.course) },
                onQuizzes = { onManageQuizzes?.invoke(item.course) }
            ))
        }
    }
}
