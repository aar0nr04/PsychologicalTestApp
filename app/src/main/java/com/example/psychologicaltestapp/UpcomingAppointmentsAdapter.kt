package com.example.psychologicaltestapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class UpcomingAppointmentsAdapter(private val appointments: List<AppointmentRequest>) : RecyclerView.Adapter<UpcomingAppointmentsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.dateText)
        val detailText: TextView = view.findViewById(R.id.nameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.upcoming_appointment_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = appointments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.dateText.text = "${appointment.date} - ${appointment.time}"
        holder.detailText.text = appointment.notes.ifBlank { "Cita agendada" }
    }
}
