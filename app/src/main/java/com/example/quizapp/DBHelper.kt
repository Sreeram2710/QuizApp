package com.example.quizapp

import Question
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "QuizAppDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE results(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "email TEXT, " +  // User who took the quiz
                    "level TEXT, " +
                    "score INTEGER, " +  // Correct count
                    "total INTEGER, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )

        db.execSQL(
            "CREATE TABLE users(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "email TEXT UNIQUE, " +
                    "password TEXT)"
        )

        db.execSQL(
            "CREATE TABLE questions(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "level TEXT, " +
                    "question TEXT, " +
                    "option1 TEXT, option2 TEXT, option3 TEXT, option4 TEXT, " +
                    "correct_option TEXT)"
        )

        // Basic Level Questions
        db.execSQL(
            "INSERT INTO questions(level, question, option1, option2, option3, option4, correct_option) VALUES" +
                    "('Basic', 'What is ASP.NET?', 'A database', 'A programming language', 'A web development framework', 'An IDE', 'A web development framework')," +
                    "('Basic', 'Which language is commonly used with ASP.NET?', 'Python', 'Java', 'C#', 'JavaScript', 'C#')," +
                    "('Basic', 'What does MVC stand for in ASP.NET MVC?', 'Model View Controller', 'Main Visual Compiler', 'Module Version Control', 'Markup View Compiler', 'Model View Controller')"
        )

        // Intermediate Level Questions
        db.execSQL(
            "INSERT INTO questions(level, question, option1, option2, option3, option4, correct_option) VALUES" +
                    "('Intermediate', 'Which file typically contains routing configuration in ASP.NET MVC?', 'Startup.cs', 'Web.config', 'Route.cs', 'Layout.cshtml', 'Startup.cs')," +
                    "('Intermediate', 'What is the function of ViewBag in ASP.NET MVC?', 'Database access', 'Send data to views', 'Store session variables', 'Encrypt data', 'Send data to views')," +
                    "('Intermediate', 'Which ASP.NET component helps manage dependencies?', 'NuGet', 'ViewEngine', 'SessionManager', 'Razor', 'NuGet')"
        )

        // Hard Level Questions
        db.execSQL(
            "INSERT INTO questions(level, question, option1, option2, option3, option4, correct_option) VALUES" +
                    "('Hard', 'What is Dependency Injection in ASP.NET Core?', 'A security protocol', 'An HTML parser', 'A design pattern for resolving dependencies', 'A data binding tool', 'A design pattern for resolving dependencies')," +
                    "('Hard', 'Which middleware is used to handle errors globally in ASP.NET Core?', 'UseAuthorization()', 'UseEndpoints()', 'UseExceptionHandler()', 'UseRouting()', 'UseExceptionHandler()')," +
                    "('Hard', 'In Razor pages, what does @Model represent?', 'Controller logic', 'View state', 'Page’s model data', 'CSS classes', 'Page’s model data')"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS users")
        db.execSQL("DROP TABLE IF EXISTS questions")
        onCreate(db)
    }

    // User Registration
    fun insertUser(name: String, email: String, password: String): Boolean {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }

        val result = db.insert("users", null, values)
        return result != -1L
    }

    // User Login Check
    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email = ? AND password = ?",
            arrayOf(email, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Add question to DB (can be used for manual or programmatic insert)
    fun insertQuestion(level: String, question: String, o1: String, o2: String, o3: String, o4: String, correct: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("level", level)
            put("question", question)
            put("option1", o1)
            put("option2", o2)
            put("option3", o3)
            put("option4", o4)
            put("correct_option", correct)
        }

        val result = db.insert("questions", null, values)
        return result != -1L
    }

    // Fetch questions for a specific level
    fun getQuestionsByLevel(level: String): List<Question> {
        val questionList = mutableListOf<Question>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM questions WHERE level = ?", arrayOf(level))

        if (cursor.moveToFirst()) {
            do {
                questionList.add(
                    Question(
                        cursor.getInt(0),         // id
                        cursor.getString(1),      // level
                        cursor.getString(2),      // question
                        cursor.getString(3),      // option1
                        cursor.getString(4),      // option2
                        cursor.getString(5),      // option3
                        cursor.getString(6),      // option4
                        cursor.getString(7)       // correct_option
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return questionList
    }

    fun insertResult(email: String, level: String, score: Int, total: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("email", email)
            put("level", level)
            put("score", score)
            put("total", total)
        }
        val result = db.insert("results", null, values)
        return result != -1L
    }

    fun getAllResults(): List<Result> {
        val resultList = mutableListOf<Result>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM results ORDER BY timestamp DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val result = Result(
                    cursor.getInt(0),                 // id
                    cursor.getString(1),              // email
                    cursor.getString(2),              // level
                    cursor.getInt(3),                 // score
                    cursor.getInt(4),                 // total
                    cursor.getString(5)               // timestamp
                )
                resultList.add(result)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return resultList
    }

    // Delete all quiz results from the database
    fun deleteAllResults(): Boolean {
        val db = this.writableDatabase
        val result = db.delete("results", null, null)
        return result != -1
    }

    fun deleteResultsByEmail(email: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete("results", "email = ?", arrayOf(email))
        return result != -1
    }
}