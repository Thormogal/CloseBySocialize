package com.example.closebysocialize.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.FacebookSdk
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.example.closebysocialize.ContainerActivity
import com.example.closebysocialize.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        FacebookSdk.setApplicationId("3196808700613916")
        FacebookSdk.sdkInitialize(applicationContext)

        val splashText = findViewById<TextView>(R.id.splash_text)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_animation)
        splashText.startAnimation(fadeInAnimation)

        fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                checkLoginAndNavigate()
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
    }

    private fun checkLoginAndNavigate() {
        if (FirebaseAuth.getInstance().currentUser != null) {
            navigateToMainPage()
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToMainPage() {
        val intent = Intent(this, ContainerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
