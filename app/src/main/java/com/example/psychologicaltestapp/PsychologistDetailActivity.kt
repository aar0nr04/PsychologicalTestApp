package com.example.psychologicaltestapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.psychologicaltestapp.databinding.ActivityPsychologistDetailBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

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
                showAppointmentDialog(psy)
            }

        } ?: run {
            finish()
        }
    }

    private fun showAppointmentDialog(psychologist: Psychologist) {
        val calendar = Calendar.getInstance()

        // Selección de Fecha
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)

            // Selección de Hora
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)

                // Aquí puedes pedir notas opcionales con un diálogo si quieres. Por ahora vacío.
                saveAppointmentRequest(psychologist, selectedDate, selectedTime, "")

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveAppointmentRequest(psychologist: Psychologist, date: String, time: String, notes: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Combine date and time into a single string
        val dateTime = "$date $time" // Example: "2023-10-27 14:30"

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val parsedStart = runCatching { formatter.parse(dateTime) }.getOrNull()?.let { Timestamp(it) }

        val requestData = hashMapOf(
            "userId" to userId,
            "psychologistId" to psychologist.id,
            "psychologistName" to psychologist.name,
            "status" to "pending",
            "proposedDate" to date,
            "proposedTime" to time,
            "dateTime" to dateTime,
            "notes" to notes,
            "createdAt" to System.currentTimeMillis()
        )

        parsedStart?.let { requestData["startTime"] = it }

        val db = FirebaseFirestore.getInstance()
        db.collection("appointments")
            .add(requestData)
            .addOnSuccessListener {
                Toast.makeText(this, "Solicitud enviada, esperando respuesta", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { error ->
                saveAppointmentFallback(db, userId, requestData, error)
            }
    }

    private fun saveAppointmentFallback(
        db: FirebaseFirestore,
        userId: String,
        requestData: HashMap<String, Any>,
        originalError: Exception
    ) {
        db.collection("appointment_requests")
            .add(requestData)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.appointment_saved_limited_message),
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { secondaryError ->
                db.collection("users")
                    .document(userId)
                    .collection("appointments")
                    .add(requestData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            getString(R.string.appointment_saved_offline_message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            getString(R.string.appointment_error_with_reason, secondaryError.message ?: originalError.message ?: "Error desconocido"),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
    }
}
