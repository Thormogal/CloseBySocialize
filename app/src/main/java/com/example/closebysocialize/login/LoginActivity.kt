package com.example.closebysocialize.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.loginEmailEditText)
        passwordEditText = findViewById(R.id.loginPasswordEditText)
        loginButton = findViewById(R.id.loginButton)
        loginFirebaseGoogle = LoginFirebaseGoogle(this)
        loginFirebaseGithub = LoginFirebaseGithub(this)

        findViewById<TextView>(R.id.forgotPasswordTextView).setOnClickListener {
            navigateToResetPage()
        }

        loginButton.setOnClickListener {
            validateLoginInformation()
        }

        findViewById<Button>(R.id.signInEmailButton).setOnClickListener {
            navigateToSignupPage()
        }

        findViewById<Button>(R.id.signInGoogleButton).setOnClickListener {
            loginFirebaseGoogle.startSigninIntent()
        }

        findViewById<Button>(R.id.signInGithubButton).setOnClickListener {
            loginFirebaseGithub.signInWithGitHub()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LoginFirebaseGoogle.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            loginFirebaseGoogle.handleSigninResult(task)
        }
    }

    private fun navigateToResetPage() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMainPage() {
        val intent = Intent(this, ContainerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignupPage() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
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
                    navigateToMainPage()
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