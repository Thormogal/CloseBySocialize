package com.example.closebysocialize

import AuthUtil
import UserDetailsFetcher
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddEventFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var chosenDay: String? = null
    private var chosenDate: String? = null
    private var chosenTime: String? = null

    private lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        firestore = FirebaseFirestore.getInstance()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayout)
        var selectedImageView: ImageView? = null
        var selectedCategory: String? = null
        val numberPicker = view.findViewById<NumberPicker>(R.id.spotPicker)
        numberPicker.maxValue = 20
        numberPicker.minValue = 1
        numberPicker.value = 4

        for (i in 0 until gridLayout.childCount) {
            val child = gridLayout.getChildAt(i)
            if (child is ImageView) {
                child.tag = child.contentDescription
                child.setOnClickListener {
                    selectedImageView?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            android.R.color.transparent
                        )
                    )
                    val background = it.background
                    if (background is ColorDrawable) {
                        val color = background.color
                        if (color == ContextCompat.getColor(
                                requireContext(),
                                R.color.primary_blue
                            )
                        ) {
                            it.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    android.R.color.transparent
                                )
                            )
                        } else {
                            it.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.primary_blue
                                )
                            )
                            selectedImageView = it as ImageView
                        }
                    } else {
                        it.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary_blue
                            )
                        )
                        selectedImageView = it as ImageView
                        selectedCategory = it.tag as String


                    }
                }
            }
        }


        val eventDateEditText = view.findViewById<TextInputEditText>(R.id.eventDate)
        eventDateEditText.isFocusable = false
        eventDateEditText.isClickable = true

        eventDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val datePickerDialog = DatePickerDialog(
                it.context,
                R.style.DialogTheme,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val timePickerDialog = TimePickerDialog(
                        it.context,
                        R.style.DialogTheme,
                        { _, selectedHour, selectedMinute ->
                            calendar.set(
                                selectedYear,
                                selectedMonth,
                                selectedDay,
                                selectedHour,
                                selectedMinute
                            )
                            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            chosenDay = dayFormat.format(calendar.time)
                            chosenDate = dateFormat.format(calendar.time)
                            chosenTime = timeFormat.format(calendar.time)
                            eventDateEditText.setText("$chosenDay, $chosenDate, $chosenTime")
                        },
                        hour,
                        minute,
                        true
                    )
                    timePickerDialog.show()
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        val createEventButton = view.findViewById<MaterialButton>(R.id.materialButton)

        createEventButton.setOnClickListener {
            val eventNameTextView = view.findViewById<TextInputEditText>(R.id.eventNameTextView)
            val eventPlace = view.findViewById<TextInputEditText>(R.id.eventPlace)
            val eventDate = view.findViewById<TextInputEditText>(R.id.eventDate)
            val cityTextView = view.findViewById<TextInputEditText>(R.id.cityTextView)
            //    val eventGuests = view.findViewById<TextInputEditText>(R.id.eventGuests)
            // val guests = eventGuests.text.toString()
            val eventDescription = view.findViewById<TextInputEditText>(R.id.eventDescription)
            val spots = numberPicker.value


            val eventName = eventNameTextView.text.toString()
            val place = eventPlace.text.toString()
            val date = eventDate.text.toString()
            val description = eventDescription.text.toString()
            val city = cityTextView?.text?.toString()

            if (eventName.isEmpty()) {
                eventNameTextView.error = "Event name is required"
                return@setOnClickListener
            }
            if (place.isEmpty()) {
                eventPlace.error = "Place is required"
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                eventDate.error = "Date is required"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                eventDescription.error = "Description is required"
                return@setOnClickListener
            }
            if (selectedCategory == null) {
                Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentUserId = AuthUtil.getCurrentUserId()
            UserDetailsFetcher.fetchUserDetails(currentUserId) { userDetails, exception ->
                if (exception != null) {
                    Log.e("AddEvent", "Error fetching user details", exception)
                    return@fetchUserDetails
                }
                if (userDetails == null) {
                    Log.e("AddEvent", "User details are null")
                    return@fetchUserDetails
                }
                val attendedPeopleProfilePictureUrls = mutableListOf(userDetails.profileImageUrl)

                val event = hashMapOf(
                    "title" to eventName,
                    "location" to place,
                    "city" to city,
                    "day" to chosenDay,
                    "date" to chosenDate,
                    "time" to chosenTime,
                    "spots" to spots,
                    "description" to description,
                    "eventType" to selectedCategory,
                    "authorId" to currentUserId,
                    "authorProfileImageUrl" to userDetails.profileImageUrl,
                    "authorFirstName" to userDetails.firstName,
                    "authorLastName" to userDetails.lastName,
                    "attendedPeopleProfilePictureUrls" to attendedPeopleProfilePictureUrls

                )

                firestore.collection("events").add(event)
                    .addOnSuccessListener {
                        eventNameTextView.text = null
                        eventPlace.text = null
                        eventDate.text = null
                        //        eventGuests.text = null
                        eventDescription.text = null
                        selectedCategory = null
                        selectedImageView?.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                android.R.color.transparent
                            )
                        )
                        selectedImageView = null
                        Toast.makeText(context, "Event added successfully", Toast.LENGTH_SHORT)
                            .show()
                    }

                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Error adding event: ${e.message}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddEventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }

}
