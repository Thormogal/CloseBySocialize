package com.example.closebysocialize

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var navButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navButton = findViewById(R.id.navEventsButton)
        navButton.setOnClickListener {
            val intent = Intent(this, ContainerActivity::class.java)
            startActivity(intent)
        }


    }
}