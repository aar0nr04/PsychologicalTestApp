package com.example.psychologicaltestapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestResultAdapter(
    private val results: List<TestResult>,
    private val onItemClick: (TestResult) -> Unit
) : RecyclerView.Adapter<TestResultAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val testType: TextView = itemView.findViewById(R.id.testTypeTextView)
        val testName: TextView = itemView.findViewById(R.id.testNameTextView)
        val score: TextView = itemView.findViewById(R.id.scoreTextView)
        val resultMessage: TextView = itemView.findViewById(R.id.resultMessageTextView)
        val date: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_result, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = results.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.testType.text = result.testType
        holder.testName.text = result.testName
        holder.score.text = "Puntuación: ${result.score}"
        holder.resultMessage.text = result.resultMessage
        holder.date.text = "Fecha: ${result.date}"

        holder.itemView.setOnClickListener {
            onItemClick(result)
        }
    }
}
