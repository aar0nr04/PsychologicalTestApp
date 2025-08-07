package com.example.psychologicaltestapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.psychologicaltestapp.databinding.ActivityPsychologistDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class PsychologistDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityPsychologistDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPsychologistDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val psychologist = intent.getParcelableExtra<Psychologist>("PSYCHOLOGIST")

        psychologist?.let { psy ->
            binding.nameTextView.text = psy.name
            binding.specialtyTextView.text = psy.specialty
            binding.descriptionTextView.text = psy.description

            Glide.with(this)
                .load(psy.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.profileImageView)

            binding.bookAppointmentButton.setOnClickListener {
                showAppointmentDialog(psy.id)
            }

        } ?: run {
            finish()
        }
    }

    private fun showAppointmentDialog(psychologistId: String) {
        val calendar = Calendar.getInstance()

        // Selección de Fecha
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            // Selección de Hora
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)

                // Aquí puedes pedir notas opcionales con un diálogo si quieres. Por ahora vacío.
                saveAppointmentRequest(psychologistId, selectedDate, selectedTime, "")

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveAppointmentRequest(psychologistId: String, date: String, time: String, notes: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Combine date and time into a single string
        val dateTime = "$date $time" // Example: "2023-10-27 14:30"

        val request = AppointmentRequest(
            userId = userId,
            psychologistId = psychologistId,
            dateTime = dateTime, // Pass the combined string to the 'dateTime' field
            notes = notes,
            status = "pending"
        )

        FirebaseFirestore.getInstance()
            .collection("appointment_requests")
            .add(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitud enviada, esperando respuesta", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al enviar solicitud: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
