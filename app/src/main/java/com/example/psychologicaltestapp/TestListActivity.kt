package com.example.psychologicaltestapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.example.psychologicaltestapp.databinding.ActivityTestListBinding
import android.widget.Toast

class TestListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestListBinding
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityTestListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // Load categories from JSON
            val categories = loadTestsFromJson(this)

            // Configure RecyclerView
            binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
            categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
                showTestsInCategory(selectedCategory)
            }
            binding.categoriesRecyclerView.adapter = categoryAdapter

            // Configure the "Back" button
            binding.backButton.setOnClickListener {
                finish() // Close this activity and return to the previous menu
            }

        } catch (e: Exception) {
            // Show an error message if something fails
            e.printStackTrace()
            Toast.makeText(this, "Error al cargar las categorías", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showTestsInCategory(category: Category) {
        val intent = Intent(this, TestDetailsActivity::class.java)
        intent.putExtra("CATEGORY", Gson().toJson(category)) // Pass the category as JSON
        startActivity(intent)
    }
}