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
                description = "Especializado en desarrollo infantil y trastornos del aprendizaje."
            ),
            Psychologist(
                id = "3",
                name = "Dra. Laura Martínez",
                specialty = "Neuropsicología",
                imageUrl = "https://example.com/laura-martinez.jpg",
                description = "Especialista en evaluación neuropsicológica y rehabilitación cognitiva."
            ),
            Psychologist(
                id = "4",
                name = "Dr. Javier Torres",
                specialty = "Psicología Organizacional",
                imageUrl = "https://example.com/javier-torres.jpg",
                description = "Consultor en bienestar laboral y desarrollo organizacional."
            ),
            Psychologist(
                id = "5",
                name = "Dra. Sofía Ramírez",
                specialty = "Psicología Forense",
                imageUrl = "https://example.com/sofia-ramirez.jpg",
                description = "Experta en peritajes psicológicos y mediación familiar."
            ),
            Psychologist(
                id = "6",
                name = "Dr. Miguel Ángel Ruiz",
                specialty = "Psicología Deportiva",
                imageUrl = "https://example.com/miguel-angel-ruiz.jpg",
                description = "Entrenador mental para atletas de alto rendimiento."
            ),
            Psychologist(
                id = "7",
                name = "Dra. Valeria Castro",
                specialty = "Psicología Educativa",
                imageUrl = "https://example.com/valeria-castro.jpg",
                description = "Especializada en orientación vocacional y habilidades de estudio."
            ),
            Psychologist(
                id = "8",
                name = "Dr. Eduardo Fernández",
                specialty = "Terapia Familiar",
                imageUrl = "https://example.com/eduardo-fernandez.jpg",
                description = "Experto en dinámicas familiares y terapia de pareja."
            ),
            Psychologist(
                id = "9",
                name = "Dra. Andrea Herrera",
                specialty = "Psicología Positiva",
                imageUrl = "https://example.com/andrea-herrera.jpg",
                description = "Enfoque en bienestar emocional y resiliencia personal."
            ),
            Psychologist(
                id = "10",
                name = "Dr. Daniel Sánchez",
                specialty = "Psicología Geriátrica",
                imageUrl = "https://example.com/daniel-sanchez.jpg",
                description = "Especialista en el manejo de la salud mental en adultos mayores."
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