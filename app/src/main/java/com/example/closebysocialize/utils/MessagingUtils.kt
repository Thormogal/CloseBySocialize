package com.example.closebysocialize.utils

import android.util.Log
import com.example.closebysocialize.dataClass.Message
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

object MessagingUtils {

    private var unreadMessageListenerRegistration: ListenerRegistration? = null


    fun listenForNewMessages(userId: String, updateBadge: (count: Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        unreadMessageListenerRegistration?.remove()

        unreadMessageListenerRegistration =
            db.collection("conversations").whereArrayContains("participants", userId)
                .addSnapshotListener { conversationsSnapshot, conversationsError ->
                    if (conversationsError != null) {
                        Log.e(
                            "MessagingUtils",
                            "Error listening for new messages",
                            conversationsError
                        )
                        return@addSnapshotListener
                    }

                    val conversationIds =
                        conversationsSnapshot?.documents?.mapNotNull { it.id } ?: listOf()
                    if (conversationIds.isEmpty()) {
                        updateBadge(0)
                        return@addSnapshotListener
                    }

                    var totalUnreadMessages = AtomicInteger(0)
                    val countdownLatch = CountDownLatch(conversationIds.size)

                    conversationIds.forEach { conversationId ->
                        db.collection("conversations").document(conversationId)
                            .collection("messages").whereEqualTo("isRead", false)
                            .whereEqualTo("receiverId", userId)
                            .addSnapshotListener { messagesSnapshot, messagesError ->
                                if (messagesError != null) {
                                    Log.e(
                                        "MessagingUtils",
                                        "Error listening for message updates",
                                        messagesError
                                    )
                                    countdownLatch.countDown()
                                    return@addSnapshotListener
                                }
                                synchronized(this) {
                                    if (messagesSnapshot != null) {
                                        if (countdownLatch.count == conversationIds.size.toLong()) {
                                            totalUnreadMessages.set(0)
                                        }
                                        totalUnreadMessages.addAndGet(messagesSnapshot.size())
                                    }
                                    countdownLatch.countDown()

                                    if (countdownLatch.count == 0L) {
                                        updateBadge(totalUnreadMessages.get())
                                    }
                                }
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
        db.collection("conversations").document(conversationId).collection("messages")
            .whereEqualTo("receiverId", userId).whereEqualTo("isRead", false).get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    val docRef = db.collection("conversations").document(conversationId)
                        .collection("messages").document(document.id)
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
        FirebaseFirestore.getInstance().collection("conversations").document(conversationId)
            .collection("messages").add(newMessage).addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun fetchMessages(
        conversationId: String,
        onMessagesFetched: (List<Message>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseFirestore.getInstance().collection("conversations").document(conversationId)
            .collection("messages").orderBy("timestamp").addSnapshotListener { snapshots, e ->
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
        friendId: String, userId: String, callback: (String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("conversations").whereArrayContains("participants", userId).get()
            .addOnSuccessListener { documents ->
                val conversation = documents.documents.firstOrNull { document ->
                    val participants = document["participants"] as List<*>
                    participants.contains(friendId)
                }
                callback(conversation?.id)
            }.addOnFailureListener { exception ->
                Log.e("MessagingUtils", "Error checking for existing conversation: ", exception)
                callback(null)
            }
    }


}

