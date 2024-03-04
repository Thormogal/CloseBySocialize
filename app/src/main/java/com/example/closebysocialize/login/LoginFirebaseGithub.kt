package com.example.closebysocialize.login

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.utils.FirestoreUtils
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider

class LoginFirebaseGithub(private val activity: Activity) {

    fun signInWithGitHub() {
        val provider = OAuthProvider.newBuilder("github.com")

        val auth = FirebaseAuth.getInstance()
        auth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener { authResult ->
                handleSignInResult(authResult)
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Github sign-in failed: ${it.message}", Toast.LENGTH_LONG)
                    .show()
            }

    }

    private fun handleSignInResult(authResult: AuthResult) {
        val user = authResult.user
        if (user != null) {
            FirestoreUtils.saveUserToFirestore(user, activity)
            val intent = Intent(activity, ContainerActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        }
    }
}