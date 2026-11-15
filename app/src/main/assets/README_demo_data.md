# Demo data for the e-learning app

`demo_data.json` is the source fixture. `DemoDataSeeder` loads missing records into Room on app start and keeps existing records; it does not reset or overwrite the database.

## Demo accounts

All sample passwords are `Demo123!`. The fixture stores SHA-256 hashes for demo use only.

| Role | Name | Email | Seeded enrollments |
|---|---|---|---|
| Student | Nadia Hassan | `nadia@softsage.test` | Kotlin Foundations, Python for Curious Minds |
| Student | Omar Adel | `omar@softsage.test` | Web Pages with JavaScript |
| Admin | Soft Sage Admin | `admin@softsage.test` | — |

The current student defaults to the first saved student (Nadia when the fixture is installed into an empty database). Student enrollment rows are keyed by `userId`, so each student's Enrolled Courses tab is isolated.

## Fixture coverage

- Three programming courses across Kotlin, Python, and JavaScript.
- Lessons with text and demo video URLs for the lessons team.
- Checkpoint quizzes and multiple choice questions for the quiz team.
- Enrollments, lesson completion examples, quiz scores, and answer snapshots for progress/dashboard work.
- A demo administrator profile for course-management workflows.

## Authentication integration

`CourseRepository.ACTIVE_USER_ID` is stored in the `student_session` preferences. The authentication flow should call `setActiveUserId(userId)` after sign-in so Home, Profile, and Enrolled Courses use the signed-in student. Demo account hashes are fixture values and should be adapted if the authentication member chooses a different password-hashing scheme.