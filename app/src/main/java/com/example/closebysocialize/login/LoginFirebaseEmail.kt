package com.example.closebysocialize.login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginFirebaseEmail(private val auth: FirebaseAuth) {

    fun registerUser(
        email: String,
        password: String,
        onSuccess: (FirebaseUser?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendVerificationEmail(auth.currentUser, onSuccess, onError)
                } else {
                    task.exception?.let { onError(it) }
                }
            }
    }

    private fun sendVerificationEmail(
        user: FirebaseUser?,
        onSuccess: (FirebaseUser?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(user)
                } else {
                    task.exception?.let { onError(it) }
                }
            }
    }

    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    task.exception?.let { onError(it) }
                }
            }
    }
}