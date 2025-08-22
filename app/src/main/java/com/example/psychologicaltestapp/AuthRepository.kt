package com.example.psychologicaltestapp

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    /** REGISTRO con rol; envía verificación por correo */
    fun registerUser(
        email: String,
        password: String,
        role: String = "patient",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser!!.uid
                val data = hashMapOf(
                    "email" to email,
                    "role" to role,                       // "patient" | "psychologist"
                    "verifiedEmail" to false,
                    "isApprovedPsychologist" to false,    // útil para flujo de aprobación
                    "createdAt" to System.currentTimeMillis()
                )
                db.collection("users").document(uid).set(data)
                    .addOnSuccessListener {
                        auth.currentUser!!.sendEmailVerification()
                            .addOnCompleteListener { onSuccess() }
                    }
                    .addOnFailureListener { e -> onError(e.message ?: "Firestore error") }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Auth error") }
    }

    /** LOGIN: exige email verificado */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                auth.currentUser?.reload()?.addOnCompleteListener {
                    if (auth.currentUser?.isEmailVerified == true) {
                        onSuccess()
                    } else {
                        // opcional: re-enviar verificación automáticamente
                        auth.currentUser?.sendEmailVerification()
                        auth.signOut()
                        onError("Tu correo aún no está verificado. Te reenvié el correo.")
                    }
                }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Login error") }
    }

    /** Reenviar verificación */
    fun resendVerification(onDone: () -> Unit, onError: (String) -> Unit) {
        val u = auth.currentUser ?: return onError("No hay usuario logueado")
        u.sendEmailVerification().addOnCompleteListener { onDone() }
            .addOnFailureListener { e -> onError(e.message ?: "Error reenviando correo") }
    }

    /** Google client para lanzar el intent */
    fun getGoogleClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /** Login con Google usando idToken devuelto por el intent */
    fun signInWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Google Auth error") }
    }

    fun signOut() = auth.signOut()
}
