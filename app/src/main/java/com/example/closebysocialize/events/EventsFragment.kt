package com.example.closebysocialize.events
import android.health.connect.datatypes.ExerciseRoute
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.AddEventFragment
import com.example.closebysocialize.R
import com.example.closebysocialize.chat.ChatFragment
import com.example.closebysocialize.dataClass.Event
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.FirestoreUtils.fetchAllEvents
import com.example.closebysocialize.utils.FragmentUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.type.LatLng
import android.location.Location



class EventsFragment : Fragment() {
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0
    private lateinit var recyclerView: RecyclerView
    private var eventsAdapter = EventsAdapter(emptyList())
    private lateinit var savedTextView: TextView
    private lateinit var allTextView: TextView
    private lateinit var attendedTextView: TextView
    private lateinit var filterImageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        filterImageView = view.findViewById(R.id.filterImageView)
        savedTextView = view.findViewById(R.id.savedTextView)
        allTextView = view.findViewById(R.id.allTextView)
        attendedTextView = view.findViewById(R.id.attendedTextView)

        setFilterTextViewsListeners()

        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = eventsAdapter

        FirebaseApp.initializeApp(requireContext())

        eventsAdapter.chatImageViewClickListener = { event ->
            val containerId = R.id.fragment_container
            val args = Bundle().apply {
            }
            activity?.let {
                if (it is AppCompatActivity) {
                    FragmentUtils.switchFragment(it, containerId, ChatFragment::class.java, args)
                }
            }
        }
        val floatingActionButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener {
            floatingActionButton.animate().scaleX(0.7f).scaleY(0.7f).setDuration(200).withEndAction {
                floatingActionButton.animate().scaleX(1f).scaleY(1f).setDuration(200).withEndAction {
                    FragmentUtils.switchFragment(
                        activity = activity as AppCompatActivity,
                        containerId = R.id.fragment_container,
                        fragmentClass = AddEventFragment::class.java
                    )

                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreUtils.fetchSavedEventsByUser(userId, onSuccess = { savedEvents ->
            eventsAdapter.savedEventsIds.clear()
            eventsAdapter.savedEventsIds.addAll(savedEvents.map { it.id })
            eventsAdapter.notifyDataSetChanged()
        }, onFailure = { exception ->
            Log.e("EventsFragment", "Error fetching saved events", exception)
        })



        initializeDefaultSelection()
        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        eventsAdapter = EventsAdapter(listOf())
        recyclerView.adapter = eventsAdapter
        eventsAdapter.chatImageViewClickListener = { eventId ->
            val chatFragment = ChatFragment.newInstance(eventId)
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragment_container, chatFragment)
                ?.addToBackStack(null)
                ?.commit()
        }


        val showOnlyMyEvents = arguments?.getBoolean("showOnlyMyEvents", false) ?: false
        if (showOnlyMyEvents) {
            setAllFiltersToDefaultSize()
        } else {
            initializeDefaultSelection()
        }
        setFilterTextViewsListeners()
     //   fetchDataFromFirestore()
        fetchUserSavedEvents()

      //  filterImageView.setOnClickListener { showFilterMenu(it) }
    }

    private fun setAllFiltersToDefaultSize() {
        val defaultTextSize = 14f
        savedTextView.textSize = defaultTextSize
        allTextView.textSize = defaultTextSize
        attendedTextView.textSize = defaultTextSize
    }

    /*
    private fun showFilterMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.sort_by_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_time_created -> {
                    fetchAllEvents(
                        ::updateRecyclerView,
                        ::handleError
                    )
                }

                R.id.filter_event_time -> {
                    fetchEventsByHappeningTime(::updateRecyclerView, ::handleError)
                }

                R.id.filter_distance -> {
                    val userLocation = com.google.android.gms.maps.model.LatLng(userLat, userLng)
                    fetchEventsByDistance(userLocation, ::updateRecyclerView, ::handleError)
                }
            }
            true
        }
    }


        fun fetchEventsByHappeningTime(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
            .orderBy("eventTime", Query.Direction.ASCENDING) // Assuming 'eventTime' is your field
            .get()
            .addOnSuccessListener { snapshot ->
                val eventsList = snapshot.documents.mapNotNull { document ->
                    val event = document.toObject(Event::class.java)
                    event?.id = document.id
                    event
                }
                onSuccess(eventsList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

     */
    /*
    fun fetchEventsByDistance(userLocation: com.google.android.gms.maps.model.LatLng, onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
        fetchAllEvents({ eventsList ->
            val sortedByDistance = eventsList.sortedBy { event ->
                val eventLocation = event.place_coordinates
                if (eventLocation != null) {
                    val eventLatLng = com.google.android.gms.maps.model.LatLng(eventLocation.latitude, eventLocation.longitude)
                    calculateDistance(userLocation, eventLatLng)
                } else {

                    Float.MAX_VALUE
                }
            }
            onSuccess(sortedByDistance)
        }, onFailure)
    }


    fun calculateDistance(userLocation: LatLng, eventLocation: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            eventLocation.latitude, eventLocation.longitude,
            results)
        return results[0]
    }


    private fun fetchDataFromFirestore() {
        val showOnlyMyEvents = this.arguments?.getBoolean("showOnlyMyEvents", false) ?: false
        if (showOnlyMyEvents) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                FirestoreUtils.fetchUserEvents(
                    userId = currentUserId,
                    onSuccess = { eventsList -> updateRecyclerView(eventsList) },
                    onFailure = { exception -> Log.d("EventsFragment", "Error fetching user's events: ", exception) }
                )
            }
        } else {
            FirestoreUtils.fetchAllEvents(
                onSuccess = { eventsList -> updateRecyclerView(eventsList) },
                onFailure = { exception -> Log.d("EventsFragment", "Error fetching events: ", exception) }
            )
        }
    }

     */




    private fun updateRecyclerView(eventsList: List<Event>) {
        eventsAdapter.updateData(eventsList)
    }


    private fun initializeDefaultSelection() {
        val defaultTextSize = 14f
        val selectedTextSize = 18f
        resetTextViewsSize(defaultTextSize)
        allTextView.textSize = selectedTextSize
        filterData("all")
    }

    private fun setFilterTextViewsListeners() {
        val defaultTextSize = 14f
        val selectedTextSize = 18f
        val clickListener = View.OnClickListener { view ->
            resetTextViewsSize(defaultTextSize)
            if (view is TextView) {
                view.textSize = selectedTextSize
                when (view.id) {
                    R.id.savedTextView -> filterData("saved")
                    R.id.allTextView -> filterData("all")
                    R.id.attendedTextView -> filterData("attended")
                }
            }
        }
        savedTextView.setOnClickListener(clickListener)
        allTextView.setOnClickListener(clickListener)
        attendedTextView.setOnClickListener(clickListener)
    }
    private fun resetTextViewsSize(size: Float) {
        savedTextView.textSize = size
        allTextView.textSize = size
        attendedTextView.textSize = size
    }
    private fun filterData(filterType: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        when (filterType) {
            "all" -> FirestoreUtils.fetchAllEvents(::updateRecyclerView, ::handleError)
            "saved" -> FirestoreUtils.fetchSavedEventsByUser(userId, ::updateRecyclerView, ::handleError)
            "attending" -> FirestoreUtils.fetchAttendingEventsByUser(userId, ::updateRecyclerView, ::handleError)
        }
    }

    private fun fetchUserSavedEvents() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreUtils.fetchSavedEventsByUser(userId,
            onSuccess = { events -> updateRecyclerView(events) },
            onFailure = { exception -> Log.w("EventsFragment", "Error fetching saved events", exception) }
        )
    }
    private fun handleError(exception: Exception) {
        Log.e("EventsFragment", "Error fetching data from Firestore", exception)
    }



}

