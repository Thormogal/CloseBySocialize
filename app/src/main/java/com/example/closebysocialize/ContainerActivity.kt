package com.example.closebysocialize

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
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
                else -> null
            }
            fragmentClass?.let {
                FragmentUtils.switchFragment(this, R.id.fragment_container, it)
                true
            } ?: false
        }
        navView.selectedItemId = R.id.navigation_events
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile_button -> {
                showProfileMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun showProfileMenu() {
        val view = findViewById<View>(R.id.profile_button) ?: return
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_profile -> {
                    FragmentUtils.switchFragment(this, R.id.fragment_container, ProfileFragment::class.java)
                    true
                }
                R.id.menu_edit_profile -> {
                  //  FragmentUtils.switchFragment(this, R.id.fragment_container, EditProfileFragment::class.java)
                    true
                }
                R.id.menu_logout -> {
                    // Handle "Log Out" action
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}