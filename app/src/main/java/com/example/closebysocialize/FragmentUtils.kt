package com.example.closebysocialize.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

object FragmentUtils {
    fun switchFragment(activity: AppCompatActivity, containerId: Int, fragmentClass: Class<out Fragment>) {
        val fragmentManager = activity.supportFragmentManager
        val fragment = fragmentClass.newInstance()
        fragmentManager.beginTransaction().replace(containerId, fragment).commit()
    }
}
