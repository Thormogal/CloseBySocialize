package com.example.closebysocialize.chat

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class ChatFragment : Fragment() {
    private var eventId: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
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

        fun newInstance(eventId: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EVENT_ID, eventId)
                }
            }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
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
        commentAdapter = CommentAdapter(mutableListOf(), this)
        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchComments()
    }
    private fun postComment() {
        val commentText = editTextComment.text.toString().trim()
        if (commentText.isNotEmpty()) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"
            val userPhotoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl.toString()
            val newComment = hashMapOf(
                "userId" to userId,
                "commentText" to commentText,
                "displayName" to userName,
                "profileImageUrl" to userPhotoUrl,
                "timestamp" to FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance().collection("events").document(eventId!!)
                .collection("comments").add(newComment)
                .addOnSuccessListener {
                    Log.d("ChatFragment", "Comment added successfully.")
                    editTextComment.setText("")
                    fetchComments()
                }
                .addOnFailureListener { e ->
                    Log.e("ChatFragment", "Error adding comment", e)
                }
        }
    }


    private fun fetchComments() {
        FirebaseFirestore.getInstance()
            .collection("events")
            .document(eventId!!)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val commentsList = documents.mapNotNull { document ->
                    document.toObject(Comment::class.java).apply {
                        id = document.id
                    }
                }.toMutableList()
                commentAdapter.updateComments(commentsList)
            }
            .addOnFailureListener { exception ->
                Log.e("ChatFragment", "Error fetching comments", exception)
            }
    }



}
