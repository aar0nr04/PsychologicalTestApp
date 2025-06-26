package com.example.psychologicaltestapp

import TestResult
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class TestHistoryActivity : AppCompatActivity() {

    private lateinit var testHistoryAdapter: TestHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_history)

        val recyclerView = findViewById<RecyclerView>(R.id.testHistoryRecyclerView)

        // Example: Create a list of test results (replace with actual data)
        val testResults = listOf(
            TestResult("Test 1", "2023-01-01", "85"),
            TestResult("Test 2", "2023-01-02", "90")
        )

        // Pass the testResults to the adapter
        testHistoryAdapter = TestHistoryAdapter(testResults)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = testHistoryAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Redirect to login or show an error message
            Toast.makeText(this, "Debes iniciar sesión para guardar los resultados.", Toast.LENGTH_SHORT).show()
            return
        }
    }
}