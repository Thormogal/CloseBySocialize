import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class UserDetails(
    val firstName: String,
    val lastName: String,
    val email: String,
    val profileImageUrl: String
)

object UserDetailsFetcher {
    fun fetchUserDetails(userId: String?, callback: (UserDetails?, Exception?) -> Unit) {
        if (userId == null) {
            callback(null, IllegalArgumentException("User ID cannot be null"))
            return
        }
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val fullName = document.getString("name") ?: "Unknown Unknown"
                    val parts = fullName.split(" ", limit = 2)
                    val firstName = parts.getOrElse(0) { "Unknown" }
                    val lastName = parts.getOrElse(1) { "Unknown" }
                    val userDetails = UserDetails(
                        firstName = firstName,
                        lastName = lastName,
                        email = document.getString("email") ?: "No Email",
                        profileImageUrl = document.getString("profileImageUrl") ?: ""
                    )
                    callback(userDetails, null)
                } else {
                    callback(null, NullPointerException("Document is null"))
                }
            }
            .addOnFailureListener { exception ->
                callback(null, exception)
            }
    }
}


object AuthUtil {
    fun getCurrentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid
}