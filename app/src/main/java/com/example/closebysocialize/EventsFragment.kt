package com.example.closebysocialize

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
import com.example.closebysocialize.util.FragmentUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        db.collection("events").get().addOnSuccessListener { result ->
            val eventsList = result.toObjects(Event::class.java)
            eventsAdapter.updateData(eventsList)
        }.addOnFailureListener { exception ->
            Log.d("EventsFragment", "Error getting documents: ", exception)
        }
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