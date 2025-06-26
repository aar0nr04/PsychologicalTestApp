import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class TestResult(
    val testType: String = "",
    val testName: String = "",
    val score: String = "0",
    val resultMessage: String = "",
    val date: String = ""
)

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser?.uid ?: throw IllegalStateException("No user is currently logged in")
    }

    fun saveUserData(userId: String, email: String, name: String) {
        val user = hashMapOf(
            "userId" to userId,
            "email" to email,
            "name" to name
        )

        firestore.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                println("User data saved successfully!")
            }
            .addOnFailureListener { e ->
                println("Error saving user data: ${e.message}")
            }
    }

    fun saveTestResult(userId: String, testResult: TestResult, onComplete: (() -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
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

    fun getTestResultsForUser(userId: String, onComplete: (List<TestResult>) -> Unit) {
        firestore.collection("users")
            .document(userId)
            .collection("test_results")
            .get()
            .addOnSuccessListener { result ->
                val results = result.documents.mapNotNull { doc ->
                    doc.toObject(TestResult::class.java)
                }
                onComplete(results)
            }
            .addOnFailureListener { e ->
                println("Error fetching test history: ${e.message}")
                onComplete(emptyList())
            }
    }

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
