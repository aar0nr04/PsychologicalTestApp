package com.example.psychologicaltestapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.psychologicaltestapp.data.profile.UserRepository
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileAppointmentAdapter(
    private val onItemClick: (UserRepository.AppointmentItem) -> Unit
) : RecyclerView.Adapter<ProfileAppointmentAdapter.ViewHolder>() {

    private val items = mutableListOf<UserRepository.AppointmentItem>()
    private val formatter = SimpleDateFormat("EEE dd MMM yyyy, HH:mm", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)

        fun bind(item: UserRepository.AppointmentItem) {
            val formattedDate = item.startTime?.toDate()?.let { formatter.format(it) }
            dateText.text = formattedDate ?: itemView.context.getString(R.string.appointment_date_placeholder)
            nameText.text = item.title ?: itemView.context.getString(R.string.appointment_title_placeholder)
            statusText.text = itemView.context.getString(
                R.string.appointment_status_template,
                item.status?.ifBlank { itemView.context.getString(R.string.appointment_status_unknown) }
                    ?: itemView.context.getString(R.string.appointment_status_unknown)
            )
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.upcoming_appointment_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun submitList(newItems: List<UserRepository.AppointmentItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
