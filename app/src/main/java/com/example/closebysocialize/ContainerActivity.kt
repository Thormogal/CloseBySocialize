package com.example.closebysocialize

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.closebysocialize.util.FragmentUtils

class ContainerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        val toolbar: Toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        navView.setOnNavigationItemSelectedListener { item ->
            val fragmentClass = when (item.itemId) {
                R.id.navigation_events -> EventsFragment::class.java
                R.id.navigation_message -> MessageFragment::class.java
                R.id.navigation_map -> MapFragment::class.java
                R.id.navigation_profile -> ProfileFragment::class.java
                else -> null
            }
            fragmentClass?.let {
                FragmentUtils.switchFragment(this, R.id.fragment_container, it)
                true
            } ?: false
        }
        navView.selectedItemId = R.id.navigation_events
    }
}