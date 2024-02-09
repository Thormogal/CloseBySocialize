package com.example.closebysocialize.events

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.chat.ChatFragment
import com.example.closebysocialize.dataClass.Event
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.FragmentUtils
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.QuerySnapshot


class EventsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private var eventsAdapter = EventsAdapter(emptyList())
    private lateinit var savedTextView: TextView
    private lateinit var allTextView: TextView
    private lateinit var attendedTextView: TextView
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
            // TODO not working    putString("eventId", event.id)
            }
            activity?.let {
                if (it is AppCompatActivity) {
                    FragmentUtils.switchFragment(it, containerId, ChatFragment::class.java, args)
                }
            }
        }
        val floatingActionButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener {
            // animation on click, can remove if you want
            floatingActionButton.animate().scaleX(0.7f).scaleY(0.7f).setDuration(200).withEndAction {
                floatingActionButton.animate().scaleX(1f).scaleY(1f).setDuration(200).withEndAction {
                    /*  TODO switch ADddFragment
                    FragmentUtils.switchFragment(
                        activity = activity as AppCompatActivity,
                        containerId = R.id.fragment_container,
                        fragmentClass = AddEventFragment::class.java
                    )
                    */

                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeDefaultSelection()
        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        eventsAdapter = EventsAdapter(listOf())
        recyclerView.adapter = eventsAdapter
        eventsAdapter.chatImageViewClickListener = { eventName ->
            val args = Bundle().apply {
                putString("eventName", eventName)
            }
            activity?.let {
                if (it is AppCompatActivity) {
                    val containerId = R.id.fragment_container
                    FragmentUtils.switchFragment(it, containerId, ChatFragment::class.java, args)
                }
            }
        }
        val showOnlyMyEvents = arguments?.getBoolean("showOnlyMyEvents", false) ?: false
        if (showOnlyMyEvents) {
            setAllFiltersToDefaultSize()
        } else {
            initializeDefaultSelection()
        }
        setFilterTextViewsListeners()
        fetchDataFromFirestore()
        fetchUserSavedEvents()
    }

    private fun setAllFiltersToDefaultSize() {
        val defaultTextSize = 14f
        savedTextView.textSize = defaultTextSize
        allTextView.textSize = defaultTextSize
        attendedTextView.textSize = defaultTextSize
    }

    private fun fetchDataFromFirestore(showOnlyMyEvents: Boolean = this.arguments?.getBoolean("showOnlyMyEvents", false) ?: false) {
        if (showOnlyMyEvents) {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            FirestoreUtils.fetchUserEvents(
                userId = currentUserId,
                onSuccess = { eventsList -> updateRecyclerView(eventsList) },
                onFailure = { exception -> Log.d("EventsFragment", "Error fetching user's events: ", exception) }
            )
        } else {
            FirestoreUtils.fetchEventsForAllUsers(
                onSuccess = { eventsList -> updateRecyclerView(eventsList) },
                onFailure = { exception -> Log.d("EventsFragment", "Error fetching events: ", exception) }
            )
        }
    }


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
            "all" -> FirestoreUtils.fetchEventsForAllUsers(::updateRecyclerView, ::handleError)
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

