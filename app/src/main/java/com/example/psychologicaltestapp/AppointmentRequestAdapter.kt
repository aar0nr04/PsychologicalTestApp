package com.example.psychologicaltestapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.psychologicaltestapp.databinding.ItemAppointmentRequestBinding

class AppointmentRequestAdapter(
    private val onItemClick: (AppointmentRequest) -> Unit
) : RecyclerView.Adapter<AppointmentRequestAdapter.RequestViewHolder>() {

    private var items = listOf<AppointmentRequest>()

    fun submitList(newItems: List<AppointmentRequest>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemAppointmentRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class RequestViewHolder(private val binding: ItemAppointmentRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: AppointmentRequest) {
            binding.textViewDate.text = request.proposedDate
            binding.textViewTime.text = request.proposedTime
            binding.textViewStatus.text = request.status.capitalize()
            binding.textViewNotes.text = request.notes

            binding.root.setOnClickListener {
                onItemClick(request)
            }
        }
    }
}
