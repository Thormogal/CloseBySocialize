package com.example.closebysocialize.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.example.closebysocialize.R
import android.app.AlertDialog


class ProfileFragment : Fragment() {
    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView

    private lateinit var reportBugs : ImageView
    private lateinit var language: ImageView
    private lateinit var darkModeSwitch : Switch
    private lateinit var editTextText : EditText

    val languageOptions = arrayOf("Swedish", "English")




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

            reportBugs = view.findViewById(R.id.reportBugsButtonImageView)
            language = view.findViewById(R.id.languageButtonImageView)
            darkModeSwitch = view.findViewById(R.id.darkModeSwitch)
            profileImageView = view.findViewById(R.id.profileimageView)
            nameTextView = view.findViewById(R.id.nameTextView)
            editTextText = view.findViewById(R.id.editTextText)

            return view
        }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //kallar på funktionerna, clicklis
        reportBugs.setOnClickListener {
            //Ska man komma vidare till någon sida här?
        }
        language.setOnClickListener {
            //Picker eller spinner med länder?
            showLanguagePicker()


        }


        darkModeSwitch.setOnClickListener {
            //darkmode ska kunna justeras
        }

    }

    //Funktioner

    private fun showLanguagePicker() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Language")
            .setItems(languageOptions) { dialog, which ->
                val selectedLanguage = languageOptions[which]
                // Uppdatera språk i appen baserat på valt språk
                Toast.makeText(
                    requireContext(),
                    "Selected Language: $selectedLanguage",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }
}

