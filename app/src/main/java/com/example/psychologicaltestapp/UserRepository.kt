package com.example.psychologicaltestapp

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun saveUserData(userId: String, email: String, name: String) {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "name" to name,
            "testHistory" to emptyList<TestResult>() // Initialize with an empty list
        )
        firestore.collection("users").document(userId).set(user)
    }

    fun saveTestResult(userId: String, testResult: TestResult) {
        firestore.collection("users").document(userId)
            .update("testHistory", FieldValue.arrayUnion(testResult))
            .addOnSuccessListener {
                println("Test result saved successfully!")
            }
            .addOnFailureListener { e ->
                println("Error saving test result: ${e.message}")
            }
    }
    fun getTestHistory(userId: String, onComplete: (List<TestResult>) -> Unit) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val testHistory = document.get("testHistory") as? List<Map<String, Any>>
                val results = testHistory?.map { map ->
                    TestResult(
                        testType = map["testType"] as String,
                        testName = map["testName"] as String,
                        resultMessage = map["resultMessage"] as String,
                        date = map["date"] as String
                    )
                } ?: emptyList()
                onComplete(results)
            }
            .addOnFailureListener { e ->
                println("Error fetching test history: ${e.message}")
                onComplete(emptyList())
            }
    }
}