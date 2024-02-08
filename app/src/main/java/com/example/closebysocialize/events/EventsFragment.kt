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
import com.example.closebysocialize.utils.FragmentUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.floatingactionbutton.FloatingActionButton


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
            floatingActionButton.animate().scaleX(0.7f).scaleY(0.7f).setDuration(200).withEndAction {
                floatingActionButton.animate().scaleX(1f).scaleY(1f).setDuration(200).withEndAction {
/*  TODO
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
        fetchDataFromFirestore()
    }

    private fun fetchDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val eventsList = mutableListOf<Event>()
        val usersRef = db.collection("users")
        usersRef.get().addOnSuccessListener { usersSnapshot ->
            if (usersSnapshot.documents.isEmpty()) {
                updateRecyclerView(eventsList)
            }
            for (userDocument in usersSnapshot) {
                userDocument.reference.collection("Event")
                    .get()
                    .addOnSuccessListener { eventsSnapshot ->
                        for (eventDocument in eventsSnapshot) {
                            val event = eventDocument.toObject(Event::class.java)
                            eventsList.add(event)
                        }
                        updateRecyclerView(eventsList)
                    }
                    .addOnFailureListener { exception ->
                        Log.d("EventsFragment", "Error getting events for user ${userDocument.id}: ", exception)
                    }
            }
        }.addOnFailureListener { exception ->
            Log.d("EventsFragment", "Error getting users: ", exception)
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
        /*    TODO (fetch from firestore or filter existing cotent
        filter for savedTextView
        filter for allTextView
        filter for attendedTextView
        */
    }

}