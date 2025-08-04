package com.example.quizapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FunctionalitiesActivity : AppCompatActivity() {

    private val IMAGE_PICK_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_functionalities)

        // Pick image from gallery
        findViewById<Button>(R.id.btnGallery).setOnClickListener {
            pickImageFromGallery()
        }

        // Dial a number
        findViewById<Button>(R.id.btnDial).setOnClickListener {
            dialPhoneNumber("1234567890")
        }

        // Send an email
        findViewById<Button>(R.id.btnEmail).setOnClickListener {
            sendEmail("someone@example.com", "Subject here", "Email body here")
        }

        // Send an SMS
        findViewById<Button>(R.id.btnSMS).setOnClickListener {
            sendSMS("1234567890", "This is a test SMS.")
        }

        // Open website
        findViewById<Button>(R.id.btnOpenWebsite).setOnClickListener {
            val url = "https://developerquiz.org/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }

        // Open maps to a location
        findViewById<Button>(R.id.btnOpenMaps).setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:-36.8485,174.7633") // Auckland
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Google Maps app not found!", Toast.LENGTH_SHORT).show()
            }
        }

        // Play tutorial video
        val btnVideo = findViewById<Button>(R.id.btnPlayTutorial)
        btnVideo.setOnClickListener {
            val videoIntent = Intent(Intent.ACTION_VIEW)
            videoIntent.setData(Uri.parse("https://youtu.be/xuFdrXqpPB0?si=ncuCDbZtf8OUtHRQ"))
            startActivity(videoIntent)
        }

        // Add event to Google Calendar
        findViewById<Button>(R.id.btnAddCalendarEvent).setOnClickListener {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Sample Event")
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Mount Roskill Kindergarten")
                putExtra(CalendarContract.Events.DESCRIPTION, "Event created from QuizApp")
                putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    System.currentTimeMillis() + 60 * 60 * 1000 // 1 hour from now
                )
                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    System.currentTimeMillis() + 2 * 60 * 60 * 1000 // 2 hours from now
                )
            }

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No calendar app found.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$to")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }

        try {
            startActivity(Intent.createChooser(intent, "Send SMS"))
        } catch (e: Exception) {
            Toast.makeText(this, "No SMS app found.", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle selected image result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            Toast.makeText(this, "Image picked: $imageUri", Toast.LENGTH_SHORT).show()
            // Optional: Display image using ImageView if needed
        }
    }
}
