package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResultsActivity : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        db = DBHelper(this)
        listView = findViewById(R.id.resultsListView)
        val deleteButton = findViewById<Button>(R.id.deleteResultsButton)
        val backButton = findViewById<Button>(R.id.btnBackToDashboard)  // 👈 Back button

        loadResults()

        deleteButton.setOnClickListener {
            val deleted = db.deleteAllResults()
            if (deleted) {
                Toast.makeText(this, "All results deleted", Toast.LENGTH_SHORT).show()
                loadResults()
            } else {
                Toast.makeText(this, "No results to delete", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loadResults() {
        val resultList = db.getAllResults()
        val displayList = resultList.map {
            "User: ${it.email}\nLevel: ${it.level}\nScore: ${it.score}/${it.total}\nTime: ${it.timestamp}"
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        listView.adapter = adapter
    }
}