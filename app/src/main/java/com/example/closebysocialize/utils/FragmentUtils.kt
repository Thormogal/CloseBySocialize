package com.example.closebysocialize.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.closebysocialize.AddEventFragment
import com.example.closebysocialize.R
import com.example.closebysocialize.chat.ChatFragment
import com.example.closebysocialize.events.EventsFragment
import com.example.closebysocialize.profile.EditProfileFragment
import com.example.closebysocialize.profile.ProfileFragment

object FragmentUtils {
    fun switchFragment(
        activity: AppCompatActivity,
        containerId: Int,
        fragmentClass: Class<out Fragment>,
        args: Bundle? = null
    ) {
        val fragmentManager = activity.supportFragmentManager
        val fragment = fragmentClass.newInstance().apply {
            args?.let { arguments = it }
        }
        fragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()

        updateActionBarTitle(activity, fragmentClass)
    }

    private fun updateActionBarTitle(activity: AppCompatActivity, fragmentClass: Class<out Fragment>){
        val title = when (fragmentClass){
            EventsFragment::class.java -> "Events"
            ProfileFragment::class.java -> "Profile"
            EditProfileFragment::class.java -> "Edit Profile"
            AddEventFragment::class.java -> "Add Event"

            else -> activity.getString(R.string.app_name)
        }
        activity.supportActionBar?.title = title
    }

}
