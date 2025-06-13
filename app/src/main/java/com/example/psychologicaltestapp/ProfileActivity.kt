package com.example.psychologicaltestapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psychologicaltestapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize the repository
        userRepository = UserRepository()

        // Get the current user ID
        val userId = userRepository.getCurrentUserId()

        // Fetch and display test results
        userRepository.getTestResultsForUser(userId) { testResults ->
            displayTestResults(testResults)
        }
    }

    private fun displayTestResults(results: List<TestResult>) {
        // Find the RecyclerView or TextView where results will be displayed
        val resultsRecyclerView = findViewById<RecyclerView>(R.id.resultsRecyclerView)
        val adapter = TestHistoryAdapter(results) // Use an adapter to display the results
        resultsRecyclerView.adapter = adapter
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}