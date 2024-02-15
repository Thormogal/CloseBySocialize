package com.example.closebysocialize.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import com.example.closebysocialize.R
import com.google.android.gms.auth.api.signin.GoogleSignIn


class LoginActivity : AppCompatActivity() {

    private lateinit var loginFirebaseGoogle: LoginFirebaseGoogle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginFirebaseGoogle = LoginFirebaseGoogle(this)

        findViewById<Button>(R.id.signInGoogleButton).setOnClickListener {
            loginFirebaseGoogle.startSigninIntent()
        }

        findViewById<Button>(R.id.signInEmailButton).setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LoginFirebaseGoogle.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            loginFirebaseGoogle.handleSigninResult(task)
        }

    }
}