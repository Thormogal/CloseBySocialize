package com.example.closebysocialize.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import com.example.closebysocialize.R

class ProfileFragment : Fragment() {
    private lateinit var reportBugs : ImageButton
    private lateinit var language: ImageButton
    private lateinit var darkModeSwitch : Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_profile, container, false)

            reportBugs = view.findViewById(R.id.reportBugsImageButton)
            language = view.findViewById(R.id.languageImageButton)
            darkModeSwitch = view.findViewById(R.id.darkModeSwitch)

            return view
        }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //kallar på funktionerna, clicklis
        reportBugs.setOnClickListener{
            //Ska man komma vidare till någon sida här?
        }
        language.setOnClickListener{
            //Picker eller spinner med länder?
        }
        darkModeSwitch.setOnClickListener {
            //darkmode ska kunna justeras
        }

    }

        //Funktioner

}

