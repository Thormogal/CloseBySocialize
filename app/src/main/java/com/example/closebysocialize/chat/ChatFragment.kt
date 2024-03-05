package com.example.closebysocialize.chat

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Comment
import com.example.closebysocialize.message.FireBaseMessagingService.Companion.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.ui.graphics.Color
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class ChatFragment : Fragment(), CommentAdapter.CommentInteractionListener {
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
            val commentText = editTextComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                postComment(commentText, null)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        commentAdapter = CommentAdapter(mutableListOf(), requireContext(), this, eventId)
            .also {
                it.listener = this
            }

        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        commentAdapter.setupRealtimeListener()
        fetchAndOrganizeComments()
    }

    private fun fetchAndOrganizeComments() {
        FirebaseFirestore.getInstance()
            .collection("events").document(eventId!!)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val allComments = documents.mapNotNull { it.toObject(Comment::class.java) }

                val parentComments = allComments.filter { it.parentId == null }
                val organizedComments = mutableListOf<Comment>()

                parentComments.forEach { parentComment ->
                    organizedComments.add(parentComment)
                    val replies = allComments.filter { it.parentId == parentComment.id }
                    organizedComments.addAll(replies)
                }

                commentAdapter.updateComments(organizedComments)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching comments", exception)
            }
    }


    override fun onReply(commentId: String) {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_reply, null)

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)

        val postButton = view.findViewById<Button>(R.id.postReplyButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelReplyButton)

        val replyTextInputLayout = view.findViewById<TextInputLayout>(R.id.inputReplyText)
        val replyTextInputEditText = replyTextInputLayout.editText

        postButton.setOnClickListener {
            replyTextInputEditText?.let {
                val replyText = it.text.toString()
                postComment(replyText, commentId)
            }
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun postComment(commentText: String, parentId: String? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"
        val userPhotoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl.toString()

        val commentRef = FirebaseFirestore.getInstance().collection("events").document(eventId!!)
            .collection("comments").document()

        val newCommentMap = hashMapOf(
            "id" to commentRef.id,
            "userId" to userId,
            "commentText" to commentText,
            "displayName" to userName,
            "profileImageUrl" to userPhotoUrl,
            "timestamp" to FieldValue.serverTimestamp()
        )

        parentId?.let {
            newCommentMap["parentId"] = it
        }
        commentRef.set(newCommentMap)
            .addOnSuccessListener {
                editTextComment.setText("")
                fetchAndOrganizeComments()
            }
            .addOnFailureListener { e ->
                Log.e("ChatFragment", "Error adding comment", e)
            }
    }


}
