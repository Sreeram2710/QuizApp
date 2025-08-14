package com.example.quizapp

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

data class ContactDto(
    val id: Long,
    val name: String,
    val relationship: String,
    val phone: String,
    val email: String?
)
data class CreateContactBody(
    val name: String,
    val relationship: String,
    val phone: String,
    val email: String?
)
data class CreateResponse(val ok: Boolean, val id: Long?)
data class UpdateContactBody(
    val name: String,
    val relationship: String,
    val phone: String,
    val email: String?
)

interface SqlApi {
    @GET("contacts") suspend fun contacts(): List<ContactDto>
    @GET("contacts/search") suspend fun search(@Query("q") q: String): List<ContactDto>
    @POST("contacts") suspend fun create(@Body b: CreateContactBody): CreateResponse
    @PUT("contacts/{id}") suspend fun update(@Path("id") id: Long, @Body b: UpdateContactBody): Map<String, Any>
    @DELETE("contacts/{id}") suspend fun delete(@Path("id") id: Long): Map<String, Any>
}

class SqlContactsActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var searchInput: EditText
    private lateinit var searchBtn: Button
    private lateinit var addBtn: Button
    private val root: View by lazy { findViewById(android.R.id.content) }

    private val BASE_URL = "http://10.0.2.2:3000/"

    private val api: SqlApi by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(log).build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
            .create(SqlApi::class.java)
    }

    private val adapter = SqlContactsAdapter(onLongPress = { c -> showItemMenu(c) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sql_contacts)
        title = "SQL Contacts (HTTP)"

        list = findViewById(R.id.sqlContactsList)
        progress = findViewById(R.id.sqlProgress)
        searchInput = findViewById(R.id.sqlSearchInput)
        searchBtn = findViewById(R.id.sqlSearchBtn)
        addBtn = findViewById(R.id.sqlAddBtn)

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        loadAll()

        searchBtn.setOnClickListener {
            val q = searchInput.text?.toString()?.trim().orEmpty()
            if (q.isBlank()) loadAll() else search(q)
        }

        addBtn.setOnClickListener { showCreateDialog() }
    }

    private fun loadAll() {
        progress.visibility = View.VISIBLE
        list.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val rows = withContext(Dispatchers.IO) { api.contacts() }
                adapter.submit(rows)
                list.visibility = View.VISIBLE
            } catch (t: Throwable) {
                Snackbar.make(root, t.message ?: "Failed to fetch.", Snackbar.LENGTH_LONG).show()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun search(q: String) {
        progress.visibility = View.VISIBLE
        list.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val rows = withContext(Dispatchers.IO) { api.search(q) }
                adapter.submit(rows)
                list.visibility = View.VISIBLE
                if (rows.isEmpty()) Snackbar.make(root, "No matches for \"$q\".", Snackbar.LENGTH_LONG).show()
            } catch (t: Throwable) {
                Snackbar.make(root, t.message ?: "Search failed.", Snackbar.LENGTH_LONG).show()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun showCreateDialog() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 0)
        }
        val name = EditText(this).apply { hint = "Name" }
        val rel = EditText(this).apply { hint = "Relationship" }
        val phone = EditText(this).apply { hint = "Phone"; inputType = InputType.TYPE_CLASS_PHONE }
        val email = EditText(this).apply { hint = "Email"; inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        form.addView(name); form.addView(rel); form.addView(phone); form.addView(email)

        AlertDialog.Builder(this)
            .setTitle("Add Contact")
            .setView(form)
            .setPositiveButton("Save") { _, _ ->
                val b = CreateContactBody(
                    name.text.toString().trim(),
                    rel.text.toString().trim(),
                    phone.text.toString().trim(),
                    email.text.toString().trim().ifBlank { null }
                )
                if (b.name.isBlank() || b.relationship.isBlank() || b.phone.isBlank()) {
                    Snackbar.make(root, "Name, relationship, phone required.", Snackbar.LENGTH_LONG).show()
                } else {
                    createContact(b)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createContact(b: CreateContactBody) {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { api.create(b) }
                Snackbar.make(root, "Contact created.", Snackbar.LENGTH_LONG).show()
                loadAll()
            } catch (t: Throwable) {
                Snackbar.make(root, t.message ?: "Create failed.", Snackbar.LENGTH_LONG).show()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun showItemMenu(c: ContactDto) {
        val items = arrayOf("Edit phone", "Delete")
        AlertDialog.Builder(this)
            .setTitle(c.name)
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showEditDialog(c)
                    1 -> confirmDelete(c)
                }
            }
            .show()
    }

    private fun showEditDialog(c: ContactDto) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_PHONE
            setText(c.phone)
            setSelection(text.length)
        }
        AlertDialog.Builder(this)
            .setTitle("Edit phone for ${c.name}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newPhone = input.text?.toString()?.trim().orEmpty()
                if (newPhone.isBlank()) {
                    Snackbar.make(root, "Phone cannot be empty.", Snackbar.LENGTH_LONG).show()
                } else {
                    doUpdate(c, newPhone)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun doUpdate(c: ContactDto, newPhone: String) {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val body = UpdateContactBody(c.name, c.relationship, newPhone, c.email)
                withContext(Dispatchers.IO) { api.update(c.id, body) }
                Snackbar.make(root, "Updated ${c.name}.", Snackbar.LENGTH_LONG).show()
                loadAll()
            } catch (t: Throwable) {
                Snackbar.make(root, t.message ?: "Update failed.", Snackbar.LENGTH_LONG).show()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun confirmDelete(c: ContactDto) {
        AlertDialog.Builder(this)
            .setTitle("Delete ${c.name}?")
            .setMessage("This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> doDelete(c.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun doDelete(id: Long) {
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { api.delete(id) }
                Snackbar.make(root, "Deleted.", Snackbar.LENGTH_LONG).show()
                loadAll()
            } catch (t: Throwable) {
                Snackbar.make(root, t.message ?: "Delete failed.", Snackbar.LENGTH_LONG).show()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }
}

private class SqlContactsAdapter(
    val onLongPress: (ContactDto) -> Unit
) : RecyclerView.Adapter<SqlVH>() {
    private val data = mutableListOf<ContactDto>()
    fun submit(newData: List<ContactDto>) { data.clear(); data.addAll(newData); notifyDataSetChanged() }
    override fun onCreateViewHolder(p: android.view.ViewGroup, vt: Int): SqlVH {
        val v = android.view.LayoutInflater.from(p.context)
            .inflate(android.R.layout.simple_list_item_2, p, false)
        return SqlVH(v, onLongPress)
    }
    override fun onBindViewHolder(h: SqlVH, pos: Int) = h.bind(data[pos])
    override fun getItemCount() = data.size
}
private class SqlVH(v: View, val onLongPress: (ContactDto) -> Unit) : RecyclerView.ViewHolder(v) {
    private val t1 = v.findViewById<TextView>(android.R.id.text1)
    private val t2 = v.findViewById<TextView>(android.R.id.text2)
    private var current: ContactDto? = null
    init { v.setOnLongClickListener { current?.let(onLongPress); true } }
    fun bind(c: ContactDto) {
        current = c
        t1.text = c.name
        t2.text = "${c.relationship} • ${c.phone}" + (c.email?.let { " • $it" } ?: "")
    }
}
