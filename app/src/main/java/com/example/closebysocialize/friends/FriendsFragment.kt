package com.example.closebysocialize.friends

import android.os.Bundle
import android.util.Log
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

class FriendsFragment : Fragment(), FriendsAdapter.FriendSelectionListener {
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var removeFriendButton: Button

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
        friendsAdapter = FriendsAdapter(listOf(), this)
        friendsAdapter.selectionListener = this
        friendsRecyclerView.adapter = friendsAdapter
        friendsRecyclerView.layoutManager = LinearLayoutManager(context)

        loadFriends()
        val addFriendButton = view.findViewById<Button>(R.id.addFriendButton)
        addFriendButton.setOnClickListener {
            openAddFriendFragment()
        }

        removeFriendButton = view.findViewById<Button>(R.id.removeFriendButton)
        removeFriendButton.visibility = View.GONE
        removeFriendButton.setOnClickListener {
            removeSelectedFriends()
            removeFriendButton.visibility = View.GONE
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

    private fun removeSelectedFriends() {
        val selectedFriends = friendsAdapter.friends.filter { it.isSelected }
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        selectedFriends.forEach { friend ->
            db.collection("users").document(userId).collection("friends").document(friend.id)
                .delete()
                .addOnSuccessListener {
                    Log.d("FriendsFragment", "Friend successfully deleted from Firestore")
                }
                .addOnFailureListener { e ->
                    Log.w("FriendsFragment", "Error deleting friend from Firestore", e)
                }
        }
        loadFriends()
    }
    override fun onSelectionChanged() {
        val anySelected = friendsAdapter.friends.any { it.isSelected }
        val addFriendButton = view?.findViewById<Button>(R.id.addFriendButton)
        addFriendButton?.visibility = if (anySelected) View.GONE else View.VISIBLE
        removeFriendButton?.visibility = if (anySelected) View.VISIBLE else View.GONE
    }

}