package com.example.closebysocialize.login

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.utils.FirestoreUtils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth

class LoginFirebaseFacebook(private val activity: Activity) {
    private var callbackManager = CallbackManager.Factory.create()
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun startFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("FacebookLogin", "Facebook onSuccess")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d("FacebookLogin", "Facebook onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.e("FacebookLogin", "Facebook onError", error)
            }
        })
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Log in with Facebook succeeded.", Toast.LENGTH_SHORT).show()
                    val firebaseUser = firebaseAuth.currentUser
                    firebaseUser?.let {
                        FirestoreUtils.saveUserToFirestore(it, activity)
                        val intent = Intent(activity, ContainerActivity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                } else {
                    Log.e("FacebookLogin", "Firebase Authentication failed", task.exception)
                    Toast.makeText(activity, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
