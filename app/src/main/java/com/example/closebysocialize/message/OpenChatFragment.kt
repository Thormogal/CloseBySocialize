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
import com.google.firebase.firestore.FirebaseFirestore

class OpenChatFragment : Fragment() {
    private var eventId: String? = null
    private var conversationId: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var editTextComment: EditText
    private lateinit var buttonPostComment: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            conversationId = it.getString(ARG_EVENT_ID)
        }
        Log.d("OpenChatFragment", "Retrieved conversation ID: $conversationId")
    }

    companion object {
        private const val ARG_EVENT_ID = "conversationId"
        fun newInstance(conversationId: String): OpenChatFragment {
            val fragment = OpenChatFragment()
            val args = Bundle().apply {
                putString(ARG_EVENT_ID, conversationId)
            }
            fragment.arguments = args
            return fragment
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
            postComment()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        messageAdapter = MessageAdapter(mutableListOf(), currentUserId)
        recyclerView.adapter = messageAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        fetchMessages()
        fetchConversations()
    }

    private fun postComment() {
        val commentText = editTextComment.text.toString().trim()
        if (commentText.isEmpty()) {
            Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.e("OpenChatFragment", "Current user ID is null")
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val newMessage = hashMapOf(
            "senderId" to currentUserId,
            "content" to commentText,
        )

        conversationId?.let {
            FirebaseFirestore.getInstance()
                .collection("conversations")
                .document(it)
                .collection("messages")
                .add(newMessage)
                .addOnSuccessListener {
                    Log.d("OpenChatFragment", "Comment posted successfully")
                    editTextComment.setText("") // Clear the input field after posting
                    fetchMessages() // Optionally refresh messages
                }
                .addOnFailureListener { e ->
                    Log.e("OpenChatFragment", "Failed to post comment", e)
                    Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
                }
        } ?: Log.e("OpenChatFragment", "Conversation ID is null")
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
            .get()
            .addOnSuccessListener { documents ->
                Log.d("OpenChatFragment", "Fetched ${documents.size()} messages")
                documents.forEach { document ->
                    val content = document.getString("content") ?: "Content not found"
                    Log.d("OpenChatFragment", "Message Content: $content")
                    Log.d("OpenChatFragment", "Document data: ${document.data}")
                }
                val messagesList = documents.mapNotNull { document ->
                    document.toObject(Message::class.java)
                }.toMutableList()
                messageAdapter.updateMessages(messagesList)
            }
            .addOnFailureListener { exception ->
                Log.e("OpenChatFragment", "Error fetching messages", exception)
            }
    }

    private fun fetchConversations() {
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("YourFragment", "Fetched ${documents.size()} conversations")
                for (document in documents) {
                    // Assuming each conversation document has a 'title' or similar field you're interested in
                    val title = document.getString("title") ?: "No title"
                    Log.d("YourFragment", "Conversation ID: ${document.id}, Title: $title")
                    // Log other fields as needed
                }
            }
            .addOnFailureListener { exception ->
                Log.e("YourFragment", "Error fetching conversations", exception)
            }
    }



}
