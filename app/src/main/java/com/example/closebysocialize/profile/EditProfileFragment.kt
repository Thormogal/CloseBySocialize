package com.example.closebysocialize.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.closebysocialize.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class EditProfileFragment : Fragment() {

    lateinit var goBackButtonImageView: ImageView
    lateinit var editImage: ImageView
    lateinit var profileSaveButton: Button
    lateinit var editName: EditText
    lateinit var birthYearPicker: NumberPicker
    lateinit var aboutMeEditText: EditText

    lateinit var dogImageView: ImageView
    lateinit var airplaneImageView: ImageView
    lateinit var bookImageView: ImageView
    lateinit var cookingImageView: ImageView
    lateinit var gardenImageView: ImageView
    lateinit var cinemaImageView: ImageView
    lateinit var restaurantImageView: ImageView
    lateinit var sportImageView: ImageView
    lateinit var coffeeImageView: ImageView
    lateinit var gameImageView: ImageView
    lateinit var theatreImageView: ImageView
    lateinit var strollerImageView: ImageView

    private val selectedInterests = mutableListOf<String>()

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val GALLERY_REQUEST_CODE = 100
    private var selectedImageUri: Uri? = null




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        goBackButtonImageView = view.findViewById(R.id.goBackButtonImageView)
        editImage = view.findViewById(R.id.editPictureImageView)
        profileSaveButton = view.findViewById(R.id.profileSaveButton)
        editName = view.findViewById(R.id.editNameEditText)
        birthYearPicker = view.findViewById(R.id.birthYearPicker)
        aboutMeEditText = view.findViewById(R.id.aboutMeEditText)
        dogImageView = view.findViewById(R.id.dogImageView)
        airplaneImageView = view.findViewById(R.id.airPlaneImageView)
        bookImageView = view.findViewById(R.id.bookImageView)
        cookingImageView = view.findViewById(R.id.cookingImageView)
        gardenImageView = view.findViewById(R.id.gardenImageView)
        cinemaImageView = view.findViewById(R.id.cinemaImageView)
        restaurantImageView = view.findViewById(R.id.restaurantImageView)
        sportImageView = view.findViewById(R.id.sportImageView)
        coffeeImageView = view.findViewById(R.id.coffeeImageView)
        gameImageView = view.findViewById(R.id.gameImageView)
        theatreImageView = view.findViewById(R.id.theatreImageView)
        strollerImageView = view.findViewById(R.id.strollerImageView)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        birthYearPicker.minValue = 1900
        birthYearPicker.maxValue = currentYear
        birthYearPicker.value = currentYear

        return view
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val interestImageViews = listOf(
            dogImageView, airplaneImageView, bookImageView,
            cookingImageView, gardenImageView, cinemaImageView, restaurantImageView, sportImageView,
            coffeeImageView, gameImageView, theatreImageView, strollerImageView
        )

        val interestClickListener = View.OnClickListener { view ->
            val interest = view.tag as? String
            interest?.let {
                if (selectedInterests.contains(interest)) {
                    // Om intresset redan är valt, ta bort det från listan och återställ knappens animation
                    selectedInterests.remove(interest)
                    view.isSelected = false
                } else {
                    // Om intresset inte är valt, lägg till det i listan och tillämpa nedtryckningsanimationen
                    selectedInterests.add(interest)
                    view.isSelected = true
                }
            }
        }

        interestImageViews.forEach { imageView ->
            imageView.setOnClickListener(interestClickListener)
            imageView.setBackgroundResource(R.drawable.unselected_button_background)// Ange standardbakgrund


        }

//kallar på funktionerna, clicklis


        goBackButtonImageView.setOnClickListener {
            //tillbaka till fragmentet profile fragment
        }
        editImage.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }
        birthYearPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            val selectedYear = newVal
        }
        profileSaveButton.setOnClickListener {
            saveProfileDataToDatabase()
        }

    }

    private fun saveProfileDataToDatabase() {
        val aboutMeText = aboutMeEditText.text.toString()
        val name = editName.text.toString()
        val birthYear = birthYearPicker.value
        val profileImageURI = selectedImageUri.toString()

        val userRef = db.collection("users").document(userId)

        // Uppdatera flera fält samtidigt i databasen
        userRef.update(
            mapOf(
                "aboutMe" to aboutMeText,
                "name" to name,
                "birthYear" to birthYear,
                "profileImage" to profileImageURI
                // Lägg till fler fält här om det behövs
            )
        )
            .addOnSuccessListener {
            }
            .addOnFailureListener {
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
        if (imageUri != null) {
            val bitmap = MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                imageUri
            )
            editImage.setImageBitmap(bitmap)
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
}








