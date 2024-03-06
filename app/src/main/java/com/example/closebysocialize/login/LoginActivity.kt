package com.example.closebysocialize.login

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var loginFirebaseGoogle: LoginFirebaseGoogle
    private lateinit var loginFirebaseGithub: LoginFirebaseGithub
    private lateinit var loginFirebaseFacebook: LoginFirebaseFacebook
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeUI()
        initializeFirebaseServices()
        setupEventListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LoginFirebaseGoogle.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            loginFirebaseGoogle.handleSigninResult(task)
        }
        loginFirebaseFacebook.handleActivityResult(requestCode, resultCode, data)
    }

    private fun initializeUI() {
        emailEditText = findViewById(R.id.loginEmailEditText)
        passwordEditText = findViewById(R.id.loginPasswordEditText)
        loginButton = findViewById(R.id.loginButton)
    }

    private fun initializeFirebaseServices() {
        loginFirebaseGoogle = LoginFirebaseGoogle(this)
        loginFirebaseGithub = LoginFirebaseGithub(this)
        loginFirebaseFacebook = LoginFirebaseFacebook(this)
    }

    private fun setupEventListeners() {
        setOnClickListener(R.id.forgotPasswordTextView) { navigateTo(ResetPasswordActivity::class.java) }
        setOnClickListener(R.id.signInEmailButton) { navigateTo(SignupActivity::class.java) }
        setOnClickListener(R.id.signInGoogleButton) { loginFirebaseGoogle.startSigninIntent() }
        setOnClickListener(R.id.signInFacebookButton) { loginFirebaseFacebook.startFacebookLogin() }
        setOnClickListener(R.id.signInGithubButton) { loginFirebaseGithub.signInWithGitHub() }
        loginButton.setOnClickListener { validateLoginInformation() }
    }

    private fun <T : Activity> navigateTo(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        if (activityClass == ContainerActivity::class.java) {
            finish()
        }
    }

    private fun setOnClickListener(viewId: Int, action: () -> Unit) {
        findViewById<View>(viewId).setOnClickListener { action() }
    }

    private fun validateForm(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailEditText.error = "Email required."
            return false
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Password required"
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Logged in successfully", Toast.LENGTH_SHORT).show()
                    navigateTo(ContainerActivity::class.java)
                } else {
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun validateLoginInformation() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (validateForm(email, password)) {
            loginUser(email, password)
        }
    }
}