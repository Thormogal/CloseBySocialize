package com.example.closebysocialize.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.example.closebysocialize.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val splashText = findViewById<TextView>(R.id.splash_text)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_screen_animation)
        splashText.startAnimation(fadeInAnimation)

        fadeInAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                navigateToLogin()
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
