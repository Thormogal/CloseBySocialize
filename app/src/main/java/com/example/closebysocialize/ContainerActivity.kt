package com.example.closebysocialize

import FriendsFragment
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.closebysocialize.message.MessageFragment
import com.example.closebysocialize.events.EventsFragment
import com.example.closebysocialize.login.LoginActivity
import com.example.closebysocialize.map.MapFragment
import com.example.closebysocialize.profile.EditProfileFragment
import com.example.closebysocialize.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.closebysocialize.utils.FragmentUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.closebysocialize.dataClass.Users
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.request.transition.Transition


class ContainerActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var profileMenuItem: MenuItem? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_api_key))
        }
        placesClient = Places.createClient(this)
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        placesClient = Places.createClient(this)
        Places.initialize(applicationContext, getString(R.string.google_maps_api_key))

        // Check and request location permissions if needed
        if (hasLocationPermission()) {
            startLocationUpdates()
        } else {
            requestLocationPermission()
        }

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

        navView.selectedItemId = R.id.navigation_events
        listenForNewMessages()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.top_app_bar, menu)
        profileMenuItem = menu?.findItem(R.id.profile_button)
        loadUserProfile()
        return true
    }


    private fun listenForNewMessages() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("ContainerActivity", "Current user ID: $userId")

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("conversations")
                .whereArrayContains("participants", userId)
                .get()
                .addOnSuccessListener { conversationDocuments ->
                    val conversationIds = conversationDocuments.documents.map { it.id }
                    var totalUnreadMessages = 0

                    conversationIds.forEach { conversationId ->
                        db.collection("conversations").document(conversationId)
                            .collection("messages")
                            .whereEqualTo("isRead", false)
                            .whereEqualTo("receiverId", userId)
                            .get()
                            .addOnSuccessListener { messageDocuments ->
                                val unreadMessagesCount = messageDocuments.size()
                                totalUnreadMessages += unreadMessagesCount

                                updateBottomNavigationBadge(totalUnreadMessages)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("ContainerActivity", "Error fetching conversations", e)
                }
        } else {
            Log.d("ContainerActivity", "User ID is null, cannot listen for messages")
        }
    }


    private fun updateBottomNavigationBadge(count: Int) {
        Log.d("ContainerActivity", "Updating badge count: $count")
        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val menuItemId = R.id.navigation_message
        if (count > 0) {
            val badge = navView.getOrCreateBadge(menuItemId)
            badge.isVisible = true
            badge.number = count
        } else {
            navView.removeBadge(menuItemId)
        }
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                // Handle the case where the user denies the location permission
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Update interval in milliseconds
            fastestInterval = 1000 // Fastest update interval in milliseconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }


    private val locationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val mapFragment =
                    supportFragmentManager.findFragmentById(R.id.fragment_container) as? MapFragment
                mapFragment?.updateMapLocation(location)
            }
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
                    FragmentUtils.switchFragment(
                        this,
                        R.id.fragment_container,
                        EventsFragment::class.java,
                        args
                    )
                    true
                }

                R.id.menu_profile -> {
                    Log.d("!!!", "Profile opens")
                    FragmentUtils.switchFragment(
                        this,
                        R.id.fragment_container,
                        ProfileFragment::class.java
                    )
                    true
                }

                R.id.menu_friends -> {
                    FragmentUtils.switchFragment(
                        this,
                        R.id.fragment_container,
                        FriendsFragment::class.java
                    )
                    true
                }

                R.id.menu_edit_profile -> {
                    FragmentUtils.switchFragment(
                        this,
                        R.id.fragment_container,
                        EditProfileFragment::class.java
                    )
                    true
                }

                R.id.menu_logout -> {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(Users::class.java)
                    user?.profileImageUrl?.let {
                        showProfileImage(it)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ContainerActivity", "Error fetching user profile", e)
                }
        }
    }

    private fun showProfileImage(profileImageUrl: String) {
        Glide.with(this)
            .asBitmap()
            .load(profileImageUrl)
            .circleCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val drawable = BitmapDrawable(resources, resource)
                    profileMenuItem?.icon = drawable
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    fun setLocationUpdatesEnabled(enabled: Boolean) {
        if (enabled) {
            startLocationUpdates()
        } else {
            stopLocationUpdates()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}
