package com.example.psychologicaltestapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.psychologicaltestapp.adapters.AppointmentRequestAdapter
import com.example.psychologicaltestapp.adapters.UpcomingAppointmentsAdapter
import com.example.psychologicaltestapp.adapters.TestHistoryAdapter
import com.example.psychologicaltestapp.models.AppointmentRequest
import om.example.psychologicaltestapp.models.TestResultc
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var adView: AdView
    private lateinit var testHistoryRecyclerView: RecyclerView
    private lateinit var testHistoryHeader: TextView
    private lateinit var emptyMessage: TextView
    private lateinit var appointmentRequestsRecyclerView: RecyclerView
    private lateinit var appointmentAdapter: AppointmentRequestAdapter
    private lateinit var calendarView: CalendarView
    private lateinit var dayScheduleRecyclerView: RecyclerView
    private lateinit var dayScheduleAdapter: AppointmentRequestAdapter
    private lateinit var upcomingAppointmentsRecyclerView: RecyclerView
    private lateinit var noAppointmentsText: TextView
    private var lastY = 0f
    private val db = FirebaseFirestore.getInstance()
    private val testResults = mutableListOf<TestResult>()
    private lateinit var adapter: TestHistoryAdapter
    private var isPsychologist = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        adView = findViewById(R.id.adView)
        testHistoryRecyclerView = findViewById(R.id.testHistoryRecyclerView)
        testHistoryHeader = findViewById(R.id.testHistoryHeader)
        emptyMessage = findViewById(R.id.emptyMessage)
        appointmentRequestsRecyclerView = findViewById(R.id.appointmentRequestsRecyclerView)
        calendarView = findViewById(R.id.calendarView)
        dayScheduleRecyclerView = findViewById(R.id.dayScheduleRecyclerView)
        upcomingAppointmentsRecyclerView = findViewById(R.id.upcomingAppointmentsRecyclerView)
        noAppointmentsText = findViewById(R.id.noAppointmentsText)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adapter = TestHistoryAdapter(testResults) { selectedResult ->
            val testJson = selectedResult.testJson
            val responsesJson = selectedResult.userResponsesJson

            if (!testJson.isNullOrBlank() && !responsesJson.isNullOrBlank()) {
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra("TEST_JSON", testJson)
                    putExtra("USER_RESPONSES", responsesJson)
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Este test no contiene información completa para visualizarlo.", Toast.LENGTH_LONG).show()
            }
        }
        testHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        testHistoryRecyclerView.adapter = adapter

        appointmentAdapter = AppointmentRequestAdapter { appointmentRequest ->
            val intent = Intent(this, AppointmentDetailActivity::class.java).apply {
                putExtra("appointmentId", appointmentRequest.id)
                putExtra("psychologistId", appointmentRequest.psychologistId)
            }
            startActivity(intent)
        }
        appointmentRequestsRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentRequestsRecyclerView.adapter = appointmentAdapter

        dayScheduleAdapter = AppointmentRequestAdapter { appointmentRequest ->
            val intent = Intent(this, AppointmentDetailActivity::class.java).apply {
                putExtra("appointmentId", appointmentRequest.id)
                putExtra("psychologistId", appointmentRequest.psychologistId)
            }
            startActivity(intent)
        }
        dayScheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        dayScheduleRecyclerView.adapter = dayScheduleAdapter

        upcomingAppointmentsRecyclerView.layoutManager = LinearLayoutManager(this)

        fetchUserRole { role ->
            isPsychologist = role == "psychologist"
            if (isPsychologist) {
                calendarView.visibility = View.VISIBLE
                dayScheduleRecyclerView.visibility = View.VISIBLE
                testHistoryRecyclerView.visibility = View.GONE
                testHistoryHeader.visibility = View.GONE
                emptyMessage.visibility = View.GONE

                calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                    val dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    loadAppointmentsForPsychologistOnDate(dateStr)
                }

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                loadAppointmentsForPsychologistOnDate(today)
            } else {
                calendarView.visibility = View.GONE
                dayScheduleRecyclerView.visibility = View.GONE
                loadUserAppointments()
                loadTestResults()
            }

            loadUpcomingAppointments()
        }
    }

    private fun loadUserAppointments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("appointment_requests")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val appointments = result.map { doc ->
                    val appointment = doc.toObject(AppointmentRequest::class.java)
                    appointment.copy(id = doc.id)
                }
                appointmentAdapter.submitList(appointments)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando citas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUpcomingAppointments() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("appointment_requests")
            .get()
            .addOnSuccessListener { result ->
                val today = LocalDate.now()
                val appointments = result.mapNotNull { doc ->
                    val appointment = doc.toObject(AppointmentRequest::class.java)
                    val apptDate = runCatching { LocalDate.parse(appointment.proposedDate) }.getOrNull()
                    if (apptDate != null && apptDate >= today) {
                        if ((isPsychologist && appointment.psychologistId == userId) ||
                            (!isPsychologist && appointment.userId == userId)) {
                            appointment.copy(id = doc.id)
                        } else null
                    } else null
                }.sortedBy { it.proposedDate + it.proposedTime }.take(5)

                if (appointments.isEmpty()) {
                    noAppointmentsText.visibility = View.VISIBLE
                } else {
                    noAppointmentsText.visibility = View.GONE
                    upcomingAppointmentsRecyclerView.adapter = UpcomingAppointmentsAdapter(appointments)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando próximas citas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTestResults() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userRepository = UserRepository()

        userRepository.getTestResultsForUser(currentUser.uid) { results ->
            runOnUiThread {
                if (results.isNotEmpty()) {
                    adapter.submitList(results)
                    testHistoryHeader.visibility = View.VISIBLE
                    testHistoryRecyclerView.visibility = View.VISIBLE
                    emptyMessage.visibility = View.GONE
                } else {
                    testHistoryHeader.visibility = View.GONE
                    testHistoryRecyclerView.visibility = View.GONE
                    emptyMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun fetchUserRole(onRoleReady: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val role = doc.getString("role") ?: "patient"
                onRoleReady(role)
            }
            .addOnFailureListener {
                onRoleReady("patient")
            }
    }

    private fun loadAppointmentsForPsychologistOnDate(date: String) {
        val psychologistId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("appointment_requests")
            .whereEqualTo("psychologistId", psychologistId)
            .whereEqualTo("proposedDate", date)
            .get()
            .addOnSuccessListener { result ->
                val appointments = result.map { doc ->
                    val appointment = doc.toObject(AppointmentRequest::class.java)
                    appointment.copy(id = doc.id)
                }
                dayScheduleAdapter.submitList(appointments)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error cargando agenda del día: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> lastY = event.y
            MotionEvent.ACTION_UP -> {
                val deltaY = event.y - lastY
                val topBar = findViewById<View>(R.id.topBar)

                if (deltaY > 100) {
                    topBar.visibility = View.VISIBLE
                } else if (deltaY < -100) {
                    topBar.visibility = View.GONE
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
