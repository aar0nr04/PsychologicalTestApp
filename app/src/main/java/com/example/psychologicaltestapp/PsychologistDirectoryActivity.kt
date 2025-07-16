package com.example.psychologicaltestapp

import PsychologistAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PsychologistDirectoryActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PsychologistAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var allPsychologists = listOf<Psychologist>()

    override fun onCreate(savedInstanceState: Bundle?) {
        //addFakePsychologists() //para añadir psicologos

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

        //boton de agregar psicologo
        findViewById<Button>(R.id.becomePsychologistButton).setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("registerAsPsychologist", true)
            startActivity(intent)
        }

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
        Log.d("TestApp", "Mandando psicólogo: $psychologist")
        val intent = Intent(this, PsychologistDetailActivity::class.java)
        intent.putExtra("PSYCHOLOGIST", psychologist)
        startActivity(intent)
    }



    private fun addFakePsychologists() {
        val db = FirebaseFirestore.getInstance()

        val fakeList = listOf(
            mapOf(
                "name" to "Dr. Emily Brown",
                "specialty" to "Family Therapy",
                "location" to "Los Angeles",
                "phone" to "555-123-4567",
                "email" to "emily@example.com",
                "description" to "Helping families build strong relationships.",
                "imageUrl" to "https://i.pravatar.cc/150?img=5"
            ),
            mapOf(
                "name" to "Dr. Michael Lee",
                "specialty" to "Child Psychology",
                "location" to "Chicago",
                "phone" to "555-987-6543",
                "email" to "michael@example.com",
                "description" to "Expert in child behavioral development.",
                "imageUrl" to "https://i.pravatar.cc/150?img=10"
            ),
            mapOf(
                "name" to "Dr. Sarah Johnson",
                "specialty" to "Clinical Psychology",
                "location" to "New York",
                "phone" to "555-555-1212",
                "email" to "sarah@example.com",
                "description" to "Specialist in mood disorders and depression.",
                "imageUrl" to "https://i.pravatar.cc/150?img=15"
            ),
            mapOf(
                "name" to "Dr. Robert Smith",
                "specialty" to "Cognitive Behavioral Therapy",
                "location" to "Houston",
                "phone" to "555-888-7777",
                "email" to "robert@example.com",
                "description" to "Helping patients overcome anxiety and phobias.",
                "imageUrl" to "https://i.pravatar.cc/150?img=20"
            ),
            mapOf(
                "name" to "Dr. Laura Martinez",
                "specialty" to "Couples Counseling",
                "location" to "San Francisco",
                "phone" to "555-321-6543",
                "email" to "laura@example.com",
                "description" to "Relationship and marriage therapy expert.",
                "imageUrl" to "https://i.pravatar.cc/150?img=25"
            ),
            mapOf(
                "name" to "Dr. Daniel Wilson",
                "specialty" to "Neuropsychology",
                "location" to "Miami",
                "phone" to "555-444-3333",
                "email" to "daniel@example.com",
                "description" to "Cognitive assessments and brain health specialist.",
                "imageUrl" to "https://i.pravatar.cc/150?img=30"
            ),
            mapOf(
                "name" to "Dr. Sofia Gonzalez",
                "specialty" to "Addiction Counseling",
                "location" to "Austin",
                "phone" to "555-222-1111",
                "email" to "sofia@example.com",
                "description" to "Supporting recovery from substance abuse.",
                "imageUrl" to "https://i.pravatar.cc/150?img=35"
            ),
            mapOf(
                "name" to "Dr. David Kim",
                "specialty" to "Stress Management",
                "location" to "Seattle",
                "phone" to "555-654-9876",
                "email" to "david@example.com",
                "description" to "Helping clients manage stress and improve wellbeing.",
                "imageUrl" to "https://i.pravatar.cc/150?img=40"
            ),
            mapOf(
                "name" to "Dr. Maria Lopez",
                "specialty" to "Grief Counseling",
                "location" to "Dallas",
                "phone" to "555-111-2222",
                "email" to "maria@example.com",
                "description" to "Supporting individuals coping with loss.",
                "imageUrl" to "https://i.pravatar.cc/150?img=45"
            ),
            mapOf(
                "name" to "Dr. James Anderson",
                "specialty" to "Behavioral Therapy",
                "location" to "Phoenix",
                "phone" to "555-777-8888",
                "email" to "james@example.com",
                "description" to "Behavior modification and self-improvement coaching.",
                "imageUrl" to "https://i.pravatar.cc/150?img=50"
            )
        )

        fakeList.forEach { psychologist ->
            db.collection("psychologists").add(psychologist)
                .addOnSuccessListener {
                    println("Added ${psychologist["name"]}")
                }
                .addOnFailureListener { e ->
                    println("Error adding: ${e.message}")
                }
        }
    }

}
