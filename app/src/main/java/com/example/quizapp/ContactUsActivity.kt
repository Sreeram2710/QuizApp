package com.example.quizapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ContactUsActivity : AppCompatActivity() {

    private val root: View by lazy { findViewById(android.R.id.content) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val dialBtn = findViewById<Button>(R.id.dialBtn)
        val titleInput = findViewById<EditText>(R.id.eventTitle)
        val locInput = findViewById<EditText>(R.id.eventLocation)
        val addCalBtn = findViewById<Button>(R.id.addCalendarBtn)

        // Dial (kept simple)
        dialBtn.setOnClickListener {
            val number = phoneInput.text?.toString()?.trim().orEmpty()
            if (number.isBlank()) {
                showSnack("Enter a phone number")
            } else {
                try {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                } catch (t: Throwable) {
                    showSnack(t.message ?: "No dialer available.")
                }
            }
        }

        // Add Calendar — app first, then web fallback
        addCalBtn.setOnClickListener {
            val title = titleInput.text?.toString()?.ifBlank { "My Event" } ?: "My Event"
            val location = locInput.text?.toString()?.trim().orEmpty()

            val begin = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }.timeInMillis
            val end = begin + 60 * 60 * 1000 // +1 hour

            openCalendarInsertOrFallback(title, location, begin, end)
        }
    }

    private fun openCalendarInsertOrFallback(title: String, location: String, begin: Long, end: Long) {
        val insertIntent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end)
        }

        try {
            if (insertIntent.resolveActivity(packageManager) != null) {
                startActivity(insertIntent)
                return
            } else {
                throw ActivityNotFoundException("No Calendar app installed")
            }
        } catch (_: Throwable) {
            val url = buildGCalUrl(title, location, begin, end)
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startActivity(webIntent)
            } catch (_: Throwable) {
                showSnack("No calendar or browser available to add event.")
            }
        }
    }

    private fun buildGCalUrl(title: String, location: String, begin: Long, end: Long): String {
        fun encode(s: String) = URLEncoder.encode(s, "UTF-8")
        fun toUtcStamp(ms: Long): String {
            val fmt = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            return fmt.format(Date(ms))
        }
        val t = encode(title)
        val loc = encode(location)
        val startUtc = toUtcStamp(begin)
        val endUtc = toUtcStamp(end)
        return "https://calendar.google.com/calendar/render?action=TEMPLATE&text=$t&location=$loc&dates=$startUtc/$endUtc"
    }

    private fun showSnack(msg: String) {
        Snackbar.make(root, msg, Snackbar.LENGTH_LONG).show()
    }
}
