package com.example.closebysocialize.utils

import android.content.Context
import android.widget.Toast
import com.example.closebysocialize.dataClass.Event
import com.example.closebysocialize.dataClass.Friend
import com.example.closebysocialize.dataClass.Users
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

import java.util.UUID

object FirestoreUtils {
    fun saveUserToFirestore(firebaseUser: FirebaseUser, context: Context) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val userInfo = hashMapOf(
                    "id" to firebaseUser.uid,
                    "name" to firebaseUser.displayName,
                    "email" to firebaseUser.email,
                    "profileImageUrl" to (firebaseUser.photoUrl?.toString() ?: "defaultUrl")
                )

                userRef.set(userInfo)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Your information has been saved to Firestore.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Error with saving user information: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            } else {
                Toast.makeText(
                    context,
                    "User information already exists in Firestore.",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }.addOnFailureListener { e ->
            Toast.makeText(
                context,
                "Error when checking if the profile exists: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun fetchProfileImageUrl(senderId: String, context: Context, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(senderId).get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImageUrl") ?: ""
                onSuccess(profileImageUrl)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching profile image: ${exception.message}", Toast.LENGTH_SHORT).show()
                onFailure(exception)
            }
    }

    fun fetchSavedEventsByUser(
        userId: String,
        onSuccess: (List<Event>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val savedEventIds = documentSnapshot["savedEvents"] as? List<String> ?: emptyList()
                if (savedEventIds.isNotEmpty()) {
                    fetchEventsByIds(savedEventIds, onSuccess, onFailure)
                } else {
                    onSuccess(emptyList())
                }
            }
            .addOnFailureListener(onFailure)
    }

    private fun fetchEventsByIds(
        ids: List<String>,
        onSuccess: (List<Event>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val validIds = ids.filterNot { it.isBlank() }
        if (validIds.isNotEmpty()) {
            val tasks = validIds.chunked(10).map { batch ->
                db.collection("events").whereIn(FieldPath.documentId(), batch).get()
            }
            Tasks.whenAllSuccess<QuerySnapshot>(tasks)
                .addOnSuccessListener { querySnapshots ->
                    val events = querySnapshots.flatMap { snapshot ->
                        snapshot.documents.mapNotNull {
                            it.toObject(Event::class.java)
                        }
                    }
                    onSuccess(events)
                }
                .addOnFailureListener(onFailure)
        } else {
            onFailure(IllegalArgumentException("No valid document IDs provided for querying."))
        }
    }

    fun fetchAttendingEventsByUser(
        userId: String,
        onSuccess: (List<Event>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val attendingEventIds =
                    documentSnapshot["attendingEvents"] as? List<String> ?: emptyList()
                if (attendingEventIds.isNotEmpty()) {
                    fetchEventsByIds(attendingEventIds, onSuccess, onFailure)
                } else {
                    onSuccess(emptyList())
                }
            }
            .addOnFailureListener(onFailure)
    }

    fun fetchAllEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val eventsList = snapshot.documents.mapNotNull { document ->
                    val event = document.toObject(Event::class.java)
                    event?.id = document.id
                    event
                }
                onSuccess(eventsList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun loadFriends(
        userId: String,
        onSuccess: (List<Friend>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .collection("friends")
            .get()
            .addOnSuccessListener { documents ->
                val friendsList = documents.mapNotNull { it.toObject(Friend::class.java) }
                onSuccess(friendsList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun toggleSavedEvent(
        userId: String,
        eventId: String,
        isCurrentlySaved: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val savedEvents =
                    documentSnapshot.toObject(Users::class.java)?.savedEvents ?: listOf()

                if (isCurrentlySaved && savedEvents.contains(eventId)) {
                    userRef.update("savedEvents", FieldValue.arrayRemove(eventId))
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onFailure(exception) }
                } else if (!isCurrentlySaved && !savedEvents.contains(eventId)) {
                    userRef.update("savedEvents", FieldValue.arrayUnion(eventId))
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onFailure(exception) }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun toggleAttendingEvent(
        userId: String,
        eventId: String,
        isCurrentlyAttending: Boolean,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val attendingEvents =
                    documentSnapshot.toObject(Users::class.java)?.attendingEvents ?: listOf()
                if (isCurrentlyAttending && attendingEvents.contains(eventId)) {
                    userRef.update("attendingEvents", FieldValue.arrayRemove(eventId))
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onFailure(exception) }
                } else if (!isCurrentlyAttending && !attendingEvents.contains(eventId)) {
                    userRef.update("attendingEvents", FieldValue.arrayUnion(eventId))
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onFailure(exception) }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }


}



