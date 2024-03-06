package com.example.closebysocialize.profile


import android.content.Context
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class EditProfileFragment : Fragment() {

    var profileImageUpdatedListener: OnProfileImageUpdatedListener? = null
    private lateinit var editImage: ImageView
    private lateinit var profileSaveButton: Button
    private lateinit var editName: EditText
    private lateinit var birthYearPicker: NumberPicker
    private lateinit var aboutMeEditText: EditText
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private val selectedInterests = mutableSetOf<Int>()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupImagePickerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_edit, container, false)
        initializeUI(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUserInteractions(view)
        fetchUserData()
    }

    private fun initializeUI(view: View) {
        editImage = view.findViewById(R.id.editPictureImageView)
        profileSaveButton = view.findViewById(R.id.profileSaveButton)
        editName = view.findViewById(R.id.editNameEditText)
        birthYearPicker = view.findViewById(R.id.birthYearPicker)
        aboutMeEditText = view.findViewById(R.id.aboutMeEditText)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        birthYearPicker.minValue = 1900
        birthYearPicker.maxValue = currentYear
        birthYearPicker.value = currentYear
    }

    private fun setupImagePickerLauncher() {
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    selectedImageUri = it
                    uploadImageToFirebaseStorage(it)
                    setProfileImage(it.toString())
                } ?: run {
                    Toast.makeText(context, "Error in selecting image", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupUserInteractions(view: View) {
        setupInterestClickListeners(view)
        setupProfileImageViewButton()
        setupBirthYearButton()
        setupProfileSaveButton()
    }

    private fun setupProfileImageViewButton() {
        editImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun setupBirthYearButton() {
        birthYearPicker.setOnValueChangedListener { _, _, _ ->
        }
    }

    private fun setupProfileSaveButton() {
        profileSaveButton.setOnClickListener {
            saveProfileDataToDatabase()
        }
    }

    private fun setupInterestClickListeners(view: View) {
        interestToDrawableMap.forEach { (imageViewId, drawableId) ->
            val imageView = view.findViewById<ImageView>(imageViewId)
            imageView.setOnClickListener {
                handleInterestClick(imageView, drawableId)
            }
        }
    }

    private fun setProfileImage(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty() || imageUrl == "defaultUrl") {
            Glide.with(this)
                .load(R.drawable.avatar_dark)
                .circleCrop()
                .into(editImage)
        } else {
            Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .into(editImage)
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val fileName = "ProfilePictures/${UUID.randomUUID()}.jpg"
        val storageRef = FirebaseStorage.getInstance().getReference(fileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveProfileImageUrlToFirestore(uri.toString())
                    profileImageUpdatedListener?.onProfileImageUpdated(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error when trying to upload picture",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveProfileImageUrlToFirestore(imageUrl: String) {
        val userRef = db.collection("users").document(id)
        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                setProfileImage(imageUrl)
            }
            .addOnFailureListener {
            }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        profileImageUpdatedListener = context as? OnProfileImageUpdatedListener
    }

    override fun onDetach() {
        super.onDetach()
        profileImageUpdatedListener = null
    }

    interface OnProfileImageUpdatedListener {
        fun onProfileImageUpdated(newImageUrl: String)
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

    private fun saveProfileDataToDatabase() {
        val aboutMeText = aboutMeEditText.text.toString()
        val name = editName.text.toString()
        val birthYear = birthYearPicker.value

        val selectedInterestsAsString = selectedInterests.map { it.toString() }
        val userRef = db.collection("users").document(id)
        val updates = hashMapOf<String, Any>(
            "aboutMe" to aboutMeText,
            "name" to name,
            "birthYear" to birthYear
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

    private fun fetchUserData() {
        val userRef = db.collection("users").document(id)
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val name = document.getString("name")
                val aboutMe = document.getString("aboutMe")
                val birthYear = document.getLong("birthYear")?.toInt()
                val profileImageUrl = document.getString("profileImageUrl")

                editName.setText(name)
                aboutMeEditText.setText(aboutMe)
                birthYear?.let {
                    birthYearPicker.value = it
                }
                profileImageUrl?.let {
                    setProfileImage(it)
                }
                val selectedInterestsData = document.get("selectedInterests")
                if (selectedInterestsData is List<*>) {
                    updateSelectedInterestsUI(selectedInterestsData.filterIsInstance<String>())
                }
            }
        }.addOnFailureListener {
            Log.e("EditProfileFragment", "Error fetching user data", it)
        }
    }
}