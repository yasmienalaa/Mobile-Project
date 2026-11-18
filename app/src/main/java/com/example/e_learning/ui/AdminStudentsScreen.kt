package com.example.e_learning.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.e_learning.R
import com.example.e_learning.data.StudentSummary

class AdminStudentsScreen(
    private val context: Context,
    private val onBack: () -> Unit
) {
    private val root = LayoutInflater.from(context).inflate(R.layout.screen_admin_students, null, false)
    private val studentsList = root.findViewById<LinearLayout>(R.id.studentsList)

    fun createView(): View {
        root.findViewById<TextView>(R.id.backToAdminButton).setOnClickListener { onBack() }
        return root
    }

    fun render(students: List<StudentSummary>) {
        studentsList.removeAllViews()
        if (students.isEmpty()) {
            val empty = LayoutInflater.from(context)
                .inflate(R.layout.view_course_empty, studentsList, false)
            empty.findViewById<TextView>(R.id.emptyTitle).text = "No Students Found"
            empty.findViewById<TextView>(R.id.emptyDescription).text = "There are currently no registered students."
            studentsList.addView(empty)
            return
        }
        students.forEach { student ->
            val itemView = LayoutInflater.from(context).inflate(R.layout.item_admin_student, studentsList, false)
            itemView.findViewById<TextView>(R.id.studentName).text = student.fullName
            itemView.findViewById<TextView>(R.id.studentEmail).text = student.email
            itemView.findViewById<TextView>(R.id.studentLevel).text =
                "Level: ${student.level.lowercase().replaceFirstChar { it.uppercase() }}"
            itemView.findViewById<TextView>(R.id.studentCoursesCount).text =
                "${student.enrolledCoursesCount} Courses Enrolled"
            studentsList.addView(itemView)
        }
    }
}
