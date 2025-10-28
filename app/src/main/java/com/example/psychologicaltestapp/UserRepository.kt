package com.example.psychologicaltestapp

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ===== Util =====
    fun getCurrentUserId(): String {
        val uid = auth.currentUser?.uid
        return uid ?: throw IllegalStateException("No user is currently logged in")
    }

    // ===== Perfil (para ProfileActivity) =====

    /** Carga el documento del usuario actual (map crudo). */
    fun load(
        onSuccess: (Map<String, Any>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = getCurrentUserId()
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                onSuccess(doc.data ?: emptyMap())
            }
            .addOnFailureListener(onError)
    }

    /**
     * Hace merge de campos en el doc del usuario actual.
     * Acepta nulls: si un valor es null se borra el campo.
     */
    fun savePatch(
        patch: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = getCurrentUserId()
        val sanitized = patch.mapValues { (_, v) -> v ?: FieldValue.delete() }
        firestore.collection("users").document(uid)
            .set(sanitized, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    // ===== Citas (tarjeta "Próximas citas") =====

    enum class AppointmentSource {
        GLOBAL,
        REQUESTS,
        LEGACY
    }

    data class AppointmentItem(
        val id: String,
        val title: String?,
        val startTime: Timestamp?,
        val status: String?,
        val psychologistId: String?,
        val startTimeText: String? = null,
        val source: AppointmentSource = AppointmentSource.GLOBAL
    )

    /**
     * Lee próximas N citas del usuario.
     * Consulta primero la colección de solicitudes "appointment_requests" filtrando por userId
     * y, si no hay resultados o falla por permisos/campos faltantes, cae a la subcolección legacy
     * `users/{uid}/appointments`.
     */
    fun loadAppointments(
        limit: Long = 5,
        onSuccess: (List<AppointmentItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = getCurrentUserId()
        firestore.collection("appointments")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { qs ->
                val mapped = qs.documents.mapNotNull { mapAppointmentDocument(it, AppointmentSource.GLOBAL) }
                if (mapped.isNotEmpty()) {
                    onSuccess(sortAppointments(mapped))
                } else {
                    loadAppointmentRequests(uid, limit, onSuccess, onError)
                }
            }
            .addOnFailureListener { error ->
                loadAppointmentRequests(uid, limit, onSuccess) { _ ->
                    onError(error)
                }
            }
    }

    private fun loadAppointmentRequests(
        uid: String,
        limit: Long,
        onSuccess: (List<AppointmentItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("appointment_requests")
            .whereEqualTo("userId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { qs ->
                val mapped = qs.documents.mapNotNull { mapAppointmentDocument(it, AppointmentSource.REQUESTS) }
                if (mapped.isNotEmpty()) {
                    onSuccess(sortAppointments(mapped))
                } else {
                    loadLegacyAppointments(uid, limit, onSuccess, onError)
                }
            }
            .addOnFailureListener { error ->
                loadLegacyAppointments(uid, limit, onSuccess) { _ ->
                    onError(error)
                }
            }
    }

    private fun loadLegacyAppointments(
        uid: String,
        limit: Long,
        onSuccess: (List<AppointmentItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val now = Timestamp.now()
        firestore.collection("users").document(uid)
            .collection("appointments")
            .whereGreaterThan("startTime", now)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { sub ->
                val legacyItems = sub.documents.mapNotNull { mapAppointmentDocument(it, AppointmentSource.LEGACY) }
                onSuccess(sortAppointments(legacyItems))
            }
            .addOnFailureListener(onError)
    }

    private fun mapAppointmentDocument(
        document: com.google.firebase.firestore.DocumentSnapshot,
        source: AppointmentSource
    ): AppointmentItem? {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        val explicitTimestamp = document.getTimestamp("startTime")
        val composedDateTime = document.getString("dateTime")
            ?: listOfNotNull(
                document.getString("proposedDate"),
                document.getString("proposedTime")
            ).takeIf { it.isNotEmpty() }?.joinToString(separator = " ")

        val parsedTimestamp = if (explicitTimestamp == null && !composedDateTime.isNullOrBlank()) {
            runCatching {
                formatter.parse(composedDateTime)?.let { Timestamp(it) }
            }.getOrNull()
        } else {
            explicitTimestamp
        }

        val psychologistId = document.getString("psychologistId")
            ?: document.getString("providerId")
            ?: document.getString("psychologistUid")

        val displayTitle = document.getString("title")
            ?: document.getString("psychologistName")
            ?: document.getString("providerName")
            ?: "Cita"

        val fallbackText = composedDateTime
            ?: document.getString("startTimeText")
            ?: document.getString("formattedStart")
            ?: document.getString("startLabel")

        return AppointmentItem(
            id = document.id,
            title = displayTitle,
            startTime = parsedTimestamp,
            status = document.getString("status")
                ?: document.getString("state"),
            psychologistId = psychologistId,
            startTimeText = fallbackText,
            source = source
        )
    }

    private fun sortAppointments(items: List<AppointmentItem>): List<AppointmentItem> {
        return items.sortedWith(
            compareBy<AppointmentItem> {
                it.startTime?.seconds ?: Long.MAX_VALUE
            }.thenBy {
                it.startTimeText ?: "zzzz"
            }
        )
    }

    // ===== Historial de tests =====

    /**
     * Guarda un resultado de test en la ruta nueva `testResults` (camelCase).
     * Si quieres seguir usando la vieja `test_results`, activa alsoLegacy=true.
     */
    fun saveTestResult(
        userId: String,
        testResult: TestResult,
        alsoLegacy: Boolean = true,
        onComplete: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val newColl = firestore.collection("users")
            .document(userId)
            .collection("testResults")

        val opNew = newColl.add(testResult)

        if (!alsoLegacy) {
            opNew.addOnSuccessListener { onComplete?.invoke() }
                .addOnFailureListener { e -> onError?.invoke(e) }
            return
        }

        // También guarda en la legacy `test_results`
        val legacyColl = firestore.collection("users")
            .document(userId)
            .collection("test_results")

        opNew
            .continueWithTask { legacyColl.add(testResult) }
            .addOnSuccessListener { onComplete?.invoke() }
            .addOnFailureListener { e -> onError?.invoke(e) }
    }

    /**
     * Obtiene resultados de test del usuario. Lee primero `testResults` (nuevo),
     * si está vacío, hace fallback a `test_results` (legacy).
     */
    fun getTestResultsForUser(
        userId: String,
        limit: Long = 5,
        onComplete: (List<TestResult>) -> Unit
    ) {
        val newQuery = firestore.collection("users")
            .document(userId)
            .collection("testResults")
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(limit)

        newQuery.get()
            .addOnSuccessListener { result ->
                val listNew = result.documents.mapNotNull { it.toObject(TestResult::class.java) }
                if (listNew.isNotEmpty()) {
                    onComplete(listNew)
                } else {
                    // Fallback legacy
                    firestore.collection("users")
                        .document(userId)
                        .collection("test_results")
                        .orderBy("completedAt", Query.Direction.DESCENDING)
                        .limit(limit)
                        .get()
                        .addOnSuccessListener { legacy ->
                            val listLegacy = legacy.documents.mapNotNull { it.toObject(TestResult::class.java) }
                            onComplete(listLegacy)
                        }
                        .addOnFailureListener {
                            // Si truena el orderBy por falta de índice/field, intentamos sin ordenar
                            firestore.collection("users")
                                .document(userId)
                                .collection("test_results")
                                .get()
                                .addOnSuccessListener { all ->
                                    val listAll = all.documents.mapNotNull { it.toObject(TestResult::class.java) }
                                    onComplete(listAll.take(limit.toInt()))
                                }
                                .addOnFailureListener { _ -> onComplete(emptyList()) }
                        }
                }
            }
            .addOnFailureListener {
                // Si falla el query nuevo, intenta legacy directo
                firestore.collection("users")
                    .document(userId)
                    .collection("test_results")
                    .orderBy("completedAt", Query.Direction.DESCENDING)
                    .limit(limit)
                    .get()
                    .addOnSuccessListener { legacy ->
                        val listLegacy = legacy.documents.mapNotNull { it.toObject(TestResult::class.java) }
                        onComplete(listLegacy)
                    }
                    .addOnFailureListener { _ -> onComplete(emptyList()) }
            }
    }

    // ===== Métodos que ya tenías (compat) =====

    fun saveUserData(userId: String, email: String, name: String) {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "name" to name
        )
        firestore.collection("users").document(userId).set(user)
            .addOnSuccessListener { println("User data saved successfully!") }
            .addOnFailureListener { e -> println("Error saving user data: ${e.message}") }
    }

    /** Versión legacy exacta que ya usabas (con test_results). */
    fun saveTestResultLegacy(
        userId: String,
        testResult: TestResult,
        onComplete: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        firestore.collection("users")
            .document(userId)
            .collection("test_results")
            .add(testResult)
            .addOnSuccessListener {
                println("Test result saved successfully!")
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                println("Error saving test result: ${e.message}")
                onError?.invoke(e)
            }
    }

    /** Versión legacy exacta que ya usabas. */
    fun getUserData(userId: String, onComplete: (Map<String, Any>?) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) onComplete(document.data) else onComplete(null)
            }
            .addOnFailureListener { e ->
                println("Error fetching user data: ${e.message}")
                onComplete(null)
            }
    }
}
