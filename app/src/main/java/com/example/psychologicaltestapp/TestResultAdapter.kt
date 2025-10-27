package com.example.psychologicaltestapp

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TestResultAdapter(
    private val onItemClick: (TestResult) -> Unit
) : RecyclerView.Adapter<TestResultAdapter.ViewHolder>() {

    private val results = mutableListOf<TestResult>()
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val testType: TextView = itemView.findViewById(R.id.testTypeTextView)
        private val testName: TextView = itemView.findViewById(R.id.testNameTextView)
        private val score: TextView = itemView.findViewById(R.id.scoreTextView)
        private val resultMessage: TextView = itemView.findViewById(R.id.resultMessageTextView)
        private val date: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(result: TestResult) {
            testType.text = result.testType.ifBlank { "Test" }
            testName.text = result.testName

            if (result.score.isNotBlank()) {
                score.visibility = View.VISIBLE
                score.text = itemView.context.getString(R.string.test_score_template, result.score)
            } else {
                score.visibility = View.GONE
            }

            if (result.resultMessage.isNotBlank()) {
                resultMessage.visibility = View.VISIBLE
                resultMessage.text = result.resultMessage
                resultMessage.ellipsize = TextUtils.TruncateAt.END
                resultMessage.maxLines = 3
            } else {
                resultMessage.visibility = View.GONE
            }

            val formattedDate = result.createdAt?.toDate()?.let { formatDate(it) }
                ?: result.date
            date.text = formattedDate

            itemView.setOnClickListener { onItemClick(result) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_result, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = results.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(results[position])
    }

    fun submitList(newResults: List<TestResult>) {
        results.clear()
        results.addAll(newResults)
        notifyDataSetChanged()
    }

    private fun formatDate(date: Date): String {
        return dateFormatter.format(date)
    }
}
