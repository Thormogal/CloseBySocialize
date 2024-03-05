package com.example.closebysocialize.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class EditProfileFragment : Fragment() {

    private lateinit var editImage: ImageView
    private lateinit var profileSaveButton: Button
    private lateinit var editName: EditText
    private lateinit var birthYearPicker: NumberPicker
    private lateinit var aboutMeEditText: EditText

    private val selectedInterests = mutableSetOf<Int>()
    private val GALLERY_REQUEST_CODE = 100
    private var selectedImageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val id = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val interestToDrawableMap = mapOf(
        R.id.dogImageView to R.drawable.interests_dogwalk,
        R.id.airPlaneImageView to R.drawable.interests_airplane_takeoff,
        R.id.bookImageView to R.drawable.interests_book,
        R.id.cookingImageView to R.drawable.interests_cooking,
        R.id.gardenImageView to R.drawable.interests_garden,
        R.id.cinemaImageView to R.drawable.interests_movie,
        R.id.restaurantImageView to R.drawable.interests_restaurant,
        R.id.sportImageView to R.drawable.interests_football,
        R.id.coffeeImageView to R.drawable.interests_coffee_mug,
        R.id.gameImageView to R.drawable.interests_gaming_controller,
        R.id.theatreImageView to R.drawable.interests_theatre_mask,
        R.id.strollerImageView to R.drawable.interests_stroller
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_edit, container, false)

        editImage = view.findViewById(R.id.editPictureImageView)
        profileSaveButton = view.findViewById(R.id.profileSaveButton)
        editName = view.findViewById(R.id.editNameEditText)
        birthYearPicker = view.findViewById(R.id.birthYearPicker)
        aboutMeEditText = view.findViewById(R.id.aboutMeEditText)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        birthYearPicker.minValue = 1900
        birthYearPicker.maxValue = currentYear
        birthYearPicker.value = currentYear

        return view
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        interestToDrawableMap.forEach { (imageViewId, drawableId) ->
            val imageView = view.findViewById<ImageView>(imageViewId)
            imageView.setOnClickListener {
                handleInterestClick(imageView, drawableId)
            }
        }

        editImage.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        birthYearPicker.setOnValueChangedListener { _, _, newVal ->
            val selectedYear = newVal
        }

        profileSaveButton.setOnClickListener {
            saveProfileDataToDatabase()
        }

        fetchUserData()
    }

    private fun handleInterestClick(imageView: ImageView, drawableId: Int) {
        if (selectedInterests.contains(drawableId)) {
            selectedInterests.remove(drawableId)
            imageView.isSelected = false
        } else {
            if (selectedInterests.size >= 4) {
                Toast.makeText(context, "You can select up to 4 interests", Toast.LENGTH_SHORT)
                    .show()
            } else {
                selectedInterests.add(drawableId)
                imageView.isSelected = true
            }
        }
    }

    private fun saveProfileDataToDatabase() {
        val aboutMeText = aboutMeEditText.text.toString()
        val name = editName.text.toString()
        val birthYear = birthYearPicker.value
        val profileImageURI = selectedImageUri?.toString() ?: ""

        val selectedInterestsAsString = selectedInterests.map { it.toString() }

        val userRef = db.collection("users").document(id)

        val updates = hashMapOf<String, Any>(
            "aboutMe" to aboutMeText,
            "name" to name,
            "birthYear" to birthYear,
            "profileImage" to profileImageURI
        )

        if (selectedInterestsAsString.isNotEmpty()) {
            updates["selectedInterests"] = selectedInterestsAsString
        }

        userRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Failed to update profile: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            setProfileImage(selectedImageUri)
        }
    }

    private fun setProfileImage(imageUri: Uri?) {
        try {
            imageUri?.let {
                if (it.toString().startsWith("content://")) {
                    // local URI
                    Glide.with(this)
                        .load(it)
                        .into(editImage)
                } else {
                    // URL from the internet
                    Glide.with(this)
                        .load(it.toString())
                        .into(editImage)
                }
            } ?: run {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("EditProfileFragment", "Exception: ", e)
            Toast.makeText(requireContext(), "Error setting image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserData() {
        val userRef = db.collection("users").document(id)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val name = document.getString("name")
                val aboutMe = document.getString("aboutMe")
                val birthYear = document.getLong("birthYear")?.toInt()
                val profileImageUri = document.getString("profileImage")

                editName.setText(name)
                aboutMeEditText.setText(aboutMe)
                birthYear?.let {
                    birthYearPicker.value = it
                }
                profileImageUri?.let {
                    selectedImageUri = Uri.parse(it)
                    setProfileImage(selectedImageUri)
                }
                val selectedInterestsData = document.get("selectedInterests") as? List<String>
                selectedInterestsData?.let {
                    updateSelectedInterestsUI(it)
                }
            }
        }.addOnFailureListener {
            Log.e("EditProfileFragment", "Error fetching user data", it)
        }
    }

    private fun updateSelectedInterestsUI(selectedInterestsData: List<String>) {
        interestToDrawableMap.forEach { (imageViewId, drawableId) ->
            val imageView = view?.findViewById<ImageView>(imageViewId)
            if (selectedInterestsData.contains(drawableId.toString())) {
                selectedInterests.add(drawableId)
                imageView?.isSelected = true
            } else {
                imageView?.isSelected = false
            }
        }
    }
}