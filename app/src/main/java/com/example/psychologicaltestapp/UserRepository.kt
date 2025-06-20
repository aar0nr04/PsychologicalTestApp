package com.example.psychologicaltestapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Define the TestResult data class
data class TestResult(
    val testType: String = "", // Tipo de prueba (e.g., "Personalidad")
    val testName: String = "", // Nombre de la prueba
    val score: String = "0",        // Puntuación numérica
    val resultMessage: String = "", // Mensaje detallado del resultado
    val date: String = ""      // Fecha de la prueba
)
class PsychologicalTest {
    var type: String? = null
    var title: String? = null
    var description: String? = null
    var instructions: String? = null
    var questions: List<Question>? = null
}
class UserRepository {
    // Initialize Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Returns the current user's ID from Firebase Authentication.
     */
    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: throw IllegalStateException("No user is currently logged in")
    }

    /**
     * Fetches the test history for a specific user from Firestore.
     */
    fun getTestResultsForUser(userId: String, onComplete: (List<TestResult>) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val testHistory = document.get("testHistory") as? List<Map<String, Any>>
                    val results = testHistory?.mapNotNull { map ->
                        try {
                            TestResult(
                                testType = map["testType"] as? String ?: "",
                                testName = map["testName"] as? String ?: "",
// Convert the score to String
                                score = (map["score"] as? Number)?.toString() ?: "0", // Safely                                 resultMessage = map["resultMessage"] as? String ?: "",
                                date = map["date"] as? String ?: ""
                            )
                        } catch (e: Exception) {
                            println("Error parsing test result: ${e.message}")
                            null
                        }
                    } ?: emptyList()

                    onComplete(results)
                } else {
                    println("User document does not exist for userId: $userId")
                    onComplete(emptyList())
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching test history: ${e.message}")
                onComplete(emptyList())
            }
    }

    /**
     * Saves basic user data to Firestore.
     */
    fun saveUserData(userId: String, email: String, name: String) {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "name" to name,
            "testHistory" to null // Initialize as null instead of empty list
        )

        firestore.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                println("User data saved successfully!")
            }
            .addOnFailureListener { e ->
                println("Error saving user data: ${e.message}")
            }
    }

    /**
     * Saves a test result for a specific user in Firestore.
     */
    fun saveTestResult(userId: String, testResult: TestResult) {
        // Convert TestResult to a Map for Firestore compatibility
        val testResultMap = hashMapOf(
            "testType" to testResult.testType,
            "testName" to testResult.testName,
            "score" to testResult.score,
            "resultMessage" to testResult.resultMessage,
            "date" to testResult.date
        )

        firestore.collection("users").document(userId)
            .update("testHistory", FieldValue.arrayUnion(testResultMap))
            .addOnSuccessListener {
                println("Test result saved successfully!")
            }
            .addOnFailureListener { e ->
                println("Error saving test result: ${e.message}")
            }
    }

    /**
     * Fetches all user data for a specific user from Firestore.
     */
    fun getUserData(userId: String, onComplete: (Map<String, Any>?) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onComplete(document.data)
                } else {
                    println("User document does not exist for userId: $userId")
                    onComplete(null)
                }
            }
            .addOnFailureListener { e ->
                println("Error fetching user data: ${e.message}")
                onComplete(null)
            }
    }
}