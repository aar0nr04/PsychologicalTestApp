package com.example.psychologicaltestapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestHistoryAdapter : RecyclerView.Adapter<TestHistoryAdapter.ViewHolder>() {

    private var testHistory = emptyList<TestResult>()

    fun submitList(newTestHistory: List<TestResult>) {
        testHistory = newTestHistory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val testResult = testHistory[position]
        holder.bind(testResult)
    }

    override fun getItemCount(): Int = testHistory.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val testNameTextView: TextView = itemView.findViewById(R.id.testNameTextView)
        private val resultMessageTextView: TextView = itemView.findViewById(R.id.resultMessageTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(testResult: TestResult) {
            testNameTextView.text = testResult.testName
            resultMessageTextView.text = testResult.resultMessage
            dateTextView.text = testResult.date
        }
    }
}