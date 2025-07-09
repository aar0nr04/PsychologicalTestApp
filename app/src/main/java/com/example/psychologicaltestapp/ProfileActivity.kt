package com.example.psychologicaltestapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psychologicaltestapp.utils.DialogHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private lateinit var testHistoryRecyclerView: RecyclerView
    private lateinit var testHistoryTitle: TextView
    private lateinit var emptyMessage: TextView

    private lateinit var appointmentRequestsRecyclerView: RecyclerView
    private lateinit var appointmentAdapter: AppointmentRequestAdapter

    private val db = FirebaseFirestore.getInstance()
    private val testResults = mutableListOf<TestResult>()
    private lateinit var adapter: TestHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Referencias a las vistas
        adView = findViewById(R.id.adView)
        testHistoryRecyclerView = findViewById(R.id.testHistoryRecyclerView)
        testHistoryTitle = findViewById(R.id.testHistoryTitle)
        emptyMessage = findViewById(R.id.emptyMessage)
        appointmentRequestsRecyclerView = findViewById(R.id.appointmentRequestsRecyclerView)

        // Inicializar anuncios
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Configurar historial de tests
        adapter = TestHistoryAdapter(testResults) { selectedResult ->
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("TEST_JSON", selectedResult.testJson ?: "")
                putExtra("USER_RESPONSES", selectedResult.userResponsesJson ?: "")
            }
            startActivity(intent)
        }
        testHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        testHistoryRecyclerView.adapter = adapter

        // Configurar citas
        appointmentAdapter = AppointmentRequestAdapter { appointmentRequest ->
            val intent = Intent(this, AppointmentDetailActivity::class.java).apply {
                putExtra("appointmentId", appointmentRequest.id)
                putExtra("psychologistId", appointmentRequest.psychologistId)
            }
            startActivity(intent)
        }
        appointmentRequestsRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentRequestsRecyclerView.adapter = appointmentAdapter

        // Cargar datos
        loadUserAppointments()
        loadTestResults()
    }

    private fun loadUserAppointments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("appointment_requests")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val appointments = result.map { doc ->
                    val appointment = doc.toObject(AppointmentRequest::class.java)
                    appointment.copy(id = doc.id)  // asigna el id del documento aquí
                }
                appointmentAdapter.submitList(appointments)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando citas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTestResults() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .collection("test_results")
            .get()
            .addOnSuccessListener { result ->
                val testResults = result.mapNotNull { it.toObject(TestResult::class.java) }
                if (testResults.isNotEmpty()) {
                    adapter.submitList(testResults)
                    testHistoryTitle.visibility = View.VISIBLE
                    testHistoryRecyclerView.visibility = View.VISIBLE
                    emptyMessage.visibility = View.GONE
                } else {
                    testHistoryTitle.visibility = View.GONE
                    testHistoryRecyclerView.visibility = View.GONE
                    emptyMessage.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar historial", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}
