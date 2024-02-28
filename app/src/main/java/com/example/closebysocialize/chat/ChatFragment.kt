package com.example.closebysocialize.chat

import android.app.AlertDialog
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
import java.util.Date


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
        commentAdapter = CommentAdapter(mutableListOf(), this).also {
            it.listener = this
        }
        recyclerView.adapter = commentAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchComments()
    }

    override fun onReply(commentId: String) {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_reply, null)
        val editTextReply = view.findViewById<EditText>(R.id.editTextReply)

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.reply)) // todo what to say?
            .setView(view)
            .setPositiveButton(R.string.post) { dialog, which ->
                val replyText = editTextReply.text.toString().trim()
                if (replyText.isNotEmpty()) {
                    postComment(replyText, commentId)
                }
            }
            .setNegativeButton(R.string.cancel, null) // todo what to say?
            .create()
            .show()
    }
    fun organizeCommentsWithReplies(comments: List<Comment>): List<Comment> {
        val organizedComments = mutableListOf<Comment>()
        val commentMap = comments.associateBy { it.id }
        val topLevelComments = comments.filter { it.parentId == null }

        topLevelComments.forEach { comment ->
            organizedComments.add(comment)
            addRepliesRecursively(comment, organizedComments, commentMap)
        }
        Log.d("ChatFragment", "Organized Comments: ${organizedComments.map { "${it.id} - Parent: ${it.parentId}" }}")
        return organizedComments
    }

    private fun addRepliesRecursively(
        comment: Comment,
        organizedComments: MutableList<Comment>,
        commentMap: Map<String, Comment>
    ) {
        val replies = commentMap.values.filter { it.parentId == comment.id }
        for (reply in replies) {
            addRepliesRecursively(reply, organizedComments, commentMap)
        }
        organizedComments.addAll(replies)
    }


    private fun postComment(commentText: String, parentId: String? = null) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Anonymous"
        val userPhotoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl.toString()
        val newComment = hashMapOf(
            "userId" to userId,
            "commentText" to commentText,
            "displayName" to userName,
            "profileImageUrl" to userPhotoUrl,
            "timestamp" to FieldValue.serverTimestamp(),
            "parentId" to parentId
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




    private fun fetchComments() {
        FirebaseFirestore.getInstance()
            .collection("events")
            .document(eventId!!)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val fetchedComments = documents.mapNotNull { document ->
                    val comment = document.toObject(Comment::class.java)
                    comment.id = document.id
                    comment
                }

                val organizedComments = organizeCommentsWithReplies(fetchedComments)
                commentAdapter.updateComments(organizedComments)
            }
            .addOnFailureListener { exception ->
                Log.e("ChatFragment", "Error fetching comments", exception)
            }
    }



    private fun organizeComments(comments: List<Comment>): List<Comment> {
        val topLevelComments = mutableListOf<Comment>()
        val repliesMap = mutableMapOf<String, MutableList<Comment>>()
        comments.forEach { comment ->
            if (comment.parentId == null) {
                topLevelComments.add(comment)
            } else {
                repliesMap.getOrPut(comment.parentId) { mutableListOf() }.add(comment)
            }
        }
        val orderedList = mutableListOf<Comment>()
        topLevelComments.forEach { comment ->
            orderedList.add(comment)
            repliesMap[comment.id]?.let { replies ->
                orderedList.addAll(replies.sortedBy { it.timestamp })
            }
        }

        return orderedList
    }


}
