package com.example.closebysocialize.events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.AddEventFragment
import com.example.closebysocialize.R
import com.example.closebysocialize.chat.ChatFragment
import com.example.closebysocialize.dataClass.Event
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.FragmentUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.button.MaterialButtonToggleGroup
class EventsFragment : Fragment(), EventsAdapter.EventInteractionListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapter
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var filterImageView: ImageView
    private lateinit var progressBar: ProgressBar
    private var savedEventsIds: MutableSet<String> = mutableSetOf()
    private var attendingEventsIds: MutableSet<String> = mutableSetOf()
    private var allEventsList: List<Event> = emptyList()
    private var attendingEventsList: List<Event> = emptyList()
    private var savedEventsList: List<Event> = emptyList()
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString("eventId")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        initializeViews(view)
        progressBar = view.findViewById(R.id.progressBar)
        setupRecyclerView()
        setupFloatingActionButton(view)
        setFilterButtonsListeners()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        eventsAdapter = EventsAdapter(attendingEventsList, allEventsList, savedEventsList, userId, savedEventsIds, attendingEventsIds, this)
        recyclerView.adapter = eventsAdapter
        eventsAdapter.chatImageViewClickListener = { eventId ->
            openChatForEvent(eventId)
        }

        filterData("all")
    }

    private fun openChatForEvent(eventId: String) {
        val args = Bundle().apply {
            putString("eventId", eventId)
        }
        FragmentUtils.switchFragment(
            activity = requireActivity() as AppCompatActivity,
            containerId = R.id.fragment_container,
            fragmentClass = ChatFragment::class.java,
            args = args
        )
    }


    private fun initializeViews(view: View) {
        filterImageView = view.findViewById(R.id.filterImageView)
        toggleGroup = view.findViewById(R.id.toggleButtonGroup)
        recyclerView = view.findViewById(R.id.eventsRecyclerView)
        FirebaseApp.initializeApp(requireContext())
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupFloatingActionButton(view: View) {
        val floatingActionButton: FloatingActionButton = view.findViewById(R.id.floatingActionButton)
        floatingActionButton.setOnClickListener {
            floatingActionButton.animate().scaleX(0.7f).scaleY(0.7f).setDuration(200).withEndAction {
                FragmentUtils.switchFragment(
                    activity = activity as AppCompatActivity,
                    containerId = R.id.fragment_container,
                    fragmentClass = AddEventFragment::class.java
                )
            }
        }
    }

    private fun setFilterButtonsListeners() {
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.savedButton -> filterData("saved")
                    R.id.allButton -> filterData("all")
                    R.id.attendedButton -> filterData("attending")
                }
            }
        }
    }

    private fun filterData(filterType: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        when (filterType) {
            "all" -> FirestoreUtils.fetchAllEvents(::updateAllEventsList, ::handleError)
            "saved" -> FirestoreUtils.fetchSavedEventsByUser(userId, ::updateSavedEventsList, ::handleError)
            "attending" -> FirestoreUtils.fetchAttendingEventsByUser(userId, ::updateAttendingEventsList, ::handleError)
        }
    }

    private fun updateAllEventsList(eventsList: List<Event>) {
        allEventsList = eventsList
        eventsAdapter.updateData(allEventsList)
    }

    private fun updateAttendingEventsList(eventsList: List<Event>) {
        attendingEventsList = eventsList
        eventsAdapter.updateData(attendingEventsList)
    }

    private fun updateSavedEventsList(eventsList: List<Event>) {
        savedEventsList = eventsList
        eventsAdapter.updateData(savedEventsList)
    }

    override fun onToggleSaveEvent(eventId: String, isCurrentlySaved: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreUtils.toggleSavedEvent(userId, eventId, !isCurrentlySaved, onSuccess = {
            eventsAdapter.notifyDataSetChanged()
        }, onFailure = { e ->
            Log.e("EventsFragment", "Failed to toggle event save state", e)
        })
    }

    private fun handleError(exception: Exception) {
        Log.e("EventsFragment", "Error fetching data from Firestore", exception)
    }
}

