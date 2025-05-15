package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.example.psychologicaltestapp.databinding.ActivityTestListBinding

class TestListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestListBinding
    private lateinit var testAdapter: TestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando View Binding
        binding = ActivityTestListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // Obtener la categoría pasada como JSON
            val categoryJson = intent.getStringExtra("CATEGORY")
                ?: throw IllegalArgumentException("Categoría no proporcionada")

            // Convertir el JSON a un objeto Category
            val category = Gson().fromJson(categoryJson, Category::class.java)

            // Verificar si hay tests disponibles
            if (category.tests.isNullOrEmpty()) {
                Toast.makeText(this, "No hay tests disponibles", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // Configurar RecyclerView
            binding.testsRecyclerView.layoutManager = LinearLayoutManager(this)
            testAdapter = TestAdapter(category.tests) { selectedTest ->
                startTest(selectedTest)
            }
            binding.testsRecyclerView.adapter = testAdapter

            // Configurar el botón "Regresar"
            binding.backButton.setOnClickListener {
                finish() // Cerrar esta actividad y regresar al menú anterior
            }

        } catch (e: Exception) {
            // Mostrar un mensaje de error si algo falla
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar los tests", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startTest(test: Test) {
        val intent = Intent(this, TestActivity::class.java)
        intent.putExtra("TEST_TYPE", test.type) // Pasar el tipo de test
        startActivity(intent)
    }
}