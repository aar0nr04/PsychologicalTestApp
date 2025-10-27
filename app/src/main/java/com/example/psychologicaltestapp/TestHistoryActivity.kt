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
            TestResult(testName = "Test 1", date = "2023-01-01", score = "85"),
            TestResult(testName = "Test 2", date = "2023-01-02", score = "90")
        )

        testHistoryAdapter = TestResultAdapter { selectedResult ->
            if (selectedResult.testJson.isNullOrBlank() || selectedResult.userResponsesJson.isNullOrBlank()) {
                Toast.makeText(this, "Este resultado no tiene detalles guardados", Toast.LENGTH_SHORT).show()
                return@TestResultAdapter
            }

            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("TEST_PAYLOAD", selectedResult.testJson)
                putExtra("USER_RESPONSES", selectedResult.userResponsesJson)
                putExtra("FINAL_MESSAGE", selectedResult.resultMessage)
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = testHistoryAdapter
        testHistoryAdapter.submitList(testResults)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tu historial.", Toast.LENGTH_SHORT).show()
            return
        }
    }
}
