package com.example.closebysocialize.profile

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.media.Image
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import com.example.closebysocialize.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class EditProfileFragment : Fragment() {

    lateinit var goBackButtonImageView: ImageView
    lateinit var editImage : ImageView
    lateinit var profileSaveButton : Button
    lateinit var editName : EditText
    lateinit var birthYearPicker : NumberPicker
    lateinit var aboutMeEditText : EditText

    lateinit var dogImageView : ImageView
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



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)


        goBackButtonImageView = view.findViewById(R.id.goBackButtonImageView)
        editImage = view.findViewById(R.id.editPictureImageView)
        profileSaveButton = view.findViewById(R.id.profileSaveButton)
        editName = view.findViewById(R.id.editName)
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

        val interestImageViews = listOf<ImageView>(dogImageView,airplaneImageView,bookImageView,
            cookingImageView,gardenImageView,cinemaImageView,restaurantImageView,sportImageView,
            coffeeImageView,gameImageView,theatreImageView,strollerImageView)

        val interestClickListener = View.OnClickListener { view ->
            val interest = view.tag as? String
            interest?.let {
                if (selectedInterests.contains(interest)) {
                    // Om intresset redan är valt, ta bort det från listan och återställ knappens animation
                    selectedInterests.remove(interest)
                    view.clearAnimation()
                } else {
                    // Om intresset inte är valt, lägg till det i listan och tillämpa nedtryckningsanimationen
                    selectedInterests.add(interest)

                    val scaleDownAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_button_animation)
                    view.startAnimation(scaleDownAnimation)
                }
                // Uppdatera knappens markerade status baserat på om intresset är valt eller inte
                view.isSelected = selectedInterests.contains(interest)
            }
        }

        interestImageViews.forEach { imageView ->
            imageView.setOnClickListener(interestClickListener)
        }

//kallar på funktionerna, clicklis


        goBackButtonImageView.setOnClickListener{
            //tillbaka till fragmentet profile fragment
        }
        editImage.setOnClickListener{
            //redigera bild
        }
        editName.setOnClickListener {
            //Redigera namn
        }
        birthYearPicker.setOnValueChangedListener {picker, oldVal, newVal ->
            val selectedYear = newVal
        }
        aboutMeEditText.setOnClickListener {
            // redigera text om mig själv
        }

        profileSaveButton.setOnClickListener {
            val bundle = Bundle().apply{
                putStringArrayList("interests", ArrayList(selectedInterests))
            }
            // Öppna profilsidan och skicka med de valda intressena
//            val profileFragment = ProfileFragment()
//            profileFragment.arguments = bundle
//            fragmentManager?.beginTransaction()?.replace(R.id.fragment_container, profileFragment)?.commit()
//        }

    }
        }

}





