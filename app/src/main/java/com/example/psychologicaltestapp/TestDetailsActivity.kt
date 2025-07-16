package com.example.psychologicaltestapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.example.psychologicaltestapp.databinding.ActivityTestDetailsBinding

class TestDetailsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var testAdapter: TestAdapter
    private lateinit var category: Category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_details)

        // Obtener la categoría desde el intent
        val categoryJson = intent.getStringExtra("CATEGORY")
        category = Gson().fromJson(categoryJson, Category::class.java)

        // Set title
        val titleView = findViewById<TextView>(R.id.testTitle)
        titleView.text = category.title

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.testsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        testAdapter = TestAdapter(category.tests) { selectedTest ->
            val intent = Intent(this, TestActivity::class.java)
            intent.putExtra("TEST_TYPE",  selectedTest.type)
            startActivity(intent)
        }
        recyclerView.adapter = testAdapter

        // Botón de regreso
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener { finish() }
    }
}

