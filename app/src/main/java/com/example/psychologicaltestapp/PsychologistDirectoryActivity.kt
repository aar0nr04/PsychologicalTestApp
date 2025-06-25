package com.example.psychologicaltestapp

import Psychologist
import PsychologistAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PsychologistDirectoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PsychologistAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var allPsychologists = listOf<Psychologist>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_psychologist_directory)

        recyclerView = findViewById(R.id.recyclerView)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = PsychologistAdapter(listOf()) { psychologist ->
            openDetail(psychologist)
        }
        recyclerView.adapter = adapter

        loadPsychologists()

        val searchField = findViewById<EditText>(R.id.searchField)
        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadPsychologists() {
        db.collection("psychologists").get().addOnSuccessListener { snapshot ->
            allPsychologists = snapshot.map { doc ->
                Psychologist(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    specialty = doc.getString("specialty") ?: "",
                    location = doc.getString("location") ?: "",
                    phone = doc.getString("phone") ?: "",
                    email = doc.getString("email") ?: "",
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            adapter.updateList(allPsychologists)
        }.addOnFailureListener {
            Toast.makeText(this, "Error loading psychologists", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filter(query: String) {
        val filtered = allPsychologists.filter {
            it.name.contains(query, true) ||
                    it.specialty.contains(query, true) ||
                    it.location.contains(query, true)
        }
        adapter.updateList(filtered)
    }

    private fun openDetail(psychologist: Psychologist) {
        Toast.makeText(this, "Selected: ${psychologist.name}", Toast.LENGTH_SHORT).show()
        // Aquí puedes abrir una nueva pantalla con los detalles si quieres
    }
}
