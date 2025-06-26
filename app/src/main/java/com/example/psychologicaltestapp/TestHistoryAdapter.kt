package com.example.psychologicaltestapp

import TestResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestHistoryAdapter(
    private val testResults: List<TestResult>
) : RecyclerView.Adapter<TestHistoryAdapter.TestResultViewHolder>() {

    // ViewHolder class
    class TestResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val testTypeTextView: TextView = itemView.findViewById(R.id.testTypeTextView)
        val testNameTextView: TextView = itemView.findViewById(R.id.testNameTextView)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
        val resultMessageTextView: TextView = itemView.findViewById(R.id.resultMessageTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }

    // Inflate the layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_result, parent, false)
        return TestResultViewHolder(view)
    }

    // Bind data to the views in each item
    override fun onBindViewHolder(holder: TestResultViewHolder, position: Int) {
        val testResult = testResults[position]
        holder.testTypeTextView.text = testResult.testType
        holder.testNameTextView.text = testResult.testName
        holder.scoreTextView.text = "Score: ${testResult.score}"
        holder.resultMessageTextView.text = testResult.resultMessage
        holder.dateTextView.text = "Date: ${testResult.date}"
    }

    // Return the number of items
    override fun getItemCount(): Int = testResults.size
}