
package com.example.e_learning.admin

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.e_learning.R
import com.example.e_learning.auth.Role
import com.example.e_learning.auth.SessionManager
import com.example.e_learning.data.QuizRepository
import com.example.e_learning.database.AppDatabase
import com.example.e_learning.entities.QuestionEntity
import com.example.e_learning.entities.QuizEntity

class AdminCreateQuizActivity : AppCompatActivity() {

    private lateinit var repository: QuizRepository

    private lateinit var quizTitle: EditText
    private lateinit var quizDescription: EditText
    private lateinit var bulkInput: EditText
    private lateinit var questionsContainer: LinearLayout
    private lateinit var questionCount: TextView
    private lateinit var saveButton: Button

    private val questionCards = mutableListOf<QuestionCard>()

    private var courseId = -1L
    private var lessonId = -1L

    private data class QuestionCard(
        val root: LinearLayout,
        val questionInput: EditText,
        val optionInputs: List<EditText>,
        val correctGroup: RadioGroup,
        val correctButtons: List<RadioButton>,
        val pointsInput: EditText
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (SessionManager(this).role != Role.ADMIN) {
            finish()
            return
        }

        courseId = intent.getLongExtra(
            AdminQuizzesActivity.EXTRA_COURSE_ID, -1L
        )

        lessonId = intent.getLongExtra(
            AdminQuizzesActivity.EXTRA_LESSON_ID, -1L
        )

        if (courseId <= 0 || lessonId <= 0) {
            finish()
            return
        }

        setContentView(R.layout.activity_admin_create_quiz)

        repository = QuizRepository(AppDatabase.getInstance(this))

        quizTitle = findViewById(R.id.quizTitleInput)
        quizDescription = findViewById(R.id.quizDescriptionInput)
        bulkInput = findViewById(R.id.bulkQuestionsInput)
        questionsContainer = findViewById(R.id.questionsContainer)
        questionCount = findViewById(R.id.questionCountText)
        saveButton = findViewById(R.id.saveQuizButton)

        findViewById<TextView>(R.id.createQuizBackButton)
            .setOnClickListener {
                finish()
            }

        findViewById<Button>(R.id.addQuestionButton)
            .setOnClickListener {
                addQuestionCard()
            }

        findViewById<Button>(R.id.importQuestionsButton)
            .setOnClickListener {
                importQuestions()
            }

        saveButton.setOnClickListener {
            saveCompleteQuiz()
        }

        addQuestionCard()
    }

    // Create a new question card

