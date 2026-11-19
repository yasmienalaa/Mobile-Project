package com.example.e_learning.ui
import android.app.Activity
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.e_learning.R
import com.example.e_learning.data.CourseRepository
import com.example.e_learning.entities.CourseEntity
import com.example.e_learning.entities.LessonEntity


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

        val addLessonButton = form.findViewById<TextView>(R.id.addLessonButton)
        val lessonList = form.findViewById<LinearLayout>(R.id.lessonDraftList)
        val lessonCount = form.findViewById<TextView>(R.id.lessonCountLabel)
        val noLessonsLabel = form.findViewById<TextView>(R.id.noLessonsLabel)
        val lessons = mutableListOf<LessonEntity>()


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
                // new courses blocked till the lesson owner adds one
                saveButton.isEnabled = fieldsValid && (!isNewCourse || lessons.isNotEmpty())
            }
            fun renderLessons() {
                lessonList.removeAllViews()
                lessonCount.text = "Added Lessons (${lessons.size})"
                noLessonsLabel.visibility = if (lessons.isEmpty()) View.VISIBLE else View.GONE
                lessons.forEachIndexed { index, lesson ->
                    val row = LayoutInflater.from(activity).inflate(R.layout.draft_lesson, lessonList, false)
                    row.findViewById<TextView>(R.id.draftLessonTitle).text = "${index + 1}. ${lesson.title}"
                    row.findViewById<TextView>(R.id.draftLessonType).text = lesson.contentType

                    row.findViewById<TextView>(R.id.editDraftLesson).setOnClickListener {
                        showLessonEditor(lesson) { updatedLesson ->
                            val updated = updatedLesson.copy(courseId = lesson.courseId, position = lesson.position)
                            if (isNewCourse) {
                                lessons[index] = updated
                                renderLessons()
                            } else {
                                repository.updateLesson(updated) { result ->
                                    result.onSuccess {
                                        lessons[index] = updated
                                        renderLessons()
                                        Toast.makeText(activity, "Lesson updated successfully.", Toast.LENGTH_SHORT).show()
                                    }.onFailure { Toast.makeText(activity, "Couldn't update lesson.", Toast.LENGTH_LONG).show() }
                                }
                            }
                        }
                    }

                    row.findViewById<TextView>(R.id.deleteDraftLesson).setOnClickListener {
                        AlertDialog.Builder(activity)
                            .setTitle("Delete lesson?")
                            .setMessage("Are you sure you want to delete ${lesson.title}?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete") { _, _ ->
                                if (isNewCourse) {
                                    lessons.removeAt(index)
                                    renderLessons()
                                    updateSaveButton()
                                } else {
                                    repository.deleteLesson(lesson) { result ->
                                        result.onSuccess {
                                            lessons.removeAt(index)
                                            renderLessons()
                                            Toast.makeText(activity, "Lesson deleted successfully.", Toast.LENGTH_SHORT).show()
                                        }.onFailure { Toast.makeText(activity, "Couldn't delete lesson.", Toast.LENGTH_LONG).show() }
                                    }
                                }
                            }.show()
                    }
                    lessonList.addView(row)
                }
            }

            editLessonsButton.setOnClickListener {
                if (lessonSection.visibility == View.VISIBLE) {
                    lessonSection.visibility = View.GONE
                    editLessonsButton.text = "Edit Lessons"
                } else {
                    repository.loadLessonsForCourse(course!!.courseId) { result ->
                        result.onSuccess { loadedLessons ->
                            lessons.clear()
                            lessons.addAll(loadedLessons)
                            renderLessons()
                            lessonSection.visibility = View.VISIBLE
                            editLessonsButton.text = "Hide Lessons"
                        }.onFailure { Toast.makeText(activity, "Couldn't load course lessons.", Toast.LENGTH_LONG).show() }
                    }
                }
            }
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = updateSaveButton()
                override fun afterTextChanged(s: Editable?) = Unit
            }
            listOf(title, language, description).forEach { it.addTextChangedListener(watcher) }

            addLessonButton.setOnClickListener {
                showLessonEditor(null) { newLesson ->
                    if (isNewCourse) {
                        lessons.add(newLesson.copy(position = lessons.size + 1))
                        renderLessons()
                        updateSaveButton()
                    } else {
                        val nextPosition =
                            (lessons.maxOfOrNull { it.position } ?: 0) + 1
                        val lessonToSave = newLesson.copy(courseId = course!!.courseId, position = nextPosition)
                        repository.insertLesson(lessonToSave) { result ->
                            result.onSuccess { savedLesson ->
                                lessons.add(savedLesson)
                                renderLessons()
                                Toast.makeText(activity, "Lesson added successfully.", Toast.LENGTH_SHORT).show()
                            }.onFailure { Toast.makeText(activity, "Couldn't add lesson.", Toast.LENGTH_LONG).show() }
                        }
                    }
                }
            }
            renderLessons()
            updateSaveButton()

            saveButton.setOnClickListener {
                if (isNewCourse && lessons.isEmpty()) {
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
                if (isNewCourse) {

                    repository.createCourseWithLessons(item, lessons) { result ->
                        result.onSuccess { dialog.dismiss()
                            onChanged() }.onFailure {
                            updateSaveButton()
                            Toast.makeText(activity, "Couldn't save course and lessons.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else {
                    repository.updateCourse(item) { result ->
                        result.onSuccess { dialog.dismiss(); onChanged() }
                            .onFailure {
                                updateSaveButton()
                                Toast.makeText(
                                    activity,
                                    "Couldn't save course. Please try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            }
        }
        dialog.show()
    }


    private fun showLessonEditor(existingLesson: LessonEntity?, onSave: (LessonEntity) -> Unit) {

        val form = LayoutInflater.from(activity).inflate(R.layout.lesson_form, null, false)
        val title = form.findViewById<EditText>(R.id.lessonTitleInput)
        val contentType = form.findViewById<Spinner>(R.id.lessonContentType)
        val textContent = form.findViewById<EditText>(R.id.lessonTextContent)
        val videoUrl = form.findViewById<EditText>(R.id.lessonVideoUrl)


        val options = listOf("TEXT", "VIDEO")
        val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        contentType.adapter = adapter

        title.setText(existingLesson?.title.orEmpty())
        textContent.setText(existingLesson?.textContent.orEmpty())
        videoUrl.setText(existingLesson?.videoUrl.orEmpty())
        val selectedPosition = if (existingLesson?.contentType.equals("VIDEO", true)) 1 else 0
        contentType.setSelection(selectedPosition)

        fun updateContentFields(type: String) {
            if (type == "VIDEO") {
                textContent.visibility = View.GONE
                videoUrl.visibility = View.VISIBLE
            } else {
                textContent.visibility = View.VISIBLE
                videoUrl.visibility = View.GONE
            }
        }
        contentType.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    updateContentFields(options[position])
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
            }
        updateContentFields(options[selectedPosition])
        val dialog = AlertDialog.Builder(activity)
            .setTitle(
                if (existingLesson == null) "Add Lesson"
                else "Edit Lesson"
            )
            .setView(form)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val lessonTitle = title.text.toString().trim()
                    val type = contentType.selectedItem.toString()
                    val content = textContent.text.toString().trim()
                    val url = videoUrl.text.toString().trim()
                    if (lessonTitle.isBlank()) {
                        title.error = "Lesson title is required"
                        return@setOnClickListener
                    }
                    if (type == "TEXT" && content.isBlank()) {
                        textContent.error = "Lesson content is required"
                        return@setOnClickListener
                    }
                    if (type == "VIDEO" && url.isBlank()) {
                        videoUrl.error = "Video URL is required"
                        return@setOnClickListener
                    }
                    val lesson = LessonEntity(
                        lessonId = existingLesson?.lessonId ?: 0L,
                        courseId = existingLesson?.courseId ?: 0L,
                        title = lessonTitle,
                        contentType = type,
                        textContent = if (type == "TEXT") content else null,
                        videoUrl = if (type == "VIDEO") url else null,
                        position = existingLesson?.position ?: 0
                    )
                    onSave(lesson)
                    dialog.dismiss()
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
}


