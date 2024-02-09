package com.example.closebysocialize

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import com.example.closebysocialize.chat.MessageFragment
import com.example.closebysocialize.events.EventsFragment
import com.example.closebysocialize.map.MapFragment
import com.example.closebysocialize.profile.EditProfileFragment
import com.example.closebysocialize.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.closebysocialize.utils.FragmentUtils

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
                R.id.navigation_map -> MapFragment::class.java
                R.id.navigation_message -> MessageFragment::class.java
                else -> null
            }
            fragmentClass?.let {
                FragmentUtils.switchFragment(this, R.id.fragment_container, it)
                true
            } ?: false
        }
        val menuItemId = R.id.navigation_message
        val badge = navView.getOrCreateBadge(menuItemId)
        badge.isVisible = true
        badge.number = 5 //TODO for test, add dynamic later
        /* badge.backgroundColor = ContextCompat.getColor(this, R.color.primary_background)
        badge.badgeTextColor = ContextCompat.getColor(this, R.color.primary_text)
        TODO change to the colors we want
         */
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
                R.id.menu_my_events -> {
                    val args = Bundle().apply {
                        putBoolean("showOnlyMyEvents", true)
                    }
                    FragmentUtils.switchFragment(this, R.id.fragment_container, EventsFragment::class.java, args)
                    true
                }
                R.id.menu_profile -> {
                    FragmentUtils.switchFragment(this, R.id.fragment_container, ProfileFragment::class.java)
                    true
                }
                R.id.menu_edit_profile -> {
                    FragmentUtils.switchFragment(this, R.id.fragment_container, EditProfileFragment::class.java)
                    true
                }
                R.id.menu_logout -> {
                    // Handle "Log Out"
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}