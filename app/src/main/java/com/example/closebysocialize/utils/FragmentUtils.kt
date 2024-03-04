package com.example.closebysocialize.utils

import FriendsFragment
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.closebysocialize.AddEventFragment
import com.example.closebysocialize.R
import com.example.closebysocialize.chat.ChatFragment
import com.example.closebysocialize.events.EventsFragment
import com.example.closebysocialize.message.MessageFragment
import com.example.closebysocialize.profile.EditProfileFragment
import com.example.closebysocialize.profile.ProfileFragment
import com.google.android.gms.maps.MapFragment

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

    private fun updateActionBarTitle(
        activity: AppCompatActivity,
        fragmentClass: Class<out Fragment>
    ) {
        val titleResId = when (fragmentClass) {
            EventsFragment::class.java -> R.string.title_events
            ProfileFragment::class.java -> R.string.title_profile
            EditProfileFragment::class.java -> R.string.title_edit_profile
            AddEventFragment::class.java -> R.string.title_add_event
            MapFragment::class.java -> R.string.title_map
            MessageFragment::class.java -> R.string.title_message
            FriendsFragment::class.java -> R.string.title_friends
            else -> R.string.app_name
        }
        Log.d("FragmentUtils", "Updating ActionBar Title for ${fragmentClass.simpleName} to resource ID $titleResId")

        activity.supportActionBar?.title = activity.getString(titleResId)
    }

    fun openUserProfile(activity: AppCompatActivity, id: String) {
        val args = Bundle().apply {
            putString(ProfileFragment.ARG_ID, id)
        }
        switchFragment(activity, R.id.fragment_container, ProfileFragment::class.java, args)
    }

}
