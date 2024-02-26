package com.example.closebysocialize.utils

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.closebysocialize.dataClass.Event
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

object FirestoreUtils {
    // save user data to users
    fun saveUserToFirestore(firebaseUser: FirebaseUser, context: Context) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(firebaseUser.uid)
        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val uniqueId = UUID.randomUUID().toString()
                val userInfo = hashMapOf(
                    "id" to uniqueId,
                    "name" to firebaseUser.displayName,
                    "email" to firebaseUser.email,
                    "profileImageUrl" to (firebaseUser.photoUrl?.toString() ?: "defaultUrl")
                )

                userRef.set(userInfo)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Your information has been saved to Firestore.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error with saving user information: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(context, "User information already exists in Firestore.", Toast.LENGTH_SHORT).show()

            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Error when checking if the profile exists: ${e.message}", Toast.LENGTH_SHORT).show()
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

    fun fetchAllEvents(onSuccess: (List<Event>) -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events")
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



}