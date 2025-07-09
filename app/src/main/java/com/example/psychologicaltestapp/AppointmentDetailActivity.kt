package com.example.psychologicaltestapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentDetailActivity : AppCompatActivity() {

    private lateinit var psychologistPhoto: ImageView
    private lateinit var psychologistName: TextView
    private lateinit var appointmentStatus: TextView
    private lateinit var appointmentDetails: TextView
    private lateinit var buttonSendMessage: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_detail)

        psychologistPhoto = findViewById(R.id.psychologistPhoto)
        psychologistName = findViewById(R.id.psychologistName)
        appointmentStatus = findViewById(R.id.appointmentStatus)
        appointmentDetails = findViewById(R.id.appointmentDetails)
        buttonSendMessage = findViewById(R.id.buttonSendMessage)

        // Recibir el ID de la cita y psicólogo
        val appointmentId = intent.getStringExtra("appointmentId") ?: return
        val psychologistId = intent.getStringExtra("psychologistId") ?: return

        loadPsychologistData(psychologistId)
        loadAppointmentData(appointmentId)

        buttonSendMessage.setOnClickListener {
            // Aquí puedes abrir el chat o enviar mensaje, lo vemos luego
        }
    }

    private fun loadPsychologistData(psychologistId: String) {
        db.collection("psychologists").document(psychologistId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    psychologistName.text = doc.getString("name") ?: "Psicólogo"
                    val photoUrl = doc.getString("photoUrl")
                    if (!photoUrl.isNullOrEmpty()) {
                        Glide.with(this).load(photoUrl).into(psychologistPhoto)
                    }
                }
            }
            .addOnFailureListener {
                psychologistName.text = "Psicólogo"
            }
    }

    private fun loadAppointmentData(appointmentId: String) {
        db.collection("appointment_requests").document(appointmentId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val status = doc.getString("status") ?: "Desconocido"
                    val date = doc.getString("proposedDate") ?: "Fecha no disponible"
                    val time = doc.getString("proposedTime") ?: "Hora no disponible"
                    val notes = doc.getString("notes") ?: "Sin notas"

                    appointmentStatus.text = "Estado: $status"
                    appointmentDetails.text = "Fecha: $date\nHora: $time\nNotas: $notes"
                }
            }
            .addOnFailureListener {
                appointmentStatus.text = "Estado: Desconocido"
                appointmentDetails.text = "Error al cargar detalles"
            }
    }
}
