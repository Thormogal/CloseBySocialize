package com.example.closebysocialize.events
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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


class EventsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private var eventsAdapter = EventsAdapter(emptyList())
  private lateinit var toggleGroup: MaterialButtonToggleGroup

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
        toggleGroup = view.findViewById(R.id.toggleButtonGroup)

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
        setFilterButtonsListeners()

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

        toggleGroup.check(R.id.allButton)
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

    }
    private fun setFilterButtonsListeners() {
        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.savedButton -> filterData("saved")
                    R.id.allButton -> filterData("all")
                    R.id.attendedButton -> filterData("attending")
                }
            }
        }
    }


    private fun updateRecyclerView(eventsList: List<Event>) {
        eventsAdapter.updateData(eventsList)
    }

    private fun filterData(filterType: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        when (filterType) {
            "all" -> FirestoreUtils.fetchAllEvents(::updateRecyclerView, ::handleError)
            "saved" -> FirestoreUtils.fetchSavedEventsByUser(userId, ::updateRecyclerView, ::handleError)
            "attending" -> FirestoreUtils.fetchAttendingEventsByUser(userId, ::updateRecyclerView, ::handleError)
        }
    }

    private fun handleError(exception: Exception) {
        Log.e("EventsFragment", "Error fetching data from Firestore", exception)
    }



}

