package com.example.closebysocialize.utils

import android.util.Log
import com.example.closebysocialize.dataClass.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object CommentsUtils {
     const val TAG = "CommentsUtils"

    fun postComment(
        eventId: String,
        commentText: String,
        parentId: String? = null,
        userId: String,
        userName: String,
        userPhotoUrl: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val commentRef = FirebaseFirestore.getInstance().collection("events").document(eventId)
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
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun fetchAndOrganizeComments(
        eventId: String,
        onSuccess: (List<Comment>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("events").document(eventId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
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

                onSuccess(organizedComments)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun toggleLikeStatus(
        eventId: String?,
        commentId: String,
        liked: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val eventRef = FirebaseFirestore.getInstance().collection("events").document(eventId!!)
        val commentRef = eventRef.collection("comments").document(commentId)
        val userLikeRef = commentRef.collection("likes").document(userId)

        if (liked) {
            userLikeRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Like removed successfully.")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error removing like", e)
                    onFailure(e)
                }
        } else {
            val likeStatus = hashMapOf("liked" to true)
            userLikeRef.set(likeStatus)
                .addOnSuccessListener {
                    Log.d(TAG, "Like added successfully.")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding like", e)
                    onFailure(e)
                }
        }
    }


}