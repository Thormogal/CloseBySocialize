package com.example.closebysocialize.friends

import FriendsAdapter
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Users
import com.example.closebysocialize.message.FireBaseMessagingService.Companion.TAG
import com.example.closebysocialize.utils.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendFragment : Fragment() {
    private lateinit var searchEditText: EditText
    private lateinit var recyclerViewFindFriends: RecyclerView
    private lateinit var userAdapter: UserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerViewFindFriends = view.findViewById(R.id.recyclerViewFindFriends)

        userAdapter = UserAdapter(listOf())
        recyclerViewFindFriends.adapter = userAdapter
        recyclerViewFindFriends.layoutManager = LinearLayoutManager(context)

        userAdapter.onItemClick = { user ->
            //addUserAsFriend(user)
            addUserAsFriend(user)

        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchUsers(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }


    private fun searchUsers(query: String) {
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
                userAdapter.updateData(userList)
            }
            .addOnFailureListener {
            }
    }

    private fun addUserAsFriend(user: Users) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        if (user.id.isNullOrEmpty()) {
            Toast.makeText(context, "Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }
        val db = FirebaseFirestore.getInstance()
        val friendData = hashMapOf(
            "id" to user.id,
            "email" to user.email,
            "profileImageUrl" to user.profileImageUrl,
            "name" to user.name
        )
        val messageData = hashMapOf(
            "senderId" to currentUser.uid,
            "recipientId" to user.id,
            "content" to "Hello!"
        )
        db.collection("users")
            .document(currentUser.uid)
            .collection("friends")
            .document(user.id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    db.collection("users")
                        .document(currentUser.uid)
                        .collection("friends")
                        .document(user.id)
                        .set(friendData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Friend added successfully", Toast.LENGTH_SHORT).show()
                            val conversationRef = db.collection("conversations").document()
                            conversationRef.collection("messages").add(messageData)
                                .addOnSuccessListener {
                                    Log.d("!!!", "Initial message sent successfully")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("!!!", "Error sending initial message", e)
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to add friend", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "User is already your friend", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error checking existing friends", Toast.LENGTH_SHORT).show()
            }
    }




}