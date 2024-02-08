import com.google.firebase.firestore.FirebaseFirestore

data class UserDetails(
    val firstName: String,
    val lastName: String,
    val username: String,
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
                    val userDetails = UserDetails(
                        firstName = document.getString("firstName") ?: "",
                        lastName = document.getString("lastName") ?: "",
                        username = document.getString("username") ?: "Anonymous",
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
