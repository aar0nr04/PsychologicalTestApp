package com.example.psychologicaltestapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TestsAdapter(private val tests: List<Map<String, Any>>) :
    RecyclerView.Adapter<TestsAdapter.TestViewHolder>() {

    class TestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val testNameTextView: TextView = itemView.findViewById(R.id.testNameTextView)
        val testDateTextView: TextView = itemView.findViewById(R.id.testDateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test_result, parent, false)
        return TestViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        val test = tests[position]
        holder.testNameTextView.text = test["testName"].toString()
        holder.testDateTextView.text = test["date"].toString()
    }

    override fun getItemCount(): Int = tests.size
}