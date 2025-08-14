package com.example.quizapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.URL
import java.net.UnknownHostException

data class RssItem(val title: String, val link: String, val pubDate: String?)

class RssActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var progress: ProgressBar
    private val root: View by lazy { findViewById(android.R.id.content) }

    // You can change this to any RSS URL you like
    private val FEED_URL = "https://feeds.bbci.co.uk/news/rss.xml"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss)

        list = findViewById(R.id.rssList)
        progress = findViewById(R.id.progress)

        list.layoutManager = LinearLayoutManager(this)
        val adapter = RssAdapter { item ->
            // open article in browser (small exception handling)
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.link))) }
            catch (t: Throwable) { showSnack(t.message ?: "No browser available.") }
        }
        list.adapter = adapter

        loadFeed(adapter)
    }

    private fun loadFeed(adapter: RssAdapter) {
        progress.visibility = View.VISIBLE
        list.visibility = View.GONE

        Thread {
            try {
                val items = fetchAndParse(FEED_URL)
                runOnUiThread {
                    progress.visibility = View.GONE
                    list.visibility = View.VISIBLE
                    adapter.submit(items)
                }
            } catch (t: Throwable) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    val msg = when (t) {
                        is UnknownHostException -> "No internet connection."
                        else -> t.message ?: "Failed to load feed."
                    }
                    showSnack(msg)
                }
            }
        }.start()
    }

    private fun fetchAndParse(url: String): List<RssItem> {
        val stream: InputStream = URL(url).openStream()
        stream.use {
            val factory = XmlPullParserFactory.newInstance()
            val xpp = factory.newPullParser()
            xpp.setInput(it, null)

            val items = mutableListOf<RssItem>()
            var event = xpp.eventType
            var insideItem = false
            var title: String? = null
            var link: String? = null
            var pubDate: String? = null

            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        val name = xpp.name
                        if (name.equals("item", ignoreCase = true)) {
                            insideItem = true
                            title = null; link = null; pubDate = null
                        } else if (insideItem) {
                            when (name.lowercase()) {
                                "title" -> title = xpp.nextText()
                                "link" -> link = xpp.nextText()
                                "pubdate" -> pubDate = xpp.nextText()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (xpp.name.equals("item", ignoreCase = true) && insideItem) {
                            val t = title ?: "(no title)"
                            val l = link ?: "https://example.com"
                            items.add(RssItem(t, l, pubDate))
                            insideItem = false
                        }
                    }
                }
                event = xpp.next()
            }
            return items
        }
    }

    private fun showSnack(msg: String) {
        Snackbar.make(root, msg, Snackbar.LENGTH_LONG).show()
    }
}

/** Minimal adapter (kept in the same file to avoid extra classes) */
private class RssAdapter(
    private val onClick: (RssItem) -> Unit
) : RecyclerView.Adapter<RssVH>() {

    private val data = mutableListOf<RssItem>()

    fun submit(newItems: List<RssItem>) {
        data.clear()
        data.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RssVH {
        val v = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return RssVH(v, onClick)
    }

    override fun onBindViewHolder(holder: RssVH, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size
}

private class RssVH(
    v: View,
    private val onClick: (RssItem) -> Unit
) : RecyclerView.ViewHolder(v) {
    private val t1 = v.findViewById<android.widget.TextView>(android.R.id.text1)
    private val t2 = v.findViewById<android.widget.TextView>(android.R.id.text2)
    private var current: RssItem? = null
    init { v.setOnClickListener { current?.let(onClick) } }
    fun bind(item: RssItem) {
        current = item
        t1.text = item.title
        t2.text = item.pubDate ?: ""
    }
}
