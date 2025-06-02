package com.example.psychologicaltestapp

import Psychologist
import PsychologistAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.psychologicaltestapp.databinding.ActivityPsychologistDirectoryBinding

class PsychologistDirectoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPsychologistDirectoryBinding
    private lateinit var adapter: PsychologistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPsychologistDirectoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sample data (replace with real data from API or database)
        val psychologists = listOf(
            Psychologist(
                id = "1",
                name = "Dr. Ana López",
                specialty = "Psicología Clínica",
                imageUrl = "https://example.com/ana-lopez.jpg",
                description = "Experta en terapia cognitivo-conductual."
            ),
            Psychologist(
                id = "2",
                name = "Dr. Carlos Gómez",
                specialty = "Psicología Infantil",
                imageUrl = "https://example.com/carlos-gomez.jpg",
                description = "Especializado en desarrollo infantil."
            )
        )

        // Initialize RecyclerView
        adapter = PsychologistAdapter(psychologists) { psychologist ->
            // Handle click event (e.g., open a detailed profile screen)
            val intent = Intent(this, PsychologistDetailActivity::class.java).apply {
                putExtra("PSYCHOLOGIST", psychologist)
            }
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PsychologistDirectoryActivity)
            adapter = this@PsychologistDirectoryActivity.adapter
        }
    }
}