package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class TestHistoryActivity : BaseActivity() {

    private lateinit var testHistoryAdapter: TestResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_history)

        val recyclerView = findViewById<RecyclerView>(R.id.testHistoryRecyclerView)

        val testResults = listOf(
            TestResult("Test 1", "2023-01-01", "85"),
            TestResult("Test 2", "2023-01-02", "90")
        )

        // Aquí defines la acción al dar click sobre un ítem
        testHistoryAdapter = TestResultAdapter(testResults) { selectedResult ->
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("TEST_JSON", selectedResult.testJson ?: "")
                putExtra("USER_RESPONSES", selectedResult.userResponsesJson ?: "")
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = testHistoryAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tu historial.", Toast.LENGTH_SHORT).show()
            return
        }
    }
}
