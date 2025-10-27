package com.example.psychologicaltestapp

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.psychologicaltestapp.data.profile.UserRepository
import com.example.psychologicaltestapp.databinding.ActivityProfileBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val repo = UserRepository()
    private lateinit var testResultAdapter: TestResultAdapter
    private lateinit var appointmentAdapter: ProfileAppointmentAdapter

    private var currentDob: Timestamp? = null
    private var currentRole: String = "patient"

    // para foto perfil de ProfileActivity:
    private val storage by lazy { FirebaseStorage.getInstance() }
    private var pickedImageUri: Uri? = null
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedImageUri = uri
            // Preview inmediata
            Glide.with(this).load(uri).into(binding.imgAvatar)
            // Sube y guarda
            uploadAvatarToStorage(uri)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyWindowInsets()
        setupAdMob()

        setupRoleDropdown()
        setupDobPicker()
        setupSections()
        setupRecyclerViews()
        loadProfile()
        loadTestResults()
        loadAppointments()

        binding.btnSave.setOnClickListener { saveProfile() }
    }

    // --- UI helpers ---

    /** Empuja el banner por debajo del status bar para que no quede pegado */
    private fun applyWindowInsets() {
        val root = binding.rootProfile
        val banner = binding.bannerImage
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            banner.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = sb.top + dp(16)
            }
            insets
        }
    }

    private fun setupAdMob() {
        MobileAds.initialize(this)
        val adView: AdView = binding.adView
        adView.loadAd(AdRequest.Builder().build())
    }

    private fun setupRoleDropdown() {
        val roles = arrayOf("patient", "psychologist")
        binding.ddRole.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, roles))
        binding.ddRole.setOnItemClickListener { _, _, pos, _ ->
            currentRole = roles[pos]
            showProfessionalSection(currentRole == "psychologist")
        }
    }

    private fun setupDobPicker() {
        binding.btnPickDob.setOnClickListener {
            val cal = Calendar.getInstance()
            val dlg = DatePickerDialog(
                this,
                { _, y, m, d ->
                    cal.set(y, m, d, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
                    currentDob = Timestamp(cal.time)
                    binding.tvAge.text = "Edad: ${calcAge(cal)}"
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            )
            dlg.show()
        }
    }

    private fun calcAge(cal: Calendar): Int {
        val now = Calendar.getInstance()
        var age = now.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
        if (now.get(Calendar.DAY_OF_YEAR) < cal.get(Calendar.DAY_OF_YEAR)) age--
        return age
    }

    private fun showProfessionalSection(show: Boolean) {
        binding.cardProfessional.isVisible = show
    }

    // --- Load / Save ---

    private fun loadProfile() {
        repo.load(onSuccess = { data ->
            // Básicos
            val firebaseUser = FirebaseAuth.getInstance().currentUser

            val fallbackName = firebaseUser?.displayName
            val fallbackEmail = firebaseUser?.email
            val fallbackPhone = firebaseUser?.phoneNumber

            binding.etName.setText((data["name"] as? String) ?: fallbackName ?: "")
            binding.etEmail.setText((data["email"] as? String) ?: fallbackEmail ?: "")
            binding.etPhone.setText((data["phone"] as? String) ?: fallbackPhone ?: "")
            binding.etCountry.setText((data["country"] as? String) ?: "MX")
            binding.etState.setText(data["state"] as? String ?: "")
            binding.etCity.setText(data["city"] as? String ?: "")
            binding.etTimeZone.setText(data["timeZone"] as? String ?: "")

            currentRole = (data["role"] as? String) ?: "patient"
            binding.ddRole.setText(currentRole, false)
            showProfessionalSection(currentRole == "psychologist")

            // DOB
            (data["dob"] as? Timestamp)?.let {
                currentDob = it
                val cal = Calendar.getInstance().apply { time = it.toDate() }
                binding.tvAge.text = "Edad: ${calcAge(cal)}"
            }

            (data["photoUrl"] as? String)?.takeIf { it.isNotBlank() }?.let {
                Glide.with(this).load(it).into(binding.imgAvatar)
            }

            // Consentimientos
            val consents = (data["consents"] as? Map<*, *>) ?: emptyMap<String, Any>()
            binding.swReminders.isChecked = (consents["reminders"] as? Boolean) ?: true
            binding.swAnalytics.isChecked = (consents["analyticsAnon"] as? Boolean) ?: false

            // Profesional (si hay)
            val pro = data["professional"] as? Map<*, *>
            if (pro != null) {
                binding.etHeadline.setText(pro["headline"] as? String ?: "")
                binding.etSpecialties.setText((pro["specialties"] as? List<*>)?.joinToString(",") ?: "")
                binding.etProLanguages.setText((pro["languages"] as? List<*>)?.joinToString(",") ?: "es")
                binding.etModalities.setText((pro["modalities"] as? List<*>)?.joinToString(",") ?: "online,presencial")
                binding.etPrice.setText(((pro["priceMXN"] as? Number)?.toInt() ?: 0).toString())
                binding.etSessionMinutes.setText(((pro["sessionMinutes"] as? Number)?.toInt() ?: 50).toString())
                binding.etAddress.setText(pro["address"] as? String ?: "")
                binding.swPublicPhone.isChecked = (pro["isPublicPhone"] as? Boolean) ?: false
                binding.swPublicLocation.isChecked = (pro["isPublicLocation"] as? Boolean) ?: false
                binding.swAccepting.isChecked = (pro["acceptingNewPatients"] as? Boolean) ?: true

                // availability -> textos por día
                val av = (pro["availability"] as? Map<*, *>)?.mapKeys { it.key.toString() } ?: emptyMap()
                binding.etMon.setText((av["mon"] as? List<*>)?.joinToString(",") ?: "")
                binding.etTue.setText((av["tue"] as? List<*>)?.joinToString(",") ?: "")
                binding.etWed.setText((av["wed"] as? List<*>)?.joinToString(",") ?: "")
                binding.etThu.setText((av["thu"] as? List<*>)?.joinToString(",") ?: "")
                binding.etFri.setText((av["fri"] as? List<*>)?.joinToString(",") ?: "")
                binding.etSat.setText((av["sat"] as? List<*>)?.joinToString(",") ?: "")
                binding.etSun.setText((av["sun"] as? List<*>)?.joinToString(",") ?: "")
            }
        }, onError = {
            Toast.makeText(this, "Error cargando perfil: ${it.message}", Toast.LENGTH_LONG).show()
        })

        // Clicks para cambiar foto
        binding.imgAvatar.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.fabChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }

    }

    private fun loadTestResults() {
        val uid = runCatching { repo.getCurrentUserId() }.getOrElse { return }
        repo.getTestResultsForUser(uid, limit = 20) { results ->
            if (results.isEmpty()) {
                binding.tvResultsEmpty.isVisible = true
                testResultAdapter.submitList(emptyList())
            } else {
                binding.tvResultsEmpty.isVisible = false
                testResultAdapter.submitList(results)
            }
        }
    }

    private fun loadAppointments() {
        repo.loadAppointments(limit = 20, onSuccess = { items ->
            if (items.isEmpty()) {
                binding.tvAppointmentsEmpty.isVisible = true
                appointmentAdapter.submitList(emptyList())
            } else {
                binding.tvAppointmentsEmpty.isVisible = false
                appointmentAdapter.submitList(items)
            }
        }, onError = {
            Toast.makeText(this, "Error cargando citas: ${it.message}", Toast.LENGTH_LONG).show()
            binding.tvAppointmentsEmpty.isVisible = true
        })
    }

    private fun saveProfile() {
        // Validaciones mínimas
        val tz = binding.etTimeZone.text.toString().trim()
        if (tz.isEmpty()) {
            Toast.makeText(this, "Agrega zona horaria (ej. America/Chihuahua)", Toast.LENGTH_SHORT).show()
            return
        }
        currentDob?.let {
            val age = calcAge(Calendar.getInstance().apply { time = it.toDate() })
            if (age < 6 || age > 120) {
                Toast.makeText(this, "Revisa la fecha de nacimiento (edad inválida).", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Consentimientos
        val consents = mapOf(
            "reminders" to binding.swReminders.isChecked,
            "analyticsAnon" to binding.swAnalytics.isChecked
        )

        val patch = mutableMapOf<String, Any?>(
            "name" to binding.etName.text.toString().trim(),
            "phone" to binding.etPhone.text.toString().trim(),
            "country" to binding.etCountry.text.toString().uppercase(Locale.US),
            "state" to binding.etState.text.toString().trim(),
            "city" to binding.etCity.text.toString().trim(),
            "timeZone" to tz,
            "role" to currentRole,
            "consents" to consents
        )
        currentDob?.let { patch["dob"] = it }

        // Si es psicólogo, empaquetamos professional
        if (currentRole == "psychologist") {
            fun splitList(text: String) = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val availability = mapOf(
                "mon" to splitList(binding.etMon.text.toString()),
                "tue" to splitList(binding.etTue.text.toString()),
                "wed" to splitList(binding.etWed.text.toString()),
                "thu" to splitList(binding.etThu.text.toString()),
                "fri" to splitList(binding.etFri.text.toString()),
                "sat" to splitList(binding.etSat.text.toString()),
                "sun" to splitList(binding.etSun.text.toString()),
            )
            val pro = mapOf(
                "headline" to binding.etHeadline.text.toString().trim(),
                "specialties" to splitList(binding.etSpecialties.text.toString()),
                "languages" to splitList(binding.etProLanguages.text.toString().ifBlank { "es" }),
                "modalities" to splitList(binding.etModalities.text.toString().ifBlank { "online,presencial" }),
                "priceMXN" to binding.etPrice.text.toString().toIntOrNull(),
                "sessionMinutes" to binding.etSessionMinutes.text.toString().toIntOrNull(),
                "address" to binding.etAddress.text.toString().trim(),
                "isPublicPhone" to binding.swPublicPhone.isChecked,
                "isPublicLocation" to binding.swPublicLocation.isChecked,
                "acceptingNewPatients" to binding.swAccepting.isChecked,
                "availability" to availability
            )
            patch["professional"] = pro
        } else {
            patch["professional"] = null
        }

        repo.savePatch(patch,
            onSuccess = {
                Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show()
                finish()
            },
            onError = {
                Toast.makeText(this, "Error al guardar: ${it.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy()
    }

    private fun setupSections() {
        setupExpandableSection(
            header = binding.sectionPersonalHeader,
            content = binding.sectionPersonalContent,
            icon = binding.iconPersonalToggle
        )
        setupExpandableSection(
            header = binding.sectionPreferencesHeader,
            content = binding.sectionPreferencesContent,
            icon = binding.iconPreferencesToggle
        )
        setupExpandableSection(
            header = binding.sectionResultsHeader,
            content = binding.sectionResultsContent,
            icon = binding.iconResultsToggle
        )
        setupExpandableSection(
            header = binding.sectionAppointmentsHeader,
            content = binding.sectionAppointmentsContent,
            icon = binding.iconAppointmentsToggle
        )
        setupExpandableSection(
            header = binding.sectionProfessionalHeader,
            content = binding.sectionProfessionalContent,
            icon = binding.iconProfessionalToggle
        )
    }

    private fun setupExpandableSection(header: android.view.View, content: android.view.View, icon: android.widget.ImageView, defaultExpanded: Boolean = true) {
        content.isVisible = defaultExpanded
        icon.rotation = if (defaultExpanded) 0f else -90f
        header.setOnClickListener {
            val expanding = !content.isVisible
            content.isVisible = expanding
            icon.animate().rotation(if (expanding) 0f else -90f).setDuration(200).start()
        }
    }

    private fun setupRecyclerViews() {
        testResultAdapter = TestResultAdapter { openTestResult(it) }
        binding.recyclerTestResults.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = testResultAdapter
        }

        appointmentAdapter = ProfileAppointmentAdapter { openAppointment(it) }
        binding.recyclerAppointments.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = appointmentAdapter
        }
    }

    private fun openTestResult(result: TestResult) {
        if (result.testJson.isNullOrBlank() || result.userResponsesJson.isNullOrBlank()) {
            Toast.makeText(this, "No se encontró la información completa del resultado", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("TEST_PAYLOAD", result.testJson)
            putExtra("USER_RESPONSES", result.userResponsesJson)
            putExtra("FINAL_MESSAGE", result.resultMessage)
        }
        startActivity(intent)
    }

    private fun openAppointment(item: UserRepository.AppointmentItem) {
        if (item.psychologistId.isNullOrBlank()) {
            Toast.makeText(this, "No pudimos abrir el detalle de la cita", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, AppointmentDetailActivity::class.java).apply {
            putExtra("appointmentId", item.id)
            putExtra("psychologistId", item.psychologistId)
        }
        startActivity(intent)
    }
    private fun uploadAvatarToStorage(uri: Uri) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = storage.reference.child("users/$uid/profile.jpg")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    val patch = mapOf("photoUrl" to downloadUri.toString())
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(uid)
                        .set(patch, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "No se pudo guardar la foto: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error subiendo foto: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
