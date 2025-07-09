package com.example.psychologicaltestapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.psychologicaltestapp.utils.DialogHelper

class ProfileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adView: AdView

    private val testResults = mutableListOf<TestResult>()
    private lateinit var adapter: TestHistoryAdapter

    private lateinit var appointmentRequestsRecyclerView: RecyclerView
    private lateinit var appointmentAdapter: AppointmentRequestAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        recyclerView = findViewById(R.id.testHistoryRecyclerView)
        adView = findViewById(R.id.adView)

        appointmentRequestsRecyclerView = findViewById(R.id.appointmentRequestsRecyclerView)
        appointmentAdapter = AppointmentRequestAdapter()
        appointmentRequestsRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentRequestsRecyclerView.adapter = appointmentAdapter

        loadUserAppointments()  // llamas a tu función para obtener citas


        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adapter = TestHistoryAdapter(testResults) { selectedResult ->
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("TEST_JSON", selectedResult.testJson ?: "")
                putExtra("USER_RESPONSES", selectedResult.userResponsesJson ?: "")
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadTestResults()
    }
    private fun loadUserAppointments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("appointment_requests")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val appointments = result.map { it.toObject(AppointmentRequest::class.java) }
                appointmentAdapter.submitList(appointments)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando citas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadTestResults() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(currentUser.uid)
            .collection("testResults")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                testResults.clear()
                for (doc in documents) {
                    val testResult = doc.toObject(TestResult::class.java)
                    testResults.add(testResult)
                }
                adapter.notifyDataSetChanged()

                // Mostrar u ocultar mensaje vacío
                findViewById<TextView>(R.id.emptyMessage).visibility =
                    if (testResults.isEmpty()) TextView.VISIBLE else TextView.GONE
            }
            .addOnFailureListener { exception ->
                showErrorDialog("Error al cargar resultados: ${exception.message}")
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
