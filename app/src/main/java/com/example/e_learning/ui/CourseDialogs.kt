package com.example.e_learning.ui

import android.app.Activity
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.e_learning.R
import com.example.e_learning.data.CourseRepository
import com.example.e_learning.entities.CourseEntity

class CourseDialogs(
    private val activity: Activity,
    private val repository: CourseRepository,
    private val onChanged: () -> Unit
) {
    fun edit(course: CourseEntity? = null) {
        val form = LayoutInflater.from(activity).inflate(R.layout.dialog_course_form, null, false)
        val title = form.findViewById<EditText>(R.id.courseTitleInput)
        val language = form.findViewById<EditText>(R.id.courseLanguageInput)
        val level = form.findViewById<EditText>(R.id.courseLevelInput)
        val description = form.findViewById<EditText>(R.id.courseDescriptionInput)
        val lessonSection = form.findViewById<View>(R.id.lessonSection)
        val editLessonsButton = form.findViewById<TextView>(R.id.editLessonsButton)
        val isNewCourse = course == null

        title.setText(course?.title.orEmpty())
        language.setText(course?.language.orEmpty())
        level.setText(course?.level ?: "BEGINNER")
        description.setText(course?.description.orEmpty())
        if (isNewCourse) {
            editLessonsButton.visibility = View.GONE
        } else {
            lessonSection.visibility = View.GONE
            editLessonsButton.visibility = View.VISIBLE
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle(if (isNewCourse) "Add new course" else "Edit course")
            .setView(form)
            .setPositiveButton(if (isNewCourse) "Save course" else "Save changes", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            fun updateSaveButton() {
                val fieldsValid = title.text.isNotBlank() && language.text.isNotBlank() && description.text.isNotBlank()
                // New courses remain blocked until the lesson-management owner connects Add Lessons.
                saveButton.isEnabled = fieldsValid && !isNewCourse
            }
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = updateSaveButton()
                override fun afterTextChanged(s: Editable?) = Unit
            }
            listOf(title, language, description).forEach { it.addTextChangedListener(watcher) }
            updateSaveButton()

            saveButton.setOnClickListener {
                if (isNewCourse) {
                    Toast.makeText(activity, "Add at least one lesson before saving the course.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (title.text.isBlank() || language.text.isBlank() || description.text.isBlank()) {
                    Toast.makeText(activity, "Add the course title, language, and description.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val item = CourseEntity(
                    courseId = course?.courseId ?: 0,
                    title = title.text.toString().trim(),
                    description = description.text.toString().trim(),
                    language = language.text.toString().trim(),
                    level = level.text.toString().trim().ifBlank { "BEGINNER" }.uppercase()
                )
                saveButton.isEnabled = false
                repository.saveCourse(item) { result ->
                    result.onSuccess { dialog.dismiss(); onChanged() }
                        .onFailure {
                            updateSaveButton()
                            Toast.makeText(activity, "Couldn't save course. Please try again.", Toast.LENGTH_LONG).show()
                        }
                }
            }
        }
        dialog.show()
    }

    fun confirmDelete(course: CourseEntity) {
        AlertDialog.Builder(activity)
            .setTitle("Delete course?")
            .setMessage("${course.title} will be removed from the course library.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                repository.deleteCourse(course) { result ->
                    result.onSuccess { onChanged() }
                        .onFailure { Toast.makeText(activity, "Couldn't delete course. Please try again.", Toast.LENGTH_LONG).show() }
                }
            }
            .show()
    }

    fun showDetails(course: CourseEntity, enrolled: Boolean, studentId: Long?) {
        val details = LayoutInflater.from(activity).inflate(R.layout.dialog_course_details, null, false)
        details.findViewById<TextView>(R.id.courseDetailsMeta).text =
            "${course.language}  ·  ${course.level.lowercase().replaceFirstChar { it.uppercase() }}"
        details.findViewById<TextView>(R.id.courseDetailsDescription).text = course.description
        val enrollmentStatus = details.findViewById<TextView>(R.id.courseEnrollmentStatus)
        if (enrolled) {
            enrollmentStatus.visibility = View.VISIBLE
            enrollmentStatus.text = "You're enrolled in this course."
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle(course.title)
            .setView(details)
            .setPositiveButton(if (enrolled) "Enrolled" else "Enroll in course", null)
            .setNegativeButton("Close", null)
            .create()
        dialog.setOnShowListener {
            val enrollButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            if (enrolled || studentId == null) {
                enrollButton.isEnabled = false
            } else {
                enrollButton.setOnClickListener {
                    enrollButton.isEnabled = false
                    repository.enrollStudent(studentId, course.courseId) { result ->
                        result.onSuccess { added ->
                            Toast.makeText(
                                activity,
                                if (added) "You're enrolled in ${course.title}." else "You're already enrolled in this course.",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                            onChanged()
                        }.onFailure {
                            enrollButton.isEnabled = true
                            Toast.makeText(activity, "Couldn't enroll. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        dialog.show()
    }
}