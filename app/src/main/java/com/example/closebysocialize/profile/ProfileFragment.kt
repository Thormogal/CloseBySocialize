package com.example.closebysocialize.profile

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.closebysocialize.R
import android.app.AlertDialog
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {
    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var reportBugs: TextView
    private lateinit var language: TextView
    private lateinit var darkModeSwitch: SwitchCompat
    private lateinit var aboutMeTextView: TextView

    private val languageOptions = arrayOf("Swedish", "English")


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

        reportBugs = view.findViewById(R.id.reportBugTextView)
        language = view.findViewById(R.id.languageTextView)
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch)
        profileImageView = view.findViewById(R.id.profileImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        aboutMeTextView = view.findViewById(R.id.aboutMeTextView)

        return view
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //calls the functions, clickList

        fetchUserInfo()

        reportBugs.setOnClickListener {
            //Show a dialogue in order to report bugs to the developers
        }
        language.setOnClickListener {
            showLanguagePicker()
        }

        val currentNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK)
        darkModeSwitch.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES

            darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

            }
        }


    private fun fetchUserInfo() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userName = documentSnapshot.getString("name")
                        val profileImageUrl = documentSnapshot.getString("profileImage")
                        val aboutMe = documentSnapshot.getString("aboutMe")
                        updateProfileUI(aboutMe, userName,profileImageUrl)
                    } else {
                        Log.d("!!!", "Document does not exist")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("!!!", "Failed to fetch user name", exception)
                }
        } else {
            // The user is not logged in
        }
    }

        private fun updateProfileUI(aboutMe: String?, userName: String?, profileImageUrl: String?) {
            if (!userName.isNullOrEmpty()) {
                nameTextView.text = userName
         }
            if (!aboutMe.isNullOrEmpty()) {
               aboutMeTextView.text = aboutMe
            }


        if (!profileImageUrl.isNullOrEmpty()) {
            loadImage(profileImageUrl)
        }
    }

        private fun showLanguagePicker() {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Select Language")
                .setItems(languageOptions) { dialog, which ->
                    val selectedLanguage = languageOptions[which]
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
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.profile_top_bar_avatar)
        }
    }
}



