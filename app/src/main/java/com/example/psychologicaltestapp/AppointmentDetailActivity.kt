package com.example.psychologicaltestapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppointmentDetailActivity : BaseActivity() {

    private lateinit var psychologistPhoto: ImageView
    private lateinit var psychologistName: TextView
    private lateinit var appointmentStatus: TextView
    private lateinit var appointmentDetails: TextView
    private lateinit var buttonSendMessage: Button
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendMessageButton: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var appointmentId: String
    private lateinit var psychologistId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_detail)

        // Inicializar AdMob
        MobileAds.initialize(this) {}
        val topAdView: AdView = findViewById(R.id.topAdView)
        val bottomAdView: AdView = findViewById(R.id.bottomAdView)
        val adRequest = AdRequest.Builder().build()

        topAdView.loadAd(adRequest)
        bottomAdView.loadAd(adRequest)

        psychologistPhoto = findViewById(R.id.psychologistPhoto)
        psychologistName = findViewById(R.id.psychologistName)
        appointmentStatus = findViewById(R.id.appointmentStatus)
        appointmentDetails = findViewById(R.id.appointmentDetails)
        buttonSendMessage = findViewById(R.id.buttonSendMessage)
        messageEditText = findViewById(R.id.messageEditText)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        sendMessageButton = findViewById(R.id.sendMessageButton)

        appointmentId = intent.getStringExtra("appointmentId") ?: return
        psychologistId = intent.getStringExtra("psychologistId") ?: return

        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        loadPsychologistData(psychologistId)
        loadAppointmentData(appointmentId)
        loadChatMessages(appointmentId)

        sendMessageButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isEmpty()) {
                Toast.makeText(this, "Escribe un mensaje primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val messageData = hashMapOf<String, Any>(
                "fromUserId" to currentUser.uid,
                "toPsychologistId" to psychologistId,
                "appointmentId" to appointmentId,
                "messageText" to messageText,
                "timestamp" to Timestamp.now()
            )

            db.collection("chats").add(messageData)
                .addOnSuccessListener {
                    messageEditText.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al enviar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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

    private fun loadChatMessages(appointmentId: String) {
        db.collection("chats")
            .whereEqualTo("appointmentId", appointmentId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val messages = snapshot.toObjects(ChatMessage::class.java)
                chatRecyclerView.adapter = ChatAdapter(messages)
            }
    }
}
