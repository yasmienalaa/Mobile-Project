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
import com.example.e_learning.admin.AdminLessonsActivity
import com.example.e_learning.auth.Role
import com.example.e_learning.auth.SessionManager
import com.example.e_learning.data.CourseRepository
import com.example.e_learning.data.ProgressRepository
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.CourseEntity
import com.example.e_learning.entities.UserEntity
import com.example.e_learning.ui.AdminCoursesScreen
import com.example.e_learning.ui.AdminStudentsScreen
import com.example.e_learning.ui.CourseDialogs
import com.example.e_learning.ui.StudentCoursesScreen
import com.example.e_learning.ui.StudentDashboardScreen

class HomeActivity : AppCompatActivity() {
    private lateinit var repository: CourseRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var dialogs: CourseDialogs
    private lateinit var database: AppDatabase
    private var isAdmin = false
    private var isViewingStudents = false
    private var selectedTab = HOME_TAB
    private var currentStudent: UserEntity? = null
    private var studentScreen: StudentCoursesScreen? = null
    private var dashboardScreen: StudentDashboardScreen? = null
    private var adminScreen: AdminCoursesScreen? = null
    private var adminStudentsScreen: AdminStudentsScreen? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)
        val sessionRole = session.role
        val sessionUserId = session.userId

        if (sessionRole == null || sessionUserId == null) {
            startActivity(
                Intent(this, MainActivity::class.java)
            )
            finish()
            return
        }

        isAdmin = sessionRole == Role.ADMIN
        window.statusBarColor = getColor(R.color.sage_background)
        window.navigationBarColor = getColor(R.color.sage_background)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        database = AppDatabase.getInstance(this)
        repository = CourseRepository(applicationContext, database)
        progressRepository = ProgressRepository(database)

        if (!isAdmin) {
        repository.setActiveUserId(sessionUserId)
        }
        dialogs = CourseDialogs(this, repository, ::refreshCourses)
        showCourses()
        if (isAdmin) {
            refreshCourses()
        } else {
            repository.loadCurrentStudent { studentResult ->
                studentResult.onSuccess { student ->
                    currentStudent = student
                    refreshCourses()
                }.onFailure {
                    showLoadError()
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (::repository.isInitialized && (currentStudent != null || isAdmin)) refreshCourses()
    }

    private fun showCourses() {
        setContentView(R.layout.activity_home)
        val roleToggle = findViewById<TextView>(R.id.roleToggle)
        val screenContainer = findViewById<FrameLayout>(R.id.screenContainer)
        roleToggle.text = if (isAdmin) "Admin mode" else "Student mode"

        bindNavigation()
        findViewById<LinearLayout>(R.id.bottomNavigation).visibility = View.VISIBLE
        findViewById<TextView>(R.id.navHome).visibility = if (isAdmin) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.navEnrolled).visibility = if (isAdmin) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.navProfile).visibility = View.VISIBLE
        if (isAdmin) {
            if (isViewingStudents) {
                adminStudentsScreen = AdminStudentsScreen(
                    context = this,
                    onBack = {
                        isViewingStudents = false
                        showCourses()
                        refreshCourses()
                    }
                )
                adminScreen = null
                studentScreen = null
                screenContainer.addView(adminStudentsScreen!!.createView())
            } else {
                adminScreen = AdminCoursesScreen(
                    context = this,
                    onAddCourse = { performAdminAction { dialogs.edit() } },
                    onEditCourse = { course -> performAdminAction { dialogs.edit(course) } },
                    onDeleteCourse = { course -> performAdminAction { dialogs.confirmDelete(course) } },
                    onViewStudents = {
                        performAdminAction {
                            isViewingStudents = true
                            showCourses()
                            refreshCourses()
                        }
                    },
                    onManageLessons = {
                        Toast.makeText(this, "Select a course below to manage its lessons & quizzes", Toast.LENGTH_SHORT).show()
                    },
                    onManageQuizzes = { course ->
                        startActivity(Intent(this, AdminLessonsActivity::class.java)
                            .putExtra(AdminLessonsActivity.EXTRA_COURSE_ID, course.courseId)
                            .putExtra(AdminLessonsActivity.EXTRA_COURSE_TITLE, course.title))
                    }
                )
                adminStudentsScreen = null
                studentScreen = null
                screenContainer.addView(adminScreen!!.createView())
            }
        } else {
            when (selectedTab) {
                HOME_TAB -> {
                    studentScreen = StudentCoursesScreen(this, R.layout.screen_student_courses) { course, enrolled ->
                        openCourseOrEnroll(course, enrolled)
                    }
                    adminScreen = null
                    dashboardScreen = null
                    screenContainer.addView(studentScreen!!.createView())
                }
                ENROLLED_TAB -> {
                    studentScreen = StudentCoursesScreen(
                        this,
                        R.layout.screen_enrolled_courses
                    ) { course, enrolled ->
                        openCourseOrEnroll(course, enrolled)
                    }

                    adminScreen = null
                    dashboardScreen = null
                    screenContainer.addView(studentScreen!!.createView())
                }
            }
        }
        updateNavigationSelection()
    }

    private fun bindNavigation() {
        findViewById<TextView>(R.id.navHome).setOnClickListener { selectTab(HOME_TAB) }
        findViewById<TextView>(R.id.navEnrolled).setOnClickListener { selectTab(ENROLLED_TAB) }
        findViewById<TextView>(R.id.navProfile).setOnClickListener { startActivity(Intent(this, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN_PROFILE, true)) }
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
        setNavigationItem(R.id.navProfile, false)
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

    private fun performAdminAction(action: () -> Unit) {
        if (!isAdmin) {
            Toast.makeText(this, "Access Denied: ADMIN role required.", Toast.LENGTH_SHORT).show()
            return
        }

        action()
    }
    private fun refreshCourses() {
        if (isAdmin) {
            if (isViewingStudents) {
                repository.loadStudentsSummary { result ->
                    result.onSuccess { students -> adminStudentsScreen?.render(students) }
                        .onFailure { showLoadError() }
                }
            } else {
                repository.loadCoursesWithLessonCount { result ->
                    result.onSuccess { coursesWithCount -> adminScreen?.render(coursesWithCount) }
                        .onFailure { showLoadError() }
                }
                repository.loadAdminStats { result ->
                    result.onSuccess { stats -> adminScreen?.renderStats(stats) }
                }
            }
            return
        }
        val studentId = currentStudent?.userId ?: return
        if (selectedTab == ENROLLED_TAB || selectedTab == PROFILE_TAB) {
            progressRepository.loadStudentSummary(studentId) { result ->
                result.onSuccess { summary -> dashboardScreen?.render(summary) }
                    .onFailure { showLoadError() }
            }
            return
        }
        repository.loadCourses { allResult ->
            allResult.onSuccess { allCourses ->
                repository.loadEnrolledCourses(studentId) { enrolledResult ->
                    enrolledResult.onSuccess { enrolledCourses ->
                        val enrolledIds = enrolledCourses.map { it.courseId }.toSet()
                        studentScreen?.render(allCourses, enrolledIds)
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

    }
}
