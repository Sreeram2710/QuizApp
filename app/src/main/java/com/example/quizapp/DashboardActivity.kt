package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    private lateinit var signOutBtn: Button
    private lateinit var resultsBtn: Button
    private lateinit var contactUsBtn: Button
    private lateinit var basicBtn: Button
    private lateinit var intermediateBtn: Button
    private lateinit var hardBtn: Button
    private lateinit var functionalitiesBtn: Button
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Button initializations
        signOutBtn = findViewById(R.id.btnSignOut)
        resultsBtn = findViewById(R.id.btnResults)
        contactUsBtn = findViewById(R.id.btnContactUs)
        basicBtn = findViewById(R.id.btnBasic)
        intermediateBtn = findViewById(R.id.btnIntermediate)
        hardBtn = findViewById(R.id.btnHard)
        functionalitiesBtn = findViewById(R.id.btnFunctionalities)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Button click actions
        signOutBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        resultsBtn.setOnClickListener {
            startActivity(Intent(this, ResultsActivity::class.java))
        }

        contactUsBtn.setOnClickListener {
            startActivity(Intent(this, ContactUsActivity::class.java))
        }

        basicBtn.setOnClickListener {
            startQuiz("Basic")
        }

        intermediateBtn.setOnClickListener {
            startQuiz("Intermediate")
        }

        hardBtn.setOnClickListener {
            startQuiz("Hard")
        }

        functionalitiesBtn.setOnClickListener {
            startActivity(Intent(this, FunctionalitiesActivity::class.java))
        }

        // Bottom navigation logic
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on Dashboard
                    true
                }
                R.id.nav_quiz -> {
                    startActivity(Intent(this, QuizActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun startQuiz(level: String) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("level", level)
        startActivity(intent)
    }
}
