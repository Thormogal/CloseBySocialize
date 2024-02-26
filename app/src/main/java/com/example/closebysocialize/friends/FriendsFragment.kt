package com.example.closebysocialize.friends

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment() {
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView)
        friendsAdapter = FriendsAdapter(listOf())
        friendsRecyclerView.adapter = friendsAdapter
        friendsRecyclerView.layoutManager = LinearLayoutManager(context)

        loadFriends()
        val addFriendButton = view.findViewById<Button>(R.id.addFriendButton)
        addFriendButton.setOnClickListener {
            openAddFriendFragment()
        }

    }

    override fun onResume() {
        super.onResume()
        loadFriends()
    }

    private fun loadFriends() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .addOnSuccessListener { documents ->
                val friendsList = documents.mapNotNull { it.toObject(Friend::class.java) }
                friendsAdapter.updateData(friendsList)
            }
            .addOnFailureListener { exception ->

            }
    }

    private fun openAddFriendFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddFriendFragment())
            .addToBackStack(null)
            .commit()
    }

}