package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SummaryActivity : AppCompatActivity() {

    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        db = DBHelper(this)

        val correct = intent.getIntExtra("CORRECT", 0)
        val wrong = intent.getIntExtra("WRONG", 0)
        val total = intent.getIntExtra("TOTAL", 0)
        val email = intent.getStringExtra("EMAIL") ?: "unknown"
        val level = intent.getStringExtra("LEVEL") ?: "Unknown"

        val correctPercent = if (total != 0) (correct * 100) / total else 0
        val wrongPercent = 100 - correctPercent

        val txtResult = findViewById<TextView>(R.id.txtResult)
        val txtCorrect = findViewById<TextView>(R.id.txtCorrect)
        val txtWrong = findViewById<TextView>(R.id.txtWrong)
        val txtTotal = findViewById<TextView>(R.id.txtTotal)
        val txtPercent = findViewById<TextView>(R.id.txtPercent)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnSave = findViewById<Button>(R.id.btnSaveResult)

        txtResult.text = "Quiz Completed!"
        txtCorrect.text = "Correct Answers: $correct"
        txtWrong.text = "Wrong Answers: $wrong"
        txtTotal.text = "Total Questions: $total"
        txtPercent.text = "Score: $correctPercent% Correct | $wrongPercent% Wrong"

        btnBack.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnSave.setOnClickListener {
            val saved = db.insertResult(email, level, correct, total)
            if (saved) {
                btnSave.text = "Saved ✓"
                btnSave.isEnabled = false
            }
        }
    }
}
