package com.example.e_learning

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.data.CourseRepository
import com.example.e_learning.data.DemoDataSeeder
import com.example.e_learning.entities.CourseEntity
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.UserEntity
import com.example.e_learning.ui.AdminCoursesScreen
import com.example.e_learning.ui.CourseDialogs
import com.example.e_learning.ui.StudentCoursesScreen

class HomeActivity : AppCompatActivity() {
    private lateinit var repository: CourseRepository
    private lateinit var dialogs: CourseDialogs
    private lateinit var database: AppDatabase
    private var isAdmin = false
    private var selectedTab = HOME_TAB
    private var currentStudent: UserEntity? = null
    private var studentScreen: StudentCoursesScreen? = null
    private var adminScreen: AdminCoursesScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.sage_background)
        window.navigationBarColor = getColor(R.color.sage_background)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        database = AppDatabase.getInstance(this)
        repository = CourseRepository(applicationContext, database)
        dialogs = CourseDialogs(this, repository, ::refreshCourses)
        showCourses()
        DemoDataSeeder(applicationContext, database).seedIfNeeded { result ->
            result.onSuccess {
                repository.loadCurrentStudent { studentResult ->
                    studentResult.onSuccess { student ->
                        currentStudent = student
                        refreshCourses()
                    }.onFailure { showLoadError() }
                }
            }.onFailure {
                showLoadError()
                repository.loadCurrentStudent { studentResult ->
                    studentResult.onSuccess { currentStudent = it; refreshCourses() }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized && currentStudent != null) refreshCourses()
    }

    private fun showCourses() {
        setContentView(R.layout.activity_home)
        val roleToggle = findViewById<TextView>(R.id.roleToggle)
        val screenContainer = findViewById<FrameLayout>(R.id.screenContainer)
        roleToggle.text = if (isAdmin) "Admin mode" else "Student mode"
        roleToggle.setOnClickListener {
            isAdmin = !isAdmin
            selectedTab = HOME_TAB
            showCourses()
            refreshCourses()
        }
        bindNavigation()
        findViewById<LinearLayout>(R.id.bottomNavigation).visibility = if (isAdmin) View.GONE else View.VISIBLE

        if (isAdmin) {
            adminScreen = AdminCoursesScreen(
                context = this,
                onAddCourse = { dialogs.edit() },
                onEditCourse = { course -> dialogs.edit(course) },
                onDeleteCourse = dialogs::confirmDelete
            )
            studentScreen = null
            screenContainer.addView(adminScreen!!.createView())
        } else {
            when (selectedTab) {
                HOME_TAB -> {
                    studentScreen = StudentCoursesScreen(this, R.layout.screen_student_courses) { course, enrolled ->
                        openCourseOrEnroll(course, enrolled)
                    }
                    adminScreen = null
                    screenContainer.addView(studentScreen!!.createView())
                }
                ENROLLED_TAB -> {
                    studentScreen = StudentCoursesScreen(this, R.layout.screen_enrolled_courses) { course, enrolled ->
                        openCourseOrEnroll(course, enrolled)
                    }
                    adminScreen = null
                    screenContainer.addView(studentScreen!!.createView())
                }
                PROFILE_TAB -> {
                    studentScreen = null
                    adminScreen = null
                }
            }
        }
        updateNavigationSelection()
    }

    private fun bindNavigation() {
        findViewById<TextView>(R.id.navHome).setOnClickListener { selectTab(HOME_TAB) }
        findViewById<TextView>(R.id.navEnrolled).setOnClickListener { selectTab(ENROLLED_TAB) }
        findViewById<TextView>(R.id.navProfile).setOnClickListener { selectTab(PROFILE_TAB) }
    }

    private fun selectTab(tab: String) {
        if (isAdmin || selectedTab == tab) return
        selectedTab = tab
        showCourses()
        refreshCourses()
    }

    private fun updateNavigationSelection() {
        setNavigationItem(R.id.navHome, selectedTab == HOME_TAB)
        setNavigationItem(R.id.navEnrolled, selectedTab == ENROLLED_TAB)
        setNavigationItem(R.id.navProfile, selectedTab == PROFILE_TAB)
    }

    private fun setNavigationItem(viewId: Int, selected: Boolean) {
        val item = findViewById<TextView>(viewId)
        item.setTextColor(getColor(if (selected) R.color.sage_text else R.color.sage_primary))
        item.setTypeface(null, if (selected) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun openCourseOrEnroll(course: CourseEntity, enrolled: Boolean) {
        startActivity(Intent(this, CourseDetailsActivity::class.java)
            .putExtra(CourseDetailsActivity.EXTRA_COURSE_ID, course.courseId))
    }

    private fun refreshCourses() {
        if (isAdmin) {
            repository.loadCourses { result ->
                result.onSuccess { courses -> adminScreen?.render(courses) }
                    .onFailure { showLoadError() }
            }
            return
        }
        val studentId = currentStudent?.userId ?: return
        repository.loadCourses { allResult ->
            allResult.onSuccess { allCourses ->
                repository.loadEnrolledCourses(studentId) { enrolledResult ->
                    enrolledResult.onSuccess { enrolledCourses ->
                        val enrolledIds = enrolledCourses.map { it.courseId }.toSet()
                        studentScreen?.render(
                            if (selectedTab == ENROLLED_TAB) enrolledCourses else allCourses,
                            enrolledIds
                        )
                    }.onFailure { showLoadError() }
                }
            }.onFailure { showLoadError() }
        }
    }

    private fun showLoadError() {
        Toast.makeText(this, "Couldn't load the demo learning data.", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val HOME_TAB = "home"
        private const val ENROLLED_TAB = "enrolled"
        private const val PROFILE_TAB = "profile"
    }
}
