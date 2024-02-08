package com.example.closebysocialize.utils

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

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
    }
}
