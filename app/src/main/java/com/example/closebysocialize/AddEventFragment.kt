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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.dataClass.Users
import com.example.closebysocialize.events.EventsFragment
import com.example.closebysocialize.utils.FragmentUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AddEventFragment : Fragment() {
    private val placeSearchRequestCode = 1
    private val citySearchRequestCode = 2
    private val pickImageRequestCode = 3
    private var param1: String? = null
    private var param2: String? = null
    private var chosenDay: String? = null
    private var chosenDate: String? = null
    private var chosenTime: String? = null
    private var imageUri: Uri? = null
    private var eventPlaceCoordinates: LatLng? = null
    private var eventCityCoordinates: LatLng? = null
    private var taggedUsers = mutableListOf<String>()
    private lateinit var eventPlace: EditText
    private lateinit var eventGuests: EditText
    private lateinit var cityTextView: EditText
    private lateinit var userAdapter: UserAdapter
    private lateinit var firestore: FirebaseFirestore
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
        eventGuests = EditText(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                placeSearchRequestCode, citySearchRequestCode -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    val placeName = place.name
                    val placeCoordinates = place.latLng
                    when (requestCode) {
                        placeSearchRequestCode -> {
                            eventPlace.setText(placeName)
                            eventPlaceCoordinates = placeCoordinates
                        }

                        citySearchRequestCode -> {
                            cityTextView.setText(placeName)
                            eventCityCoordinates = placeCoordinates
                        }
                    }
                }

                pickImageRequestCode -> {
                    Log.d("AddEvent", "onActivityResult: data: $data")
                    imageUri = data?.data
                    Log.d("AddEvent", "onActivityResult: imageUri: $imageUri")
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            recyclerViewFindUsers = view.findViewById(R.id.recyclerViewFindUsers)
            val gridLayout = view.findViewById<GridLayout>(R.id.addEventGridLayout)
            var selectedImageView: ImageView? = null
            var selectedCategory: String? = null
            eventPlace = view.findViewById(R.id.eventPlace)
            cityTextView = view.findViewById(R.id.cityTextView)

            val numberPicker = view.findViewById<NumberPicker>(R.id.spotPicker)
            numberPicker.maxValue = 20
            numberPicker.minValue = 1
            numberPicker.value = 4

            eventPlace.setOnClickListener {
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("SE")
                    .build(requireContext())
                startActivityForResult(intent, placeSearchRequestCode)
            }

            cityTextView.setOnClickListener {
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountry("SE")
                    .build(requireContext())
                startActivityForResult(intent, citySearchRequestCode)
            }

            val imageAdd = view.findViewById<ImageView>(R.id.imageAdd)
            imageAdd.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, pickImageRequestCode)
            }


            val users = listOf<Users>()
            val taggedUsers = mutableListOf<String>()
            val eventGuests = view.findViewById(R.id.eventGuests) ?: EditText(context)
            val chipGroup = view.findViewById(R.id.chipGroup) ?: ChipGroup(context)

            recyclerViewFindUsers.layoutManager = LinearLayoutManager(context)
            userAdapter = UserAdapter(listOf(), taggedUsers, eventGuests, chipGroup)
            userAdapter.setSearchText("")
            recyclerViewFindUsers.adapter = userAdapter

            userAdapter.callback = object : UserAdapter.UserAdapterCallback {
                override fun onUserRemoved() {
                    fetchAndUpdateUsers()
                }
            }




        eventGuests.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().split(",").last().trim()

                if (query.isEmpty()) {
                    userAdapter.updateData(emptyList())
                    recyclerViewFindUsers.visibility = View.GONE
                } else {
                    fetchUsers(query) { fetchedUsers ->
                        val filteredUsers = fetchedUsers.filterNot { user ->
                            taggedUsers.contains(user.id)
                        }
                        userAdapter.updateData(filteredUsers)
                        recyclerViewFindUsers.visibility = if (filteredUsers.isEmpty()) View.GONE else View.VISIBLE
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

        val createEventButton = view.findViewById<MaterialButton>(R.id.materialSubmitButton)

        createEventButton.setOnClickListener {


            val defaultImageUrlMap = mapOf(
                "reading" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2FbookCircle.png?alt=media&token=53824fa6-0de1-4914-a5fe-a11e252db1f2",
                "cafe" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Fcoffee.png?alt=media&token=7f5d8886-ff9b-4a6c-aa04-4cc35b868c65",
                "cooking" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Fcooking_background.png?alt=media&token=f343511e-70b2-4c67-9ad3-f417e82f22a9",
                "traveling" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Ftravel.png?alt=media&token=0818e63e-f220-4f31-9d33-387762891f3c",
                "gardening" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Fgardening.png?alt=media&token=e7a73244-ce8c-4f76-9d0e-dfe10955087c",
                "theatre" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Fculture.png?alt=media&token=cb7f3737-c865-44f3-a9c9-7e69ca7ca57d",
                "babyStroll" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2FbabyStroll.png?alt=media&token=fd1e0d87-d800-4972-a7e6-0a04814ca033",
                "lunch" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Frestaurant.png?alt=media&token=8ad033da-1fb6-43e4-8f4e-7df9e518e438",
                "cinema" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Fcinema.png?alt=media&token=3ce88d26-fd61-4d99-9f88-4e9c8a95ca29",
                "gaming" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Fgaming.png?alt=media&token=313abf91-abdf-4141-8c8a-d2cddf5af7b8",
                "sports" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Fsport.png?alt=media&token=3e303f47-9a51-4667-a8f4-1d192504b9a3",
                "dogstroll" to "https://firebasestorage.googleapis.com/v0/b/closebysocialize.appspot.com/o/Backgrounds%2Fdog.png?alt=media&token=c1ba8ac4-e368-440a-bf81-e4c64d161350",
            )

            val eventNameTextView = view.findViewById<TextInputEditText>(R.id.eventNameTextView)
            val eventPlace = view.findViewById<TextInputEditText>(R.id.eventPlace)
            val eventDate = view.findViewById<TextInputEditText>(R.id.eventDate)
            val cityTextView = view.findViewById<TextInputEditText>(R.id.cityTextView)
            val eventGuests = view.findViewById<TextInputEditText>(R.id.eventGuests)

            val guests = eventGuests.text.toString()
            val currentUserId = AuthUtil.getCurrentUserId()
            val eventDescription = view.findViewById<TextInputEditText>(R.id.eventDescription)
            val spots = numberPicker.value
            val placeGeoPoint = eventPlaceCoordinates?.let { GeoPoint(it.latitude, it.longitude) }
            val cityGeoPoint = eventCityCoordinates?.let { GeoPoint(it.latitude, it.longitude) }


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

            val defaultImageUrl = defaultImageUrlMap[selectedCategory] ?: ""

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
                var imageUrl: String? = null
                if (imageUri != null) {
                    uploadImage(imageUri!!) { uploadedImageUrl: String ->
                        imageUrl = uploadedImageUrl
                    }
                } else {
                    imageUrl = defaultImageUrl
                }

                val event = hashMapOf(
                    "title" to eventName,
                    "location" to eventPlace.text.toString(),
                    "place_coordinates" to placeGeoPoint,
                    "city" to cityTextView.text.toString(),
                    "city_coordinates" to cityGeoPoint,
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
                    "attendedPeopleProfilePictureUrls" to attendedPeopleProfilePictureUrls,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("events").add(event)
                    .addOnSuccessListener { documentReference ->
                        val eventId = documentReference.id!!
                        if (currentUserId != null) {
                            addUserToEventAttendees(currentUserId, eventId)
                        }

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
                        activity?.let {
                            FragmentUtils.switchFragment(
                                it as AppCompatActivity,
                                R.id.fragment_container,
                                EventsFragment::class.java
                            )
                        }
                    }
            }

            var imageUrl: String? = null
            if (imageUri != null) {
                uploadImage(imageUri!!) { uploadedImageUrl: String ->
                    imageUrl = uploadedImageUrl
                    saveEventToFirestore(imageUrl)
                }
            } else {
                imageUrl = defaultImageUrl
                saveEventToFirestore(imageUrl)
            }

        }
    }

    private fun saveEventToFirestore(imageUrl: String?) {
        val event = hashMapOf(
            "imageUrl" to imageUrl
        )
        firestore.collection("events").add(event)
    }

    private fun fetchAndUpdateUsers() {
        if(this::eventGuests.isInitialized) {
            val query = eventGuests.text.toString().split(",").last().trim()
            fetchUsers(query) { fetchedUsers ->
                val filteredUsers = fetchedUsers.filterNot { user ->
                    taggedUsers.contains(user.id)
                }
                userAdapter.updateData(filteredUsers)
                recyclerViewFindUsers.visibility = if (filteredUsers.isEmpty()) View.GONE else View.VISIBLE
            }
        } else {
            Log.d("AddEventFragment", "eventGuests have not been initialized yet.")
        }
    }


    private fun addUserToEventAttendees(userId: String, eventId: String) {
        val userRef = firestore.collection("users").document(userId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentAttendingEvents =
                snapshot.get("attendingEvents") as? MutableList<String> ?: mutableListOf()
            currentAttendingEvents.add(eventId)
            transaction.update(userRef, "attendingEvents", currentAttendingEvents)
        }.addOnSuccessListener {
            Log.d("AddEvent", "User attending events updated successfully.")
        }.addOnFailureListener { e ->
            Log.e("AddEvent", "Error updating user attending events", e)
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
