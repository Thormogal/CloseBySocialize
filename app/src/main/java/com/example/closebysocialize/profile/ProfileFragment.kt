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
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {
    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView

    private lateinit var reportBugs: ImageView
    private lateinit var language: ImageView
    private lateinit var darkModeSwitch: Switch
    private lateinit var aboutMeTextView: TextView
    private lateinit var userID: String

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
        aboutMeTextView = view.findViewById(R.id.aboutMeTextView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //kallar på funktionerna, clicklis

        fetchUserInfo()


        reportBugs.setOnClickListener {
            //Ska man komma vidare till någon sida här?
        }
        language.setOnClickListener {
            showLanguagePicker()
        }
        
        

            // adjust dark mode switch
            darkModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // call function for switch to darkmode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    // call function for switch to lightmode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

            }
        }


        //Funktioner

    private fun fetchUserInfo() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userName = documentSnapshot.getString("name")
                        val profileImageUrl = documentSnapshot.getString("profileImage")
                        nameTextView.text = userName
                        loadImage(profileImageUrl)

                        val aboutMe = documentSnapshot.getString("aboutMe")
                        aboutMeTextView.text = aboutMe
                    } else {
                        Log.d("!!!", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("!!!", "Failed to fetch user name", exception)
                }
        } else {
            // Användaren är inte inloggad
        }
    }

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

        private fun loadImage(imageUrl: String?) {
            if (imageUrl != null && imageUrl.isNotEmpty()) {
            // Använd Glide eller annan bildladdningsbibliotek för att ladda ner och visa bilden
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(profileImageView)
            } else {
            // Visa en standardbild om ingen profilbild finns
                profileImageView.setImageResource(R.drawable.profile_top_bar_avatar)
        }
    }
    }



