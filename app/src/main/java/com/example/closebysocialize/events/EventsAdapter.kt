package com.example.closebysocialize.events

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.TabType
import com.example.closebysocialize.dataClass.Event
import com.example.closebysocialize.utils.EventsDiffCallback
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.FirestoreUtils.toggleAttendingEvent
import com.example.closebysocialize.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

class EventsAdapter(
    private var attendingEventsList: List<Event>,
    private var eventsList: MutableList<Event>,
    private var savedEventsList: List<Event>,
    private val savedEventsIds: MutableSet<String>

) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    var chatImageViewClickListener: ((String) -> Unit)? = null
    private var currentTab: TabType = TabType.ALL_EVENTS

    interface EventInteractionListener {
        fun onToggleSaveEvent(eventId: String, isCurrentlySaved: Boolean)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val authorProfilePictureImageView: ImageView =
            view.findViewById(R.id.authorProfilePictureImageView)
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
        val event = getEventBasedOnTab(position)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid
        val userProfileUrl = currentUser?.photoUrl.toString()
        val isEventCreator = event.authorId == currentUserId
        val isSaved = savedEventsIds.contains(event.id)
        val availableSpots = event.spots - event.currentAttendeesCount
        val availableSpotsText =
            "$availableSpots ${holder.itemView.context.getString(R.string.spots)}"
        val isCurrentlyAttending =
            event.attendedPeopleProfilePictureUrls.contains(userProfileUrl) || isEventCreator

        updateEventDetails(holder, event)

        holder.attendButtonTextView.text =
            if (isCurrentlyAttending) holder.itemView.context.getString(R.string.event_withdraw) else holder.itemView.context.getString(
                R.string.event_attend
            )
        holder.deleteImageView.visibility = if (isEventCreator) View.VISIBLE else View.GONE
        holder.savedImageView.setImageResource(if (isSaved) R.drawable.icon_heart_filled else R.drawable.icon_heart)
        holder.savedImageView.setOnClickListener {
            val eventId = event.id
            val currentlySaved = savedEventsIds.contains(eventId)
            onToggleSaveEvent(eventId, currentlySaved)
        }
        holder.openSpotsTextView.text = availableSpotsText
        holder.deleteImageView.setOnClickListener {
            deleteEvent(event.id, position)
        }
        holder.attendButtonTextView.setOnClickListener {
            toggleEventAttendance(holder, event)
        }
        populateAttendedPeopleViews(holder, event)

    }

    private fun toggleEventAttendance(holder: ViewHolder, event: Event) {
        val isCurrentlyAttending =
            holder.attendButtonTextView.text.toString() == holder.itemView.context.getString(R.string.event_withdraw)
        val eventId = event.id
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val userId = currentUser.uid
        val userProfileUrl = currentUser.photoUrl?.toString() ?: ""
        toggleAttendingEvent(userId, eventId, userProfileUrl, isCurrentlyAttending, onSuccess = {
            val updatedCount =
                if (isCurrentlyAttending) event.currentAttendeesCount - 1 else event.currentAttendeesCount + 1
            event.currentAttendeesCount = updatedCount
            event.attendedPeopleProfilePictureUrls = if (isCurrentlyAttending) {
                event.attendedPeopleProfilePictureUrls.filter { it != userProfileUrl }
                    .toMutableList()
            } else {
                event.attendedPeopleProfilePictureUrls.toMutableList().apply { add(userProfileUrl) }
            }
            val spotsString = holder.itemView.context.getString(R.string.spots)
            val availableSpotsText = holder.itemView.context.getString(
                R.string.available_spots_format, event.spots - updatedCount, spotsString
            )
            holder.openSpotsTextView.text = availableSpotsText
            holder.attendButtonTextView.text =
                holder.itemView.context.getString(if (isCurrentlyAttending) R.string.event_attend else R.string.event_withdraw)
            updateAttendButtonAndSpotsUI(holder, event)
            populateAttendedPeopleViews(holder, event)

        }, onFailure = { exception ->
            Log.e("EventsAdapter", "Error toggling attendance", exception)
        })
    }


    private fun populateAttendedPeopleViews(holder: ViewHolder, event: Event) {
        val attendedPeopleLinearLayout =
            holder.itemView.findViewById<LinearLayout>(R.id.attendedPeopleLinearLayout)
        attendedPeopleLinearLayout.removeAllViews()

        val maxProfilesToShow = 3
        val profilePictureUrls = event.attendedPeopleProfilePictureUrls

        for ((index, url) in profilePictureUrls.take(maxProfilesToShow).withIndex()) {
            val imageView = createImageView(holder.itemView.context, url)
            attendedPeopleLinearLayout.addView(imageView)

            if (index == maxProfilesToShow - 1 && profilePictureUrls.size > maxProfilesToShow) {
                val remainingAttendeesCount = profilePictureUrls.size - maxProfilesToShow
                val moreAttendeesTextView =
                    createMoreAttendeesTextView(holder.itemView.context, remainingAttendeesCount)
                attendedPeopleLinearLayout.addView(moreAttendeesTextView)
            }
        }
    }

    private fun createImageView(context: Context, url: String): ImageView {
        val imageView = ImageView(context)
        val size = context.dpToPx(40)
        val marginEnd = context.dpToPx(8)
        val layoutParams = LinearLayout.LayoutParams(size, size).apply {
            this.marginEnd = marginEnd
        }
        imageView.layoutParams = layoutParams
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        ImageUtils.loadProfileImage(context, url, imageView)
        return imageView
    }


    private fun createMoreAttendeesTextView(
        context: Context, remainingAttendeesCount: Int
    ): TextView {
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = context.getString(R.string.more_attendees_count, remainingAttendeesCount)
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
        }
    }


    private fun updateAttendButtonAndSpotsUI(holder: ViewHolder, event: Event) {
        val availableSpots = event.spots - event.currentAttendeesCount
        if (availableSpots <= 0) {
            holder.openSpotsTextView.text = holder.itemView.context.getString(R.string.event_full)
            val currentUserProfileUrl = FirebaseAuth.getInstance().currentUser?.photoUrl.toString()
            val isCurrentUserAttending =
                event.attendedPeopleProfilePictureUrls.contains(currentUserProfileUrl)
            holder.attendButtonTextView.visibility =
                if (isCurrentUserAttending) View.VISIBLE else View.GONE
        } else {
            val spotsString = holder.itemView.context.getString(R.string.spots)
            val availableSpotsText = holder.itemView.context.getString(
                R.string.available_spots_format, availableSpots, spotsString
            )
            holder.openSpotsTextView.text = availableSpotsText
            holder.attendButtonTextView.visibility = View.VISIBLE
        }
    }

    private fun updateEventDetails(holder: ViewHolder, event: Event) {
        holder.cityTextView.text = event.city
        holder.eventTypeTextView.text = event.eventType
        holder.eventNameTextView.text = event.title
        holder.locationTextView.text = event.location
        holder.dayTextView.text = event.day
        holder.timeTextView.text = event.time
        holder.dateTextView.text = event.date
        holder.usernameTextView.text = event.authorFirstName
        holder.descriptionTextView.text = event.description
        ImageUtils.loadProfileImage(
            holder.itemView.context,
            event.authorProfileImageUrl,
            holder.authorProfilePictureImageView
        )
        holder.chatImageView.setOnClickListener {
            chatImageViewClickListener?.invoke(event.id)
        }
    }

    private fun getEventBasedOnTab(position: Int): Event {
        return when (currentTab) {
            TabType.ALL_EVENTS -> eventsList[position]
            TabType.ATTENDING_EVENTS -> attendingEventsList[position]
            TabType.SAVED_EVENTS -> savedEventsList[position]
        }
    }

    override fun getItemCount(): Int {
        return when (currentTab) {
            TabType.ALL_EVENTS -> eventsList.size
            TabType.ATTENDING_EVENTS -> attendingEventsList.size
            TabType.SAVED_EVENTS -> savedEventsList.size
        }
    }

    fun updateData(newEventsList: List<Event>) {
        val diffResult = DiffUtil.calculateDiff(EventsDiffCallback(eventsList, newEventsList))
        eventsList.clear()
        eventsList.addAll(newEventsList)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun deleteEvent(eventId: String, position: Int) {
        FirebaseFirestore.getInstance().collection("events").document(eventId).delete()
            .addOnSuccessListener {
                eventsList = eventsList.toMutableList().also { it.removeAt(position) }
                notifyItemRemoved(position)
                Log.d("EventsAdapter", "Event successfully deleted")
            }.addOnFailureListener { e ->
                Log.w("EventsAdapter", "Error deleting event", e)
            }
    }

    private fun onToggleSaveEvent(eventId: String, isCurrentlySaved: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreUtils.toggleSavedEvent(userId, eventId, !isCurrentlySaved, onSuccess = {
            if (isCurrentlySaved) {
                savedEventsIds.remove(eventId)
            } else {
                savedEventsIds.add(eventId)
            }
            notifyDataSetChanged()
        }, onFailure = { e ->
            Log.e("EventsFragment", "Failed to toggle event save state", e)
        })
    }


}


