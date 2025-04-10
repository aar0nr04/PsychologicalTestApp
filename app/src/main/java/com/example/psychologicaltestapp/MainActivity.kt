package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Cargar las categorías desde el archivo JSON
        val categories = loadTestsFromJson(this)

        // Configurar RecyclerView
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter(categories) { selectedCategory ->
            showTestsInCategory(selectedCategory)
        }
        categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun showTestsInCategory(category: Category) {
        val intent = Intent(this, TestListActivity::class.java)
        intent.putExtra("CATEGORY", Gson().toJson(category)) // Pasar la categoría como JSON
        startActivity(intent)
    }
}