package com.example.e_learning

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.CourseEntity
import com.example.e_learning.entities.LessonEntity
import com.example.e_learning.entities.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminIntegrationTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testAdminRoleValidationAndUserCounts() = runBlocking {
        // Insert Admin User
        val adminId = database.userDao().insert(
            UserEntity(
                fullName = "Admin User",
                email = "admin@test.com",
                passwordHash = "hash",
                role = "ADMIN"
            )
        )
        assertTrue(adminId > 0)

        // Insert Student Users
        val studentId = database.userDao().insert(
            UserEntity(
                fullName = "Student User",
                email = "student@test.com",
                passwordHash = "hash",
                role = "STUDENT"
            )
        )
        assertTrue(studentId > 0)

        // Validate Admin Retrieval
        val foundAdmin = database.userDao().findFirstAdmin()
        assertNotNull(foundAdmin)
        assertEquals("ADMIN", foundAdmin?.role)

        // Validate Student Count for Dashboard
        val studentCount = database.userDao().observeStudentCount().first()
        assertEquals(1, studentCount)

        // Validate Students List
        val students = database.userDao().getAllStudents()
        assertEquals(1, students.size)
        assertEquals("Student User", students[0].fullName)
    }

    @Test
    fun testAdminDashboardCounts() = runBlocking {
        // Insert Course
        val courseId = database.courseDao().insert(
            CourseEntity(
                title = "Android Development",
                description = "Learn Kotlin",
                language = "Kotlin"
            )
        )
        assertTrue(courseId > 0)

        // Insert Lesson
        val lessonId = database.lessonDao().insert(
            LessonEntity(
                courseId = courseId,
                title = "Lesson 1: Basics",
                contentType = "TEXT",
                textContent = "Intro to Kotlin",
                position = 1
            )
        )
        assertTrue(lessonId > 0)

        // Validate Counts
        val courseCount = database.courseDao().observeCount().first()
        val lessonCount = database.lessonDao().observeCount().first()

        assertEquals(1, courseCount)
        assertEquals(1, lessonCount)
    }
}
