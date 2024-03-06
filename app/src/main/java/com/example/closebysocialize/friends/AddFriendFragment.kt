package com.example.closebysocialize.friends

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.UserAdapter
import com.google.firebase.auth.FirebaseAuth

class AddFriendFragment : Fragment() {
    private lateinit var searchEditText: EditText
    private lateinit var recyclerViewFindFriends: RecyclerView
    private lateinit var userAdapter: UserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
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
        FirestoreUtils.searchUsers(query, onSuccess = { userList ->
            userAdapter.updateData(userList)
        }, onFailure = { exception ->
            Toast.makeText(
                context, "Failed to search for users: ${exception.message}", Toast.LENGTH_SHORT
            ).show()
        })
    }

    private fun addUserAsFriend(user: Users) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid == null) {
            Toast.makeText(context, "You need to be logged in to add friends", Toast.LENGTH_SHORT)
                .show()
            return
        }
        FirestoreUtils.addUserAsFriend(currentUserUid, user, onSuccess = {
            Toast.makeText(context, "Friend added successfully", Toast.LENGTH_SHORT).show()
        }, onFailure = { exception ->
            Toast.makeText(context, exception.message ?: "Failed to add friend", Toast.LENGTH_SHORT)
                .show()
        })
    }


}