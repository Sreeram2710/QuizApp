package com.example.quizapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class CloudContact(
    val id: String,
    val name: String,
    val relationship: String,
    val phone: String,
    val email: String?
)

class CloudContactsActivity : AppCompatActivity() {

    private lateinit var list: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var searchInput: EditText
    private lateinit var searchBtn: Button
    private lateinit var addBtn: Button
    private val root: View by lazy { findViewById(android.R.id.content) }

    private var col: CollectionReference? = null
    private val adapter = CloudContactsAdapter(
        onLongPress = { c -> confirmDelete(c) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_contacts)
        title = "Cloud Contacts"

        list = findViewById(R.id.contactsList)
        progress = findViewById(R.id.progress)
        searchInput = findViewById(R.id.searchInput)
        searchBtn = findViewById(R.id.searchBtn)
        addBtn = findViewById(R.id.addBtn)

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        try { col = Firebase.firestore.collection("contacts_nosql") }
        catch (_: Throwable) {
            Snackbar.make(root, "Firebase not initialised. Check google-services.json & plugin.", Snackbar.LENGTH_LONG).show()
            return
        }

        fetchAll()

        searchBtn.setOnClickListener {
            val q = searchInput.text?.toString()?.trim().orEmpty()
            if (q.isBlank()) fetchAll() else fetchPrefix(q)
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrBlank()) fetchAll()
            }
        })

        addBtn.setOnClickListener { showCreateDialog() }
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

        android.app.AlertDialog.Builder(this)
            .setTitle("Add Contact")
            .setView(form)
            .setPositiveButton("Save") { _, _ ->
                val n = name.text.toString().trim()
                val r = rel.text.toString().trim()
                val p = phone.text.toString().trim()
                val e = email.text.toString().trim().ifBlank { null }
                if (n.isBlank() || r.isBlank() || p.isBlank()) {
                    Snackbar.make(root, "Name, relationship, phone required.", Snackbar.LENGTH_LONG).show()
                } else {
                    createContact(n, r, p, e)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createContact(name: String, rel: String, phone: String, email: String?) {
        val collection = col ?: return
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    collection.add(mapOf(
                        "name" to name,
                        "relationship" to rel,
                        "phone" to phone,
                        "email" to email
                    )).await()
                }
                Snackbar.make(root, "Contact created.", Snackbar.LENGTH_LONG).show()
                fetchAll()
            } catch (t: Throwable) {
                progress.visibility = View.GONE
                Snackbar.make(root, t.message ?: "Create failed.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchAll() {
        val collection = col ?: return
        progress.visibility = View.VISIBLE
        list.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val docs = withContext(Dispatchers.IO) { collection.get().await() }
                val items = docs.documents.map { it.toContact() }
                progress.visibility = View.GONE
                list.visibility = View.VISIBLE
                adapter.submit(items)
                if (items.isEmpty()) Snackbar.make(root, "No contacts in Firestore.", Snackbar.LENGTH_LONG).show()
            } catch (t: Throwable) {
                progress.visibility = View.GONE
                Snackbar.make(root, t.message ?: "Failed to load.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchPrefix(prefixRaw: String) {
        val collection = col ?: return
        val prefix = prefixRaw.trim()
        progress.visibility = View.VISIBLE
        list.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val q = withContext(Dispatchers.IO) {
                    collection.orderBy("name").startAt(prefix).endAt(prefix + "\uf8ff").get().await()
                }
                var items = q.documents.map { it.toContact() }
                if (items.isEmpty()) {
                    val allSnap = withContext(Dispatchers.IO) { collection.get().await() }
                    items = allSnap.documents.map { it.toContact() }
                        .filter { it.name.startsWith(prefix, ignoreCase = true) }
                }
                progress.visibility = View.GONE
                list.visibility = View.VISIBLE
                adapter.submit(items)
                if (items.isEmpty()) Snackbar.make(root, "No matches for \"$prefix\".", Snackbar.LENGTH_LONG).show()
            } catch (t: Throwable) {
                progress.visibility = View.GONE
                Snackbar.make(root, t.message ?: "Search failed.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDelete(c: CloudContact) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Delete ${c.name}?")
            .setMessage("This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> doDelete(c.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun doDelete(id: String) {
        val collection = col ?: return
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { collection.document(id).delete().await() }
                Snackbar.make(root, "Deleted.", Snackbar.LENGTH_LONG).show()
                fetchAll()
            } catch (t: Throwable) {
                progress.visibility = View.GONE
                Snackbar.make(root, t.message ?: "Delete failed.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toContact() = CloudContact(
        id = id,
        name = getString("name") ?: "",
        relationship = getString("relationship") ?: "",
        phone = getString("phone") ?: "",
        email = getString("email")
    )
}

private class CloudContactsAdapter(
    val onLongPress: (CloudContact) -> Unit
) : RecyclerView.Adapter<CloudContactVH>() {
    private val data = mutableListOf<CloudContact>()
    fun submit(newData: List<CloudContact>) { data.clear(); data.addAll(newData); notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): CloudContactVH {
        val v = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return CloudContactVH(v, onLongPress)
    }
    override fun onBindViewHolder(h: CloudContactVH, pos: Int) = h.bind(data[pos])
    override fun getItemCount() = data.size
}
private class CloudContactVH(v: View, private val onLongPress: (CloudContact) -> Unit) : RecyclerView.ViewHolder(v) {
    private val t1 = v.findViewById<TextView>(android.R.id.text1)
    private val t2 = v.findViewById<TextView>(android.R.id.text2)
    private var current: CloudContact? = null
    init { v.setOnLongClickListener { current?.let(onLongPress); true } }
    fun bind(c: CloudContact) {
        current = c
        t1.text = c.name
        t2.text = "${c.relationship} • ${c.phone}" + (c.email?.let { " • $it" } ?: "")
    }
}