    private fun addQuestionCard() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))

            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.LTGRAY)
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }

        val number = questionCards.size + 1

        val header = TextView(this).apply {
            text = "Question $number"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(36, 77, 59))
        }

        val questionInput = createField(
            "Enter your question",
            true
        )

        val optionInputs = listOf(
            createField("Option A"),
            createField("Option B"),
            createField("Option C"),
            createField("Option D")
        )

        val correctLabel = TextView(this).apply {
            text = "Select the correct answer:"
            textSize = 14f
            setTextColor(Color.DKGRAY)
        }

        val correctGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
        }

        val correctButtons = listOf("A", "B", "C", "D").map { letter ->

            RadioButton(this).apply {
                text = letter
                tag = letter
                id = View.generateViewId()
                textSize = 14f
                buttonTintList = android.content.res.ColorStateList.valueOf(
                    Color.rgb(53, 107, 82)
                )

                correctGroup.addView(
                    this,
                    RadioGroup.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )
            }
        }

        correctGroup.check(correctButtons[0].id)

        val pointsInput = createField("Points (default: 1)")
        pointsInput.inputType = InputType.TYPE_CLASS_NUMBER
        pointsInput.setText("1")

        val deleteButton = Button(this).apply {
            text = "Remove Question"
            setOnClickListener {
                removeQuestionCard(questionCards.first {
                    it.root === root
                })
            }
        }

        root.addView(header)
        root.addView(questionInput)
        optionInputs.forEach { root.addView(it) }
        root.addView(correctLabel)
        root.addView(correctGroup)
        root.addView(pointsInput)
        root.addView(deleteButton)

        questionsContainer.addView(root)

        questionCards.add(
            QuestionCard(
                root,
                questionInput,
                optionInputs,
                correctGroup,
                correctButtons,
                pointsInput
            )
        )

        updateQuestionCount()
    }

    private fun removeQuestionCard(card: QuestionCard) {

        questionCards.remove(card)
        questionsContainer.removeView(card.root)

        updateQuestionCount()
        updateQuestionNumbers()
    }

    private fun updateQuestionNumbers() {

        questionCards.forEachIndexed { index, card ->

            val header = card.root.getChildAt(0) as TextView
            header.text = "Question ${index + 1}"
        }
    }

    private fun updateQuestionCount() {

        questionCount.text =
            "${questionCards.size} Questions Added"
    }

    // Create reusable EditText fields

    private fun createField(
        hintText: String,
        multiline: Boolean = false
    ): EditText {

        return EditText(this).apply {

            hint = hintText
            textSize = 14f
            setTextColor(Color.BLACK)
            setPadding(dp(12), dp(10), dp(12), dp(10))

            inputType = if (multiline) {
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
            } else {
                InputType.TYPE_CLASS_TEXT
            }

            if (multiline) {
                minLines = 2
                gravity = Gravity.TOP or Gravity.START
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
                bottomMargin = dp(5)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()

    }

    private data class ParsedQuestion(
        val question: String,
        val options: List<String>,
        val correct: String,
        val points: Int
    )

    private fun importQuestions() {

        val raw = bulkInput.text.toString().trim()

        if (raw.isEmpty()) {
            Toast.makeText(
                this,
                "Paste your questions first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val blockRegex = Regex(
            "(?im)(?=^\\s*(?:Question|Q)\\s*\\d*\\s*[:.)-])"
        )

        val blocks = raw.split(blockRegex)
            .filter { it.isNotBlank() }

        if (blocks.isEmpty()) {
            toastImport("No valid question blocks found.")
            return
        }

        val parsedQuestions = mutableListOf<ParsedQuestion>()
        val errors = mutableListOf<String>()

        val questionRegex = Regex(
            "^(?:Question|Q)\\s*\\d*\\s*[:.)-]\\s*(.+)$",
            RegexOption.IGNORE_CASE
        )

        val optionRegex = Regex(
            "^(?:Option\\s*)?([ABCD])\\s*[:.)-]\\s*(.+)$",
            RegexOption.IGNORE_CASE
        )

        val correctRegex = Regex(
            "^Correct(?:\\s+Answer)?\\s*[:=]\\s*([ABCD])$",
            RegexOption.IGNORE_CASE
        )

        val pointsRegex = Regex(
            "^Points?\\s*[:=]\\s*(\\d+)$",
            RegexOption.IGNORE_CASE
        )

        blocks.forEachIndexed { index, block ->

            val lines = block.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val question = lines
                .mapNotNull { questionRegex.find(it) }
                .firstOrNull()
                ?.groupValues?.get(1)

            val options = mutableMapOf<String, String>()

            var correct: String? = null
            var points = 1

            lines.forEach { line ->

                optionRegex.find(line)?.let {
                    options[it.groupValues[1].uppercase()] =
                        it.groupValues[2].trim()
                }

                correctRegex.find(line)?.let {
                    correct = it.groupValues[1].uppercase()
                }

                pointsRegex.find(line)?.let {
                    points = it.groupValues[1].toInt()
                }
            }

            val valid =
                !question.isNullOrBlank() &&
                        listOf("A", "B", "C", "D").all {
                            !options[it].isNullOrBlank()
                        } &&
                        correct in listOf("A", "B", "C", "D") &&
                        points >= 1

            if (!valid) {
                errors.add("Question ${index + 1} has missing or invalid data.")
            } else {

                parsedQuestions.add(
                    ParsedQuestion(
                        question = question!!,
                        options = listOf(
                            options["A"]!!,
                            options["B"]!!,
                            options["C"]!!,
                            options["D"]!!
                        ),
                        correct = correct!!,
                        points = points
                    )
                )
            }
        }

        if (errors.isNotEmpty()) {
            toastImport(errors.joinToString("\n"))
            return
        }

        // Remove the initial empty card if it has not been edited.

        if (questionCards.size == 1) {

            val card = questionCards.first()

            val empty =
                card.questionInput.text.isBlank() &&
                        card.optionInputs.all { it.text.isBlank() }

            if (empty) {
                questionCards.remove(card)
                questionsContainer.removeView(card.root)
            }
        }

        parsedQuestions.forEach { parsed ->

            addQuestionCard()

            val card = questionCards.last()

            card.questionInput.setText(parsed.question)

            parsed.options.forEachIndexed { index, option ->
                card.optionInputs[index].setText(option)
            }

            val correctIndex = listOf("A", "B", "C", "D")
                .indexOf(parsed.correct)

            card.correctGroup.check(
                card.correctButtons[correctIndex].id
            )

            card.pointsInput.setText(parsed.points.toString())
        }

        bulkInput.text.clear()

        Toast.makeText(
            this,
            "${parsedQuestions.size} questions imported successfully!",
            Toast.LENGTH_LONG
        ).show()

        updateQuestionCount()
    }

    private fun toastImport(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun saveCompleteQuiz() {

        val title = quizTitle.text.toString().trim()
        val description = quizDescription.text.toString().trim()

        if (title.isEmpty()) {
            quizTitle.error = "Quiz title is required."
            quizTitle.requestFocus()
            return
        }

        if (questionCards.isEmpty()) {
            toastImport("Add at least one question.")
            return
        }

        val questions = mutableListOf<QuestionEntity>()

        questionCards.forEachIndexed { index, card ->

            val questionText =
                card.questionInput.text.toString().trim()

            val options = card.optionInputs.map {
                it.text.toString().trim()
            }

            if (questionText.isEmpty()) {
                toastImport("Question ${index + 1} is empty.")
                card.questionInput.requestFocus()
                return
            }

            if (options.any { it.isEmpty() }) {
                toastImport(
                    "Fill all four options in Question ${index + 1}."
                )
                return
            }

            val correctIndex = card.correctButtons.indexOfFirst {
                it.id == card.correctGroup.checkedRadioButtonId
            }

            if (correctIndex == -1) {
                toastImport(
                    "Select the correct answer for Question ${index + 1}."
                )
                return
            }

            val points = card.pointsInput.text
                .toString()
                .trim()
                .toIntOrNull()

            if (points == null || points < 1) {
                toastImport(
                    "Invalid points in Question ${index + 1}."
                )
                return
            }

            questions.add(
                QuestionEntity(
                    quizId = 0L,
                    questionText = questionText,
                    optionA = options[0],
                    optionB = options[1],
                    optionC = options[2],
                    optionD = options[3],
                    correctOption = listOf("A", "B", "C", "D")[correctIndex],
                    points = points,
                    position = index + 1
                )
            )
        }

        val quiz = QuizEntity(
            courseId = courseId,
            lessonId = lessonId,
            title = title,
            description = description
        )

        saveButton.isEnabled = false
        saveButton.text = "Saving..."

        repository.saveQuizWithQuestions(
            quiz,
            questions
        ) { result ->

            result.onSuccess {

                Toast.makeText(
                    this,
                    "Quiz saved successfully!",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }

            result.onFailure { error ->

                saveButton.isEnabled = true
                saveButton.text = "Save Complete Quiz"

                Toast.makeText(
                    this,
                    error.message ?: "Couldn't save quiz.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


}
