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
import com.example.closebysocialize.utils.MessagingUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessageFragment : Fragment(), FriendsAdapter.FriendClickListener {
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var displayFriendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageRecyclerView = view.findViewById(R.id.messageRecyclerView)
        displayFriendsAdapter = FriendsAdapter(requireContext(), listOf(), showActions = false)
        messageRecyclerView.adapter = displayFriendsAdapter
        messageRecyclerView.layoutManager = LinearLayoutManager(context)
        loadFriends()
        displayFriendsAdapter.listener = this
    }

    private fun loadFriends() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreUtils.loadFriends(
            userId = userId,
            onSuccess = { friendsList ->
                displayFriendsAdapter.updateData(friendsList)
            },
            onFailure = { exception ->
                Log.e("MessageFragment", "Error loading friends: ", exception)
            }
        )
    }

    override fun onFriendClick(friend: Friend) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        MessagingUtils.checkForExistingConversation(friend.id, userId) { conversationId ->
            val chatFragment = OpenChatFragment.newInstance(conversationId, friend.id)
            navigateToChatFragment(chatFragment, friend.name ?: "Unknown")
        }
    }

    private fun navigateToChatFragment(fragment: Fragment, friendName: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = friendName
    }


    override fun onMessageClick(friend: Friend) {
    }

    override fun onBinClick(friend: Friend) {
    }
}
