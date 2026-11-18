package com.example.e_learning.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("CREATE TEMP TABLE bk_quizzes AS SELECT * FROM quizzes")
        db.execSQL("CREATE TEMP TABLE bk_questions AS SELECT * FROM questions")
        db.execSQL("CREATE TEMP TABLE bk_results AS SELECT * FROM quiz_results")
        db.execSQL("CREATE TEMP TABLE bk_answers AS SELECT * FROM quiz_answers")


        db.execSQL("DELETE FROM quiz_answers")
        db.execSQL("DELETE FROM quiz_results")
        db.execSQL("DELETE FROM questions")
        db.execSQL("DROP TABLE quizzes")


        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `quizzes` (" +
                    "`quizId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`courseId` INTEGER NOT NULL, " +
                    "`lessonId` INTEGER NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`description` TEXT NOT NULL, " +
                    "FOREIGN KEY(`courseId`) REFERENCES `courses`(`courseId`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                    "FOREIGN KEY(`lessonId`) REFERENCES `lessons`(`lessonId`) ON UPDATE NO ACTION ON DELETE CASCADE )"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_quizzes_courseId` ON `quizzes` (`courseId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_quizzes_lessonId` ON `quizzes` (`lessonId`)")

        db.execSQL(
            "INSERT INTO quizzes (quizId, courseId, lessonId, title, description) " +
                    "SELECT q.quizId, q.courseId, " +
                    "(SELECT l.lessonId FROM lessons l WHERE l.courseId = q.courseId ORDER BY l.position LIMIT 1), " +
                    "q.title, q.description " +
                    "FROM bk_quizzes q " +
                    "WHERE EXISTS (SELECT 1 FROM lessons l WHERE l.courseId = q.courseId)"
        )
        db.execSQL("INSERT INTO questions SELECT * FROM bk_questions WHERE quizId IN (SELECT quizId FROM quizzes)")
        db.execSQL("INSERT INTO quiz_results SELECT * FROM bk_results WHERE quizId IN (SELECT quizId FROM quizzes)")
        db.execSQL("INSERT INTO quiz_answers SELECT * FROM bk_answers WHERE resultId IN (SELECT resultId FROM quiz_results)")


        db.execSQL("DROP TABLE bk_answers")
        db.execSQL("DROP TABLE bk_results")
        db.execSQL("DROP TABLE bk_questions")
        db.execSQL("DROP TABLE bk_quizzes")
    }
}