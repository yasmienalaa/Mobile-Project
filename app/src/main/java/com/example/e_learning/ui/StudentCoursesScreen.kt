package com.example.e_learning.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.example.e_learning.R
import com.example.e_learning.entities.CourseEntity

class StudentCoursesScreen(
    private val context: Context,
    @LayoutRes layoutRes: Int,
    private val onCourseSelected: (CourseEntity, Boolean) -> Unit
) {
    private val root = LayoutInflater.from(context).inflate(layoutRes, null, false)
    private val languageFilters = root.findViewById<LinearLayout>(R.id.languageFilters)
    private val levelFilters = root.findViewById<LinearLayout>(R.id.levelFilters)
    private val courseList = root.findViewById<LinearLayout>(R.id.courseList)
    private val search = root.findViewById<EditText>(R.id.courseSearch)
    private var courses: List<CourseEntity> = emptyList()
    private var enrolledCourseIds: Set<Long> = emptySet()
    private var language = "All"
    private var level = "All"

    fun createView(): View {
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = renderCourses()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        renderFilters()
        return root
    }

    fun render(newCourses: List<CourseEntity>, enrolledIds: Set<Long>) {
        courses = newCourses
        enrolledCourseIds = enrolledIds
        if (language != "All" && courses.none { it.language.equals(language, true) }) language = "All"
        renderFilters()
        renderCourses()
    }

    private fun renderFilters() {
        val languages = listOf("All") + courses.map { it.language }.distinctBy { it.lowercase() }.sorted()
        renderFilterRow(languageFilters, languages, language) { selectedLanguage ->
            language = selectedLanguage
            renderFilters()
            renderCourses()
        }
        val levels = listOf("All", "Beginner", "Intermediate", "Advanced")
        renderFilterRow(levelFilters, levels, level) { selectedLevel ->
            level = selectedLevel
            renderFilters()
            renderCourses()
        }
    }

    private fun renderFilterRow(
        container: LinearLayout,
        options: List<String>,
        selectedValue: String,
        onSelected: (String) -> Unit
    ) {
        container.removeAllViews()
        options.forEach { option ->
            val chip = LayoutInflater.from(context)
                .inflate(R.layout.item_language_filter, container, false) as TextView
            val selected = selectedValue.equals(option, true)
            chip.text = option
            chip.setTextColor(context.getColor(if (selected) android.R.color.white else R.color.sage_text))
            chip.setBackgroundResource(if (selected) R.drawable.bg_filter_selected else R.drawable.bg_filter_unselected)
            chip.setOnClickListener { onSelected(option) }
            container.addView(chip)
        }
    }

    private fun renderCourses() {
        courseList.removeAllViews()
        val query = search.text.toString().trim()
        val visible = courses.filter { course ->
            (language == "All" || course.language.equals(language, true)) &&
                (level == "All" || course.level.equals(level, true)) &&
                (query.isBlank() || course.title.contains(query, true) || course.language.contains(query, true) || course.description.contains(query, true))
        }
        if (visible.isEmpty()) {
            val empty = LayoutInflater.from(context)
                .inflate(R.layout.view_course_empty, courseList, false)
            val noCourses = courses.isEmpty()
            empty.findViewById<TextView>(R.id.emptyTitle).text =
                if (noCourses) "A little room for new ideas" else "No courses found"
            empty.findViewById<TextView>(R.id.emptyDescription).text =
                if (noCourses) "Your course library is waiting for its first course." else "Try another search or filter."
            courseList.addView(empty)
            return
        }
        visible.forEach { course ->
            val enrolled = course.courseId in enrolledCourseIds
            courseList.addView(CourseCards.student(context, courseList, course, enrolled) {
                onCourseSelected(course, enrolled)
            })
        }
    }
}
