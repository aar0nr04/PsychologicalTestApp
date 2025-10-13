package com.example.psychologicaltestapp.data.profile

data class Consents(
    val analyticsAnon: Boolean = false,
    val reminders: Boolean = true,
    val email: Boolean = true,
    val push: Boolean = true
)

data class PatientPrefs(
    val modalities: List<String> = emptyList(),
    val motives: List<String> = emptyList(),
    val hours: List<String> = emptyList()
)

data class Professional(
    val headline: String? = null,
    val specialties: List<String> = emptyList(),
    val languages: List<String> = listOf("es"),
    val modalities: List<String> = listOf("online"),
    val priceMXN: Int? = null,
    val sessionMinutes: Int? = 50,
    val address: String? = null,
    val isPublicPhone: Boolean = false,
    val isPublicLocation: Boolean = false,
    val availability: Map<String, List<String>> = emptyMap(),
    val isVerified: Boolean = false,
    val acceptingNewPatients: Boolean = true
)

data class UserProfile(
    val role: String = "patient",      // "patient" | "psychologist"
    val name: String = "",
    val photoUrl: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val lang: String = "es",
    val dob: com.google.firebase.Timestamp? = null,
    val gender: String? = null,
    val country: String? = null,
    val state: String? = null,
    val city: String? = null,
    val timeZone: String? = null,
    val consents: Consents = Consents(),
    val prefs: PatientPrefs = PatientPrefs(),
    val professional: Professional? = null
)
