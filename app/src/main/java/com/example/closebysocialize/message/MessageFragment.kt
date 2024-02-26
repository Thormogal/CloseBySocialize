package com.example.closebysocialize.message

import FriendsAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Friend
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.FragmentUtils
import com.google.firebase.auth.FirebaseAuth

class MessageFragment : Fragment(), FriendsAdapter.FriendClickListener {
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageAdapter: FriendsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageRecyclerView = view.findViewById(R.id.messageRecyclerView)
        messageAdapter = FriendsAdapter(listOf(), showActions = false)
        messageRecyclerView.adapter = messageAdapter
        messageRecyclerView.layoutManager = LinearLayoutManager(context)

        loadFriends()
        messageAdapter.listener = this

    }

    private fun loadFriends() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreUtils.loadFriends(
            userId = userId,
            onSuccess = { friendsList ->
                messageAdapter.updateData(friendsList)
            },
            onFailure = { exception ->
                Log.e("MessageFragment", "Error loading friends: ", exception)
            }
        )
    }
    override fun onFriendClick(friend: Friend) {
        FragmentUtils.switchFragment(
            activity = requireActivity() as AppCompatActivity,
            containerId = R.id.fragment_container,
            fragmentClass = OpenChatFragment::class.java,
            args = Bundle().apply {
                putString("friendId", friend.id)
            }
        )
    }
    override fun onMessageClick(friend: Friend) {
    }
    override fun onBinClick(friend: Friend) {
    }
}
