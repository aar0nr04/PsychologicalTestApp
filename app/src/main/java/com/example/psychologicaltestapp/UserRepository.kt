package com.example.psychologicaltestapp

import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun saveUserData(userId: String, email: String, name: String) {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "name" to name,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                println("Datos de usuario guardados exitosamente.")
            }
            .addOnFailureListener { e ->
                println("Error al guardar datos de usuario: ${e.message}")
            }
    }
}
