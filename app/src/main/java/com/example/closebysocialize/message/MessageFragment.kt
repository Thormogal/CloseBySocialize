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
import com.google.firebase.firestore.FirebaseFirestore

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
        checkForExistingConversation(friend.id) { conversationId ->
            if (conversationId != null) {
                val chatFragment = OpenChatFragment.newInstanceForConversation(conversationId, friend.id)
                navigateToChatFragment(chatFragment)
            } else {
                val chatFragment = OpenChatFragment.newInstanceForFriend(friend.id)
                navigateToChatFragment(chatFragment)
            }
        }
    }
    private fun navigateToChatFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun checkForExistingConversation(friendId: String, callback: (String?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("conversations")
            .whereArrayContains("participants", userId)
            .get()
            .addOnSuccessListener { documents ->
                val conversation = documents.documents.firstOrNull { document ->
                    val participants = document["participants"] as List<*>
                    participants.contains(friendId)
                }
                callback(conversation?.id)
            }
            .addOnFailureListener { exception ->
                Log.e("MessageFragment", "Error checking for existing conversation: ", exception)
                callback(null)
            }
    }


    override fun onMessageClick(friend: Friend) {
    }
    override fun onBinClick(friend: Friend) {
    }
}