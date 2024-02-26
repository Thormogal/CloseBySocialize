package com.example.closebysocialize.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Patterns
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.example.closebysocialize.R
import androidx.appcompat.app.AppCompatActivity
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.utils.FirestoreUtils
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private val handler = Handler()
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var repeatPasswordEditText: EditText
    private lateinit var signupButton: Button
    private lateinit var checkEmailVerificationRunnable: Runnable
    private lateinit var progressSpinner: ProgressBar
    private lateinit var frameLayout: FrameLayout
    private val loginFirebaseEmail = LoginFirebaseEmail(FirebaseAuth.getInstance())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        repeatPasswordEditText = findViewById(R.id.repeatPasswordEditText)
        signupButton = findViewById(R.id.signupButton)
        progressSpinner = findViewById(R.id.signUpProgressSpinner)
        frameLayout = findViewById(R.id.signUpFrameLayout)

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            registerUser(email, password)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val validEndings = listOf(".com", ".net", ".org", ".se", ".gov")
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && validEndings.any {
            email.endsWith(
                it
            )
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    private fun showProgressSpinner(show: Boolean) {
        frameLayout.visibility = if (show) View.VISIBLE else View.GONE

        signupButton.alpha = if (show) 0.3f else 1.0f
    }

    private fun registerUser(email: String, password: String) {
        val repeatPassword = repeatPasswordEditText.text.toString()

        if (password != repeatPassword) {
            Toast.makeText(this, "The passwords doesn't match", Toast.LENGTH_LONG).show()
            return
        }
        if (!isEmailValid(email)) {
            Toast.makeText(this, "Enter a valid e-mail address.", Toast.LENGTH_LONG).show()
            return
        }

        if (!isPasswordValid(password)) {
            Toast.makeText(
                this,
                "The password must be at least 6 characters long",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        loginFirebaseEmail.registerUser(email, password, onSuccess = { firebaseUser ->
            firebaseUser?.let {
                FirestoreUtils.saveUserToFirestore(it, this)
                Toast.makeText(
                    this,
                    "Confirmation mail sent. Please check your e-mail account.",
                    Toast.LENGTH_LONG
                ).show()
                startEmailVerificationCheck()
                showProgressSpinner(true)
            } ?: run {
                Toast.makeText(
                    this,
                    "Registration successful but encountered an issue fetching user details.",
                    Toast.LENGTH_LONG
                ).show()
            }
        },
            onError = { exception ->
                Toast.makeText(
                    this,
                    "Error with registration: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            })
    }

    private fun startEmailVerificationCheck() {
        checkEmailVerificationRunnable = object : Runnable {
            override fun run() {
                val user = FirebaseAuth.getInstance().currentUser
                user?.reload()?.addOnCompleteListener { task ->
                    if (task.isSuccessful && user.isEmailVerified) {
                        val intent = Intent(this@SignupActivity, ContainerActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        handler.postDelayed(this, 2000)
                    }
                }
            }
        }
        handler.post(checkEmailVerificationRunnable)
    }
}