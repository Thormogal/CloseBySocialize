package com.example.closebysocialize.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.closebysocialize.dataClass.Event
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

object FirestoreUtils {
    // save user data to users
    fun saveUserToFirestore(firebaseUser: FirebaseUser, context: Context) {
        val userInfo = hashMapOf(
            "name" to firebaseUser.displayName,
            "email" to firebaseUser.email,
            "PhotoUrl" to (firebaseUser.photoUrl?.toString() ?: "defaultUrl")
        )
        FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
            .set(userInfo)
            .addOnSuccessListener {
                // TODO do you want a message?
               // Toast.makeText(context, "User information saved to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // TODO do you want a message?
                // Toast.makeText(context, "Failed to save user information: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // for any specific data class
    fun <T> fetchDataFromFirestore(collectionPath: String, clazz: Class<T>, onSuccess: (List<T>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { snapshot ->
                val dataList = snapshot.documents.mapNotNull { it.toObject(clazz) }
                onSuccess(dataList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }



    // for the nested Event class inside users
    fun fetchEventsForAllUsers(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val eventsList = mutableListOf<Event>()
        db.collection("users").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val usersSnapshot = task.result
                if (usersSnapshot != null && !usersSnapshot.isEmpty) {
                    val userCount = usersSnapshot.size()
                    var processedUsers = 0
                    usersSnapshot.documents.forEach { userDocument ->
                        userDocument.reference.collection("Event")
                            .get()
                            .addOnSuccessListener { eventsSnapshot ->
                                eventsSnapshot.forEach { eventDocument ->
                                    eventDocument.toObject(Event::class.java)?.let { event ->
                                        eventsList.add(event)
                                    }
                                }
                                processedUsers++
                                if (processedUsers == userCount) {
                                    onSuccess(eventsList)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("FirestoreUtils", "Error fetching events", exception)
                            }
                    }
                } else {
                    onSuccess(emptyList())
                }
            } else {
                task.exception?.let {
                    onFailure(it)
                }
            }
        }
    }
    // fetch events created by user
    fun fetchUserEvents(userId: String?, onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
            .whereEqualTo("authorId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val eventsList = snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
                onSuccess(eventsList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun fetchSavedEventsByUser(userId: String, onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
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

    fun fetchEventsByIds(ids: List<String>, onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val tasks = ids.chunked(10).map { batch ->
            db.collection("events").whereIn(FieldPath.documentId(), batch).get()
        }
        Tasks.whenAllSuccess<QuerySnapshot>(tasks)
            .addOnSuccessListener { querySnapshots ->
                val events = querySnapshots.flatMap { snapshot -> snapshot.documents.mapNotNull { it.toObject(Event::class.java) } }
                onSuccess(events)
            }
            .addOnFailureListener(onFailure)
    }

    fun fetchAttendingEventsByUser(userId: String, onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { documentSnapshot ->
                val attendingEventIds = documentSnapshot["attendingEvents"] as? List<String> ?: emptyList()
                if (attendingEventIds.isNotEmpty()) {
                    fetchEventsByIds(attendingEventIds, onSuccess, onFailure)
                } else {
                    onSuccess(emptyList())
                }
            }
            .addOnFailureListener(onFailure)
    }



}