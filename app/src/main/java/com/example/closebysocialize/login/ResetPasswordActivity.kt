package com.example.closebysocialize.login

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

        emailEditText = findViewById(R.id.registeredEmailEditText)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)

        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString()
            resetPassword(email)
        }
    }

    private fun resetPassword(email: String) {
        loginFirebaseEmail.sendPasswordResetEmail(email, onSuccess = {
            Toast.makeText(this, "Check your email to reset your password.", Toast.LENGTH_LONG)
                .show()
        }, //email will succeed even if no user is registered with that mail due to anti-fraud.
            //harder for hackers to know which mails that are truly registered or not.
            //Firebase makes sure that un-registered mail addresses will not receive a mail.
            onError = { exception ->
                if (exception is FirebaseAuthInvalidUserException) {
                    Toast.makeText(this, "No user found with this email address", Toast.LENGTH_LONG)
                        .show()
                } else {
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}