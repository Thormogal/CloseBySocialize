package com.example.closebysocialize.chat

import android.app.Dialog
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
import com.example.closebysocialize.message.FireBaseMessagingService.Companion.TAG
import com.example.closebysocialize.utils.CommentsUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.textfield.TextInputLayout


class ChatFragment : Fragment(), CommentAdapter.CommentInteractionListener {
    private val eventId: String by lazy {
        arguments?.getString(ARG_EVENT_ID) ?: throw IllegalArgumentException("Event ID is required")
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var editTextComment: EditText
    private lateinit var buttonPostComment: Button

    companion object {
        private const val ARG_EVENT_ID = "eventId"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false).apply {
            recyclerView = findViewById(R.id.commentRecyclerView)
            editTextComment = findViewById(R.id.commentEditText)
            buttonPostComment = findViewById(R.id.postCommentButton)
            setupButtonListeners()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        commentAdapter.setupRealtimeListener()
        fetchAndOrganizeComments()
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(mutableListOf(), requireContext(), eventId).also {
            it.listener = this
        }
        recyclerView.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupButtonListeners() {
        buttonPostComment.setOnClickListener {
            editTextComment.text.toString().trim().takeIf { it.isNotEmpty() }?.let {
                postComment(it)
            }
        }
    }

    private fun fetchAndOrganizeComments() {
        CommentsUtils.fetchAndOrganizeComments(
            eventId = eventId,
            onSuccess = { organizedComments ->
                commentAdapter.updateComments(organizedComments)
            },
            onFailure = { exception ->
                Log.e(CommentsUtils.TAG, "Error fetching comments", exception)
            }
        )
    }



    override fun onReply(commentId: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_reply, null)
        val dialog = Dialog(requireContext()).apply {
            setContentView(view)
        }

        val postButton: Button = view.findViewById(R.id.postReplyButton)
        val cancelButton: Button = view.findViewById(R.id.cancelReplyButton)
        val replyTextInputEditText =
            view.findViewById<TextInputLayout>(R.id.inputReplyText).editText

        postButton.setOnClickListener {
            replyTextInputEditText?.text.toString().takeIf { it.isNotBlank() }?.let { replyText ->
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

        CommentsUtils.postComment(
            eventId = eventId!!,
            commentText = commentText,
            parentId = parentId,
            userId = userId,
            userName = userName,
            userPhotoUrl = userPhotoUrl,
            onSuccess = {
                editTextComment.setText("")
                fetchAndOrganizeComments()
            },
            onFailure = { e ->
                Log.e("ChatFragment", "Error adding comment", e)
            }
        )
    }

}
