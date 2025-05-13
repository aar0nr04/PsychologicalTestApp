package com.example.psychologicaltestapp
import com.google.firebase.auth.FirebaseAuth

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Función para registrar un nuevo usuario
    fun registerUser(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    onSuccess()
                } else {
                    // Error durante el registro
                    onError(task.exception?.message ?: "Error desconocido")
                }
            }
    }
    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    onSuccess()
                } else {
                    // Error durante el inicio de sesión
                    onError(task.exception?.message ?: "Error desconocido")
                }
            }
    }
}