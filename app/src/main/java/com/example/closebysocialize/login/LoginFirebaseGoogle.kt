package com.example.closebysocialize.login

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.R
import com.example.closebysocialize.utils.FirestoreUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFirebaseGoogle(private val activity: Activity) {
    companion object {
        const val RC_SIGN_IN = 100
    }

    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, gso)
    }

    fun startSigninIntent() {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun handleSigninResult(task: Task<*>) {
        try {
            val account = task.getResult(ApiException::class.java) as GoogleSignInAccount
            if (account.idToken == null) {
                Toast.makeText(activity, "Error: Google idToken is null", Toast.LENGTH_SHORT).show()
                return
            }
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(activity) { firebaseTask ->
                    if (firebaseTask.isSuccessful) {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        firebaseUser?.let {
                            FirestoreUtils.saveUserToFirestore(it, activity)
                        }
                        val intent = Intent(activity, ContainerActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    } else {
                        Toast.makeText(
                            activity,
                            "Log in to Firebase failed: ${firebaseTask.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } catch (e: ApiException) {
            Toast.makeText(activity, "Log in with Google failed: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

}