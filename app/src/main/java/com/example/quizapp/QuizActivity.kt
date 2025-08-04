package com.example.quizapp

import Question
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var option1: RadioButton
    private lateinit var option2: RadioButton
    private lateinit var option3: RadioButton
    private lateinit var option4: RadioButton
    private lateinit var nextBtn: Button

    private lateinit var questions: List<Question>
    private var currentIndex = 0
    private var correctCount = 0
    private var wrongCount = 0

    private lateinit var userEmail: String
    private lateinit var level: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        questionText = findViewById(R.id.questionText)
        radioGroup = findViewById(R.id.radioGroup)
        option1 = findViewById(R.id.option1)
        option2 = findViewById(R.id.option2)
        option3 = findViewById(R.id.option3)
        option4 = findViewById(R.id.option4)
        nextBtn = findViewById(R.id.nextButton)

        level = intent.getStringExtra("LEVEL") ?: "Basic"
        userEmail = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            .getString("email", "anonymous@example.com") ?: "anonymous@example.com"

        questions = DBHelper(this).getQuestionsByLevel(level)

        if (questions.isNotEmpty()) {
            showQuestion()
        } else {
            Toast.makeText(this, "No questions found!", Toast.LENGTH_SHORT).show()
            finish()
        }

        nextBtn.setOnClickListener {
            if (radioGroup.checkedRadioButtonId != -1) {
                val selected = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
                val answer = selected.text.toString()

                if (answer == questions[currentIndex].correctAnswer) correctCount++
                else wrongCount++

                currentIndex++

                if (currentIndex < questions.size) {
                    showQuestion()
                } else {
                    val intent = Intent(this, SummaryActivity::class.java)
                    intent.putExtra("CORRECT", correctCount)
                    intent.putExtra("WRONG", wrongCount)
                    intent.putExtra("TOTAL", questions.size)
                    intent.putExtra("EMAIL", userEmail)
                    intent.putExtra("LEVEL", level)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showQuestion() {
        val q = questions[currentIndex]
        questionText.text = q.question
        option1.text = q.option1
        option2.text = q.option2
        option3.text = q.option3
        option4.text = q.option4
        radioGroup.clearCheck()
    }
}
