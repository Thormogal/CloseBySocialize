package com.example.closebysocialize.utils

import android.util.Log
import com.example.closebysocialize.dataClass.Comment
import com.example.closebysocialize.dataClass.Message
import com.example.closebysocialize.message.FireBaseMessagingService.Companion.TAG
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

object MessagingUtils {

    private var unreadMessageListenerRegistration: ListenerRegistration? = null
    private const val TAG = "MessagingUtils"


    fun listenForNewMessages(userId: String, updateBadge: (count: Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        unreadMessageListenerRegistration?.remove()

        unreadMessageListenerRegistration = db.collection("conversations")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("MessagingUtils", "Error listening for new messages", e)
                    return@addSnapshotListener
                }

                val conversationIds = snapshot?.documents?.mapNotNull { it.id } ?: listOf()
                var totalUnreadMessages = 0

                conversationIds.forEach { conversationId ->
                    db.collection("conversations").document(conversationId)
                        .collection("messages")
                        .whereEqualTo("isRead", false)
                        .whereEqualTo("receiverId", userId)
                        .addSnapshotListener { messagesSnapshot, _ ->
                            totalUnreadMessages += messagesSnapshot?.size() ?: 0
                            updateBadge(totalUnreadMessages)
                        }
                }
            }
    }

    fun updateBottomNavigationBadge(navView: BottomNavigationView, menuItemId: Int, count: Int) {
        if (count > 0) {
            val badge = navView.getOrCreateBadge(menuItemId)
            badge.isVisible = true
            badge.number = count
        } else {
            navView.removeBadge(menuItemId)
        }
    }

    fun markMessagesAsRead(userId: String, conversationId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .whereEqualTo("receiverId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    val docRef = db.collection("conversations")
                        .document(conversationId)
                        .collection("messages")
                        .document(document.id)
                    batch.update(docRef, "isRead", true)
                }
                batch.commit().addOnSuccessListener {
                    Log.d("MessagingUtils", "All messages marked as read successfully.")
                }.addOnFailureListener { e ->
                    Log.w("MessagingUtils", "Error marking messages as read", e)
                }
            }
    }

    fun removeListeners() {
        unreadMessageListenerRegistration?.remove()
    }

    fun postComment(
        conversationId: String,
        senderId: String,
        receiverId: String,
        commentText: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val newMessage = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
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
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun fetchMessages(
        conversationId: String,
        onMessagesFetched: (List<Message>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onFailure(e)
                    return@addSnapshotListener
                }

                val messages = snapshots?.mapNotNull { document ->
                    document.toObject(Message::class.java)
                } ?: emptyList()

                onMessagesFetched(messages)
            }
    }

    fun checkForExistingConversation(
        friendId: String,
        userId: String,
        callback: (String?) -> Unit
    ) {
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
                Log.e("MessagingUtils", "Error checking for existing conversation: ", exception)
                callback(null)
            }
    }

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

