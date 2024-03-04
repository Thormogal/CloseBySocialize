package com.example.closebysocialize.message

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class OpenChatFragment : Fragment() {
    private var conversationId: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var editTextComment: EditText
    private lateinit var buttonPostComment: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            conversationId = it.getString(ARG_CONVERSATION_ID)
            val friendId = it.getString(ARG_FRIEND_ID)
            Log.d("OpenChatFragment", "Retrieved friend ID: $friendId")
        }
        Log.d("OpenChatFragment", "Retrieved conversation ID: $conversationId")
    }

    companion object {
        private const val ARG_CONVERSATION_ID = "conversationId"
        private const val ARG_FRIEND_ID = "friendId"

        fun newInstance(conversationId: String? = null, friendId: String): OpenChatFragment {
            return OpenChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONVERSATION_ID, conversationId)
                    putString(ARG_FRIEND_ID, friendId)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_open_chat, container, false)
        recyclerView = view.findViewById(R.id.commentRecyclerView)
        editTextComment = view.findViewById(R.id.commentEditText)
        buttonPostComment = view.findViewById(R.id.postCommentButton)

        buttonPostComment.setOnClickListener {
            val commentText = editTextComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                arguments?.getString(ARG_FRIEND_ID)?.let { friendId ->
                    ensureConversationAndPostComment(friendId, commentText)
                } ?: run {
                    Toast.makeText(context, "Friend ID is missing.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val adapterContext = requireContext()
        messageAdapter = MessageAdapter(adapterContext, mutableListOf(), currentUserId)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        fetchMessages()
        fetchConversations()
        markMessagesAsRead()
    }

    private fun ensureConversationAndPostComment(friendId: String, commentText: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (conversationId != null) {
            postComment(conversationId!!, currentUserId, commentText)
        } else {
            val participants = listOf(currentUserId, friendId)
            val newConversationData = hashMapOf(
                "participants" to participants, "timestamp" to FieldValue.serverTimestamp()
            )
            val conversationsCollection =
                FirebaseFirestore.getInstance().collection("conversations")

            conversationsCollection.add(newConversationData)
                .addOnSuccessListener { documentReference ->
                    val newConversationId = documentReference.id
                    this.conversationId = newConversationId
                    postComment(newConversationId, currentUserId, commentText)
                }.addOnFailureListener { e ->
                Log.e("OpenChatFragment", "Failed to create conversation", e)
                Toast.makeText(context, "Failed to start conversation", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun postComment(conversationId: String, senderId: String, commentText: String) {
        val newMessage = hashMapOf(
            "senderId" to senderId,
            "receiverId" to getReceiverId(),
            "content" to commentText,
            "timestamp" to FieldValue.serverTimestamp(),
            "type" to "text",
            "isRead" to false,
            "messageStatus" to "sent"
        )
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(newMessage)
            .addOnSuccessListener {
                Log.d("OpenChatFragment", "Message sent successfully")
                editTextComment.setText("")
            }
            .addOnFailureListener { e ->
                Log.e("OpenChatFragment", "Failed to send message", e)
                Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getReceiverId(): String {
        return arguments?.getString(ARG_FRIEND_ID) ?: ""
    }

    private fun fetchMessages() {
        val conversationId = this.conversationId ?: run {
            Log.e("OpenChatFragment", "Conversation ID is null")
            return
        }
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("OpenChatFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }
                val messagesList = snapshots?.mapNotNull { document ->
                    document.toObject(Message::class.java)
                }?.toMutableList() ?: mutableListOf()
                messageAdapter.updateMessages(messagesList)
                if (messagesList.isNotEmpty()) {
                    recyclerView.scrollToPosition(messagesList.size - 1)
                }
            }
    }

    private fun fetchConversations() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val friendId = arguments?.getString(ARG_FRIEND_ID) ?: run {
            Log.e("OpenChatFragment", "Friend ID is null")
            return
        }
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .whereArrayContains("participants", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val filteredConversations = documents.filter { document ->
                    val participants =
                        document.get("participants") as? List<String> ?: return@filter false
                    friendId in participants
                }
                // TODO: Use filteredConversations for whatever your logic requires
            }
            .addOnFailureListener { exception ->
                Log.e("OpenChatFragment", "Error fetching conversations", exception)
            }
    }

    private fun markMessagesAsRead() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val conversationId = this.conversationId ?: return
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .whereEqualTo("receiverId", currentUserId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    FirebaseFirestore.getInstance()
                        .collection("conversations")
                        .document(conversationId)
                        .collection("messages")
                        .document(document.id)
                        .update("isRead", true)
                        .addOnSuccessListener {
                            Log.d("OpenChatFragment", "DocumentSnapshot successfully updated!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("OpenChatFragment", "Error updating document", e)
                        }
                }
            }
    }

}
