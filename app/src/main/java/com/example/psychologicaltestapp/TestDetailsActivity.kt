package com.example.psychologicaltestapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.example.psychologicaltestapp.databinding.ActivityTestDetailsBinding

class TestDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestDetailsBinding
    private lateinit var testAdapter: TestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityTestDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // Retrieve the category passed as JSON
            val categoryJson = intent.getStringExtra("CATEGORY")
                ?: throw IllegalArgumentException("Categoría no proporcionada")

            // Convert the JSON to a Category object
            val category = Gson().fromJson(categoryJson, Category::class.java)

            // Set the category title
            binding.testTitle.text = category.title

            // Configure RecyclerView to display the list of tests
            binding.testsRecyclerView.layoutManager = LinearLayoutManager(this)
            testAdapter = TestAdapter(category.tests) { selectedTest ->
                startTest(selectedTest)
            }
            binding.testsRecyclerView.adapter = testAdapter

            // Configure the "Back" button
            binding.backButton.setOnClickListener {
                finish() // Close this activity and return to the previous menu
            }

        } catch (e: Exception) {
            // Show an error message if something fails
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar los detalles del test", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startTest(test: Test) {
        val intent = Intent(this, TestActivity::class.java)
        intent.putExtra("TEST_TYPE", test.type) // Pass the test type
        startActivity(intent)
    }
}