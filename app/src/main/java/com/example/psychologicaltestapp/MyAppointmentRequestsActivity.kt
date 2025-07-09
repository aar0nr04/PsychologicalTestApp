package com.example.psychologicaltestapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.psychologicaltestapp.databinding.ActivityMyAppointmentRequestsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyAppointmentRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyAppointmentRequestsBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: AppointmentRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyAppointmentRequestsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AppointmentRequestAdapter { appointmentRequest ->
            // Aquí defines qué pasa al hacer click en un ítem
            // Por ejemplo, abrir detalle:
            val intent = Intent(this, AppointmentDetailActivity::class.java).apply {
                putExtra("appointmentId", appointmentRequest.id)
            }
            startActivity(intent)
        }
        binding.recyclerViewRequests.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewRequests.adapter = adapter

        fetchUserRequests()
    }

    private fun fetchUserRequests() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("appointment_requests")
            .whereEqualTo("userId", userId)
            .orderBy("proposedDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val requests = snapshot.toObjects(AppointmentRequest::class.java)
                    adapter.submitList(requests)
                }
            }
    }
}
