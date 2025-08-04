package com.example.quizapp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpLink: TextView
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize DB and SharedPreferences
        db = DBHelper(this)
        sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // Get references to UI components
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        signUpLink = findViewById(R.id.signupLink)

        // Login button click
        loginButton.setOnClickListener {
            try {
                val email = emailEditText.text.toString().trim()
                val password = passwordEditText.text.toString().trim()

                // Validation
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Check credentials
                val isAuthenticated = db.checkUser(email, password)
                if (isAuthenticated) {
                    sharedPrefs.edit().putString("email", email).apply()
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Login error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Redirect to SignUp
        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
