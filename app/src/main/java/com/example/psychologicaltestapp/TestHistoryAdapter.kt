package com.example.psychologicaltestapp
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestHistoryAdapter(
    private var testResults: List<TestResult>,
    private val onItemClick: (TestResult) -> Unit
) : RecyclerView.Adapter<TestHistoryAdapter.TestHistoryViewHolder>() {

    inner class TestHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resultTextView: TextView = itemView.findViewById(R.id.resultTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_history, parent, false)
        return TestHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestHistoryViewHolder, position: Int) {
        val testResult = testResults[position]

        // Mostrar solo resumen corto (ejemplo 150 caracteres)
        val fullMessage = testResult.resultMessage ?: ""
        val summary = if (fullMessage.length > 150) fullMessage.substring(0, 150) + "..." else fullMessage

        holder.resultTextView.text = summary

        holder.itemView.setOnClickListener {
            onItemClick(testResult)
        }
    }

    override fun getItemCount(): Int = testResults.size

    fun submitList(newList: List<TestResult>) {
        testResults = newList
        notifyDataSetChanged()
    }
}

