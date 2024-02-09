package com.example.closebysocialize.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ChatFragment : Fragment() {
    private var eventName: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var editTextComment: EditText
    private lateinit var buttonPostComment: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventName = it.getString("eventName")
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

    companion object {
        @JvmStatic
        fun newInstance(eventName: String) = ChatFragment().apply {
            arguments = Bundle().apply {
                putString("eventName", eventName)
            }
        }
    }

    private fun postComment() {
        val commentText = editTextComment.text.toString()
        if (commentText.isNotEmpty()) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid
            UserDetailsFetcher.fetchUserDetails(userId) { userDetails, exception ->
                if (exception != null) {
                    Log.e("ChatFragment", "Error fetching user details", exception)
                    return@fetchUserDetails
                }

                userDetails?.let {
                    val displayName = if (it.firstName.isNotEmpty() && it.lastName.isNotEmpty()) {
                        "${it.firstName} ${it.lastName}"
                    } else {
                        it.username
                    }
                    val profileImageUrl = it.profileImageUrl

                    val newCommentData = hashMapOf(
                        "displayName" to displayName,
                        "commentText" to commentText,
                        "profileImageUrl" to profileImageUrl,
                    )

                    val db = FirebaseFirestore.getInstance()
                    db.collection("comments").add(newCommentData).addOnSuccessListener { documentReference ->
                        val newComment = Comment(
                            id = documentReference.id,
                            displayName = displayName,
                            commentText = commentText,
                            profileImageUrl = profileImageUrl,
                        )
                        activity?.runOnUiThread {
                            val updatedComments = commentAdapter.comments.toMutableList().apply {
                                add(newComment)
                            }
                            commentAdapter.updateComments(updatedComments)
                            editTextComment.setText("")
                        }
                    }
                        .addOnFailureListener { e ->
                        }
                }
            }
        }
    }


}
