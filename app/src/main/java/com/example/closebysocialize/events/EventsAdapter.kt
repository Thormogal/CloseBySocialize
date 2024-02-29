package com.example.closebysocialize.events

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class EventsAdapter(private var eventsList: List<Event>, var savedEventsIds: MutableSet<String> = mutableSetOf()) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    var chatImageViewClickListener: ((String) -> Unit)? = null
    var eventInteractionListener: EventInteractionListener? = null
    var listener: EventInteractionListener? = null

    interface EventInteractionListener {
        fun onToggleSaveEvent(eventId: String, isCurrentlySaved: Boolean)
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val authorProfilePictureImageView: ImageView = view.findViewById(R.id.authorProfilePictureImageView)
        val cityTextView: TextView = view.findViewById(R.id.cityTextView)
        val eventTypeTextView: TextView = view.findViewById(R.id.eventTypeTextView)
        val eventNameTextView: TextView = view.findViewById(R.id.eventNameTextView)
        val locationTextView: TextView = view.findViewById(R.id.locationTextView)
        val dayTextView: TextView = view.findViewById(R.id.dayTextView)
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val usernameTextView: TextView = view.findViewById(R.id.authorUserNameTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
        val attendButtonTextView: Button = view.findViewById(R.id.attendElevatedButton)
        val openSpotsTextView: TextView = view.findViewById(R.id.openSpotsTextView)
        val chatImageView: ImageView = view.findViewById(R.id.chatImageView)
        val deleteImageView: ImageView = view.findViewById(R.id.deleteImageView)
        val savedImageView: ImageView = view.findViewById(R.id.savedImageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventsList[position]
        updateAttendButtonAndSpotsUI(holder, event)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid
        val userProfileUrl = currentUser?.photoUrl.toString()

        val isEventCreator = event.authorId == currentUserId
        val isCurrentlyAttending = event.attendedPeopleProfilePictureUrls.contains(userProfileUrl) || isEventCreator
        holder.attendButtonTextView.text = if (isCurrentlyAttending) holder.itemView.context.getString(R.string.event_withdraw) else holder.itemView.context.getString(R.string.event_attend)
        holder.deleteImageView.visibility = if (isEventCreator) View.VISIBLE else View.GONE

        val isSaved = savedEventsIds.contains(event.id)
        holder.savedImageView.setImageResource(if (isSaved) R.drawable.icon_heart_filled else R.drawable.icon_heart)
        holder.savedImageView.setOnClickListener {
            val eventId = event.id
            val currentlySaved = savedEventsIds.contains(eventId)
            val listener: EventInteractionListener? = null
            listener?.onToggleSaveEvent(eventId, currentlySaved)

            val isCurrentlySaved = savedEventsIds.contains(event.id)
            listener?.onToggleSaveEvent(event.id, isCurrentlySaved)
            if (eventId.isEmpty()) {
                return@setOnClickListener
            }
            if (currentlySaved) {
                savedEventsIds.remove(event.id)
                holder.savedImageView.setImageResource(R.drawable.icon_heart)
            } else {
                savedEventsIds.add(event.id)
                holder.savedImageView.setImageResource(R.drawable.icon_heart_filled)
                // TODO TOO BIG
            }
            toggleSavedEvent(event.id, currentlySaved)
        }


        Glide.with(holder.itemView.context)
            .load(event.authorProfileImageUrl)
            .circleCrop()
            .into(holder.authorProfilePictureImageView)
        holder.cityTextView.text = event.city
        holder.eventTypeTextView.text = event.eventType
        holder.eventNameTextView.text = event.title
        holder.locationTextView.text = event.location
        holder.dayTextView.text = event.day
        holder.timeTextView.text = event.time
        holder.dateTextView.text = event.date
        holder.usernameTextView.text = event.authorFirstName
        holder.descriptionTextView.text = event.description
        val availableSpots = event.spots - event.currentAttendeesCount
        holder.openSpotsTextView.text = "${availableSpots} ${holder.itemView.context.getString(R.string.spots)}"
        holder.chatImageView.setOnClickListener {
            chatImageViewClickListener?.invoke(event.id)
        }
        holder.deleteImageView.setOnClickListener {
            deleteEvent(event.id, position)
        }
        holder.attendButtonTextView.setOnClickListener {
            val isAttending = holder.attendButtonTextView.text.toString() == holder.itemView.context.getString(R.string.event_withdraw)
            val eventRef = FirebaseFirestore.getInstance().collection("events").document(event.id)
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userProfileUrl = currentUser?.photoUrl.toString()
            if (isAttending) {
                val updatedCount = event.currentAttendeesCount - 1
                event.currentAttendeesCount = updatedCount
                event.attendedPeopleProfilePictureUrls = event.attendedPeopleProfilePictureUrls.filter { it != userProfileUrl }.toMutableList()
                eventRef.update(mapOf(
                    "currentAttendeesCount" to updatedCount,
                    "attendedPeopleProfilePictureUrls" to event.attendedPeopleProfilePictureUrls
                )).addOnSuccessListener {
                    holder.openSpotsTextView.text = "${event.spots - updatedCount} ${holder.itemView.context.getString(R.string.spots)}"
                    holder.attendButtonTextView.text = holder.itemView.context.getString(R.string.event_attend)
                    refreshAttendedPeopleLinearLayout(holder, event.attendedPeopleProfilePictureUrls)
                }
            } else {
                val updatedCount = event.currentAttendeesCount + 1
                event.currentAttendeesCount = updatedCount
                event.attendedPeopleProfilePictureUrls.add(userProfileUrl)
                eventRef.update(mapOf(
                    "currentAttendeesCount" to updatedCount,
                    "attendedPeopleProfilePictureUrls" to event.attendedPeopleProfilePictureUrls
                )).addOnSuccessListener {
                    holder.openSpotsTextView.text = "${event.spots - updatedCount} ${holder.itemView.context.getString(R.string.spots)}"
                    holder.attendButtonTextView.text = holder.itemView.context.getString(R.string.event_withdraw)
                    refreshAttendedPeopleLinearLayout(holder, event.attendedPeopleProfilePictureUrls)
                    updateAttendButtonAndSpotsUI(holder, event)
                }
            }
        }

        val attendedPeopleLinearLayout = holder.itemView.findViewById<LinearLayout>(R.id.attendedPeopleLinearLayout)
        attendedPeopleLinearLayout.removeAllViews()
        event.attendedPeopleProfilePictureUrls.forEach { url ->
            val imageView = ImageView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    holder.itemView.dpToPx(40),
                    holder.itemView.dpToPx(40)
                ).apply {
                    marginEnd = holder.itemView.dpToPx(8)
                }
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                if (url != null && url.isNotEmpty()) {
                    Glide.with(holder.itemView.context)
                        .load(url)
                        .circleCrop()
                        .into(this)
                } else {
                    setImageResource(R.drawable.profile_top_bar_avatar)
                }
            }
            attendedPeopleLinearLayout.addView(imageView)
        }
    }

    override fun getItemCount() = eventsList.size

    fun updateData(newEventsList: List<Event>) {
        eventsList = newEventsList
        notifyDataSetChanged()
    }

    fun View.dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun deleteEvent(eventId: String, position: Int) {
        FirebaseFirestore.getInstance().collection("events").document(eventId)
            .delete()
            .addOnSuccessListener {
                eventsList = eventsList.toMutableList().also { it.removeAt(position) }
                notifyItemRemoved(position)
                Log.d("EventsAdapter", "Event successfully deleted")
            }
            .addOnFailureListener { e ->
                Log.w("EventsAdapter", "Error deleting event", e)
            }
    }

    private fun updateAttendButtonAndSpotsUI(holder: ViewHolder, event: Event) {
        val availableSpots = event.spots - event.currentAttendeesCount
        if (availableSpots <= 0) {
            holder.openSpotsTextView.text = holder.itemView.context.getString(R.string.event_full)
            holder.attendButtonTextView.visibility = if (event.attendedPeopleProfilePictureUrls.contains(FirebaseAuth.getInstance().currentUser?.photoUrl.toString())) View.VISIBLE else View.GONE
        } else {
            holder.openSpotsTextView.text = "${availableSpots} ${holder.itemView.context.getString(R.string.spots)}"
            holder.attendButtonTextView.visibility = View.VISIBLE
        }
    }


    private fun refreshAttendedPeopleLinearLayout(holder: ViewHolder, profilePictureUrls: List<String>) {
        val attendedPeopleLinearLayout = holder.itemView.findViewById<LinearLayout>(R.id.attendedPeopleLinearLayout)
        attendedPeopleLinearLayout.removeAllViews()
        val maxProfilesToShow = 3
        val profilesToShow = profilePictureUrls.take(maxProfilesToShow)
        profilesToShow.forEach { url ->
            val imageView = ImageView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    holder.itemView.dpToPx(40),
                    holder.itemView.dpToPx(40)
                ).apply {
                    marginEnd = holder.itemView.dpToPx(8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(holder.itemView.context)
                    .load(url)
                    .placeholder(R.drawable.profile_image_round)
                    .circleCrop()
                    .into(this)
            }
            attendedPeopleLinearLayout.addView(imageView)
        }
        if (profilePictureUrls.size > maxProfilesToShow) {  // TODO what to show??
            val moreAttendeesTextView = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = "...."
                textSize = 16f
                gravity = Gravity.CENTER_VERTICAL
            }
            attendedPeopleLinearLayout.addView(moreAttendeesTextView)
        }

    }

    private fun toggleSavedEvent(eventId: String, isSaved: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId ?: return)
        if (isSaved) {
            userRef.update("savedEvents", FieldValue.arrayRemove(eventId))
                .addOnSuccessListener {
                    Log.d("EventsAdapter", "Saving event ID: $eventId")
                    Log.d("EventsAdapter", "Event $eventId removed from saved events")
                }
                .addOnFailureListener { e ->
                    Log.d("EventsAdapter", "Saving event ID: $eventId")
                    Log.e("EventsAdapter", "Error removing event from saved events", e)
                }
        } else {
            userRef.update("savedEvents", FieldValue.arrayUnion(eventId))
                .addOnSuccessListener {
                    Log.d("EventsAdapter", "Saving event ID: $eventId")
                    Log.d("EventsAdapter", "Event $eventId added to saved events")
                }
                .addOnFailureListener { e ->
                    Log.d("EventsAdapter", "Saving event ID: $eventId")
                    Log.e("EventsAdapter", "Error adding event to saved events", e)
                }
        }
    }






}


