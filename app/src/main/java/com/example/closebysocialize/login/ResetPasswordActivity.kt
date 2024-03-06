package com.example.closebysocialize.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.closebysocialize.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button
    private val loginFirebaseEmail = LoginFirebaseEmail(FirebaseAuth.getInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        initializeUI()
        setupEventListeners()
    }

    private fun initializeUI() {
        emailEditText = findViewById(R.id.registeredEmailEditText)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
    }

    private fun setupEventListeners() {
        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (isValidEmail(email)) {
                resetPassword(email)
            } else {
                Toast.makeText(this, "Enter a valid e-mail address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun resetPassword(email: String) {
        loginFirebaseEmail.sendPasswordResetEmail(email, onSuccess = {
            Toast.makeText(this, "Check your email to reset your password.", Toast.LENGTH_LONG)
                .show()
            navigateToLoginPage()
        },
            onError = { exception ->
                handlePasswordResetError(exception)
            })
    }

    private fun handlePasswordResetError(exception: Exception) {
        val message = if (exception is FirebaseAuthInvalidUserException) {
            "No user found with this email address"
        } else {
            "Error: ${exception.message}"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}