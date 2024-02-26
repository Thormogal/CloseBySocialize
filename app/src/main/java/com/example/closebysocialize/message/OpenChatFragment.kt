package com.example.closebysocialize.message

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OpenChatFragment : Fragment() {
    private var eventId: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var editTextComment: EditText
    private lateinit var buttonPostComment: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_EVENT_ID)
        }
    }

    companion object {
        private const val ARG_EVENT_ID = "eventId"

        fun newInstance(eventId: String): OpenChatFragment {
            val fragment = OpenChatFragment()
            val args = Bundle().apply {
                putString(ARG_EVENT_ID, eventId)
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
    }

    private fun postComment() {
        val commentText = editTextComment.text.toString().trim()
        if (commentText.isNotEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val profileImageUrl = FirebaseAuth.getInstance().currentUser?.photoUrl.toString()

            val newComment = hashMapOf(
                "senderId" to userId,
                "content" to commentText
            )
            FirebaseFirestore.getInstance().collection("events").document(eventId!!)
                .collection("conversations").add(newComment)
                .addOnSuccessListener {
                    Log.d("OpenChatFragment", "Message added successfully.")
                    editTextComment.setText("")
                    fetchMessages()
                }
                .addOnFailureListener { e ->
                    Log.e("OpenChatFragment", "Error adding message", e)
                }
        }
    }

    private fun fetchMessages() {
        val eventId = this.eventId ?: run {
            Log.e("OpenChatFragment", "Event ID is null")
            return
        }
        FirebaseFirestore.getInstance()
            .collection("events")
            .document(eventId)
            .collection("conversations")
            .get()
            .addOnSuccessListener { documents ->
                val messagesList = documents.mapNotNull { document ->
                    document.toObject(Message::class.java)
                }.toMutableList()
                messageAdapter.updateMessages(messagesList)
            }
            .addOnFailureListener { exception ->
                Log.e("OpenChatFragment", "Error fetching messages", exception)
            }
    }

}
