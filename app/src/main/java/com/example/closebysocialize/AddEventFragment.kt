package com.example.closebysocialize

import AuthUtil
import UserAdapter
import UserDetailsFetcher
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.dataClass.Users
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddEventFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var chosenDay: String? = null
    private var chosenDate: String? = null
    private var chosenTime: String? = null
    private val PLACE_SEARCH_REQUEST_CODE = 1
    private val CITY_SEARCH_REQUEST_CODE = 2
    private var eventPlaceCoordinates: LatLng? = null
    private var eventCityCoordinates: LatLng? = null
    private lateinit var eventPlace: EditText
    private lateinit var cityTextView: EditText
    private val PICK_IMAGE_REQUEST = 3
    private var imageUri: Uri? = null
    private lateinit var userAdapter: UserAdapter
    private lateinit var firestore: FirebaseFirestore
    private var taggedUsers = mutableListOf<String>()
    private lateinit var recyclerViewFindUsers: RecyclerView



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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PLACE_SEARCH_REQUEST_CODE, CITY_SEARCH_REQUEST_CODE -> {
                    val placeName = data?.getStringExtra("place_name")
                    val placeCoordinates = data?.getParcelableExtra<LatLng>("place_coordinates")
                    when (requestCode) {
                        PLACE_SEARCH_REQUEST_CODE -> {
                            eventPlace.setText(placeName)
                            eventPlaceCoordinates = placeCoordinates
                        }

                        CITY_SEARCH_REQUEST_CODE -> {
                            cityTextView.setText(placeName)
                            eventCityCoordinates = placeCoordinates
                        }
                    }
                }

                PICK_IMAGE_REQUEST -> {
                    Log.d("AddEvent", "onActivityResult: data: $data")
                    imageUri = data?.data
                    Log.d("AddEvent", "onActivityResult: imageUri: $imageUri")
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewFindUsers = view.findViewById<RecyclerView>(R.id.recyclerViewFindUsers)
        val gridLayout = view.findViewById<GridLayout>(R.id.gridLayout)
        var selectedImageView: ImageView? = null
        var selectedCategory: String? = null
        eventPlace = view.findViewById(R.id.eventPlace)
        cityTextView = view.findViewById(R.id.cityTextView)


        val numberPicker = view.findViewById<NumberPicker>(R.id.spotPicker)
        numberPicker.maxValue = 20
        numberPicker.minValue = 1
        numberPicker.value = 4

        eventPlace.setOnClickListener {
            val intent = Intent(context, EventMapSearch::class.java)
            startActivityForResult(intent, PLACE_SEARCH_REQUEST_CODE)
        }

        cityTextView.setOnClickListener {
            val intent = Intent(context, EventMapSearch::class.java)
            startActivityForResult(intent, CITY_SEARCH_REQUEST_CODE)
        }

        val imageAdd = view.findViewById<ImageView>(R.id.imageAdd)
        imageAdd.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }


        val users = listOf<Users>() ?: listOf()
        val taggedUsers = mutableListOf<String>() ?: mutableListOf()
        val eventGuests = view.findViewById<EditText>(R.id.eventGuests) ?: EditText(context)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroup) ?: ChipGroup(context)
        recyclerViewFindUsers.layoutManager = LinearLayoutManager(context)
        userAdapter = UserAdapter(listOf(), taggedUsers, eventGuests, chipGroup)
        recyclerViewFindUsers.adapter = userAdapter




        eventGuests.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val parts = s.toString().split(",")
                val query = parts.last().trim()
                if (query.isEmpty()) {
                    userAdapter.updateData(emptyList())
                    recyclerViewFindUsers.visibility = View.GONE
                } else {
                    fetchUsers(query) { fetchedUsers ->
                        userAdapter.updateData(fetchedUsers)
                        recyclerViewFindUsers.visibility = View.VISIBLE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })












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
        eventPlace.isFocusable = false
        eventPlace.isClickable = true
        cityTextView.isFocusable = false
        cityTextView.isClickable = true

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
            val eventGuests = view.findViewById<TextInputEditText>(R.id.eventGuests)

            val guests = eventGuests.text.toString()
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
                if (imageUri != null) {
                    uploadImage(imageUri!!) { imageUrl: String ->
                        val event = hashMapOf(
                            "title" to eventName,
                            "location" to eventPlace.text.toString(),
                            "place_coordinates" to eventPlaceCoordinates,
                            "city" to cityTextView.text.toString(),
                            "city_coordinates" to eventCityCoordinates,
                            "day" to chosenDay,
                            "imageUrl" to imageUrl,
                            "date" to chosenDate,
                            "taggedUsers" to taggedUsers,
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
                                cityTextView.text = null
                                eventPlace.text = null
                                // eventGuests.text = null
                                eventDescription.text = null
                                selectedCategory = null
                                taggedUsers.clear()
                                selectedImageView?.setBackgroundColor(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        android.R.color.transparent
                                    )
                                )


                                selectedImageView = null
                                Toast.makeText(
                                    context,
                                    "Event added successfully",
                                    Toast.LENGTH_SHORT
                                )
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
        }
    }


    private fun uploadImage(imageUri: Uri, onSuccess: ((imageUrl: String) -> Unit)? = null) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    Toast.makeText(
                        context,
                        "Image uploaded successfully: $downloadUri",
                        Toast.LENGTH_SHORT
                    ).show()
                    onSuccess?.invoke(downloadUri.toString())
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Error uploading image: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun fetchUsers(query: String, onSuccess: (List<Users>) -> Unit) {
        if (query.isEmpty()) return
        val searchQuery = query.split(" ").joinToString(" ") { it.capitalize() }
        val searchQueryStart = searchQuery
        val searchQueryEnd = searchQuery + '\uf8ff'
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .orderBy("name")
            .startAt(searchQueryStart)
            .endAt(searchQueryEnd)
            .get()
            .addOnSuccessListener { documents ->
                val userList = documents.mapNotNull { it.toObject(Users::class.java) }
                onSuccess(userList)
            }
            .addOnFailureListener {
                // Handle failure here
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
