package com.example.psychologicaltestapp.data.profile

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    fun load(
        onSuccess: (MutableMap<String, Any>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().uid ?: return onError(IllegalStateException("No auth"))
        users.document(uid).get()
            .addOnSuccessListener { snap ->
                onSuccess((snap.data ?: mutableMapOf()).toMutableMap())
            }
            .addOnFailureListener(onError)
    }

    fun savePatch(
        patch: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().uid ?: return onError(IllegalStateException("No auth"))
        val merged = patch.toMutableMap().apply {
            put("updatedAt", FieldValue.serverTimestamp())
        }
        users.document(uid).set(merged, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }
}
