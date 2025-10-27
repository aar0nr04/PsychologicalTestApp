package com.example.psychologicaltestapp.data.profile

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.psychologicaltestapp.TestResult

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

    data class AppointmentItem(
        val id: String,
        val title: String?,
        val startTime: Timestamp?,
        val status: String?,
        val psychologistId: String?
    )

    /**
     * Lee próximas N citas del usuario.
     * Intenta primero colección global "appointments" (campo userId).
     * Si no existen, hace fallback a subcolección "users/{uid}/appointments".
     */
    fun loadAppointments(
        limit: Long = 5,
        onSuccess: (List<AppointmentItem>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = getCurrentUserId()
        val now = Timestamp.now()

        // Query global
        firestore.collection("appointments")
            .whereEqualTo("userId", uid)
            .whereGreaterThan("startTime", now)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { qs ->
                if (!qs.isEmpty) {
                    onSuccess(qs.documents.map { d ->
                        AppointmentItem(
                            id = d.id,
                            title = d.getString("title")
                                ?: d.getString("psychologistName")
                                ?: "Cita",
                            startTime = d.getTimestamp("startTime"),
                            status = d.getString("status"),
                            psychologistId = d.getString("psychologistId")
                        )
                    })
                } else {
                    // Fallback: subcolección
                    firestore.collection("users").document(uid)
                        .collection("appointments")
                        .whereGreaterThan("startTime", now)
                        .orderBy("startTime", Query.Direction.ASCENDING)
                        .limit(limit)
                        .get()
                        .addOnSuccessListener { sub ->
                            onSuccess(sub.documents.map { d ->
                                AppointmentItem(
                                    id = d.id,
                                    title = d.getString("title") ?: "Cita",
                                    startTime = d.getTimestamp("startTime"),
                                    status = d.getString("status"),
                                    psychologistId = d.getString("psychologistId")
                                )
                            })
                        }
                        .addOnFailureListener(onError)
                }
            }
            .addOnFailureListener(onError)
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
