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
        

            darkModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
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
            // Användaren är inte inloggad
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
            if (imageUrl != null && imageUrl.isNotEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.drawable.profile_top_bar_avatar)
        }
    }
}



