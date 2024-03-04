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
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.TabType
import com.example.closebysocialize.dataClass.Event
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.FirestoreUtils.toggleAttendingEvent
import com.example.closebysocialize.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventsAdapter(
    private var attendingEventsList: List<Event>,
    private var eventsList: List<Event>,
    private var savedEventsList: List<Event>,
    private val userId: String,
    var savedEventsIds: MutableSet<String>,
    var attendingEventsIds: MutableSet<String>,
    private val eventInteractionListener: EventInteractionListener

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
        val event = when (currentTab) {
            TabType.ALL_EVENTS -> eventsList[position]
            TabType.ATTENDING_EVENTS -> attendingEventsList[position]
            TabType.SAVED_EVENTS -> savedEventsList[position]
        }
        updateAttendButtonAndSpotsUI(holder, event)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid
        val userProfileUrl = currentUser?.photoUrl.toString()

        val isEventCreator = event.authorId == currentUserId
        val isCurrentlyAttending =
            event.attendedPeopleProfilePictureUrls.contains(userProfileUrl) || isEventCreator
        holder.attendButtonTextView.text =
            if (isCurrentlyAttending) holder.itemView.context.getString(R.string.event_withdraw) else holder.itemView.context.getString(
                R.string.event_attend
            )
        holder.deleteImageView.visibility = if (isEventCreator) View.VISIBLE else View.GONE

        val isSaved = savedEventsIds.contains(event.id)

        holder.savedImageView.setImageResource(if (isSaved) R.drawable.icon_heart_filled else R.drawable.icon_heart)
        holder.savedImageView.setOnClickListener {
            val eventId = event.id
            val currentlySaved = savedEventsIds.contains(eventId)
            onToggleSaveEvent(eventId, currentlySaved)
        }
        ImageUtils.loadProfileImage(
            holder.itemView.context,
            event.authorProfileImageUrl,
            holder.authorProfilePictureImageView
        )

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
        holder.openSpotsTextView.text =
            "${availableSpots} ${holder.itemView.context.getString(R.string.spots)}"
        holder.chatImageView.setOnClickListener {
            chatImageViewClickListener?.invoke(event.id)
        }
        holder.deleteImageView.setOnClickListener {
            deleteEvent(event.id, position)
        }
        holder.attendButtonTextView.setOnClickListener {
            val isAttending =
                holder.attendButtonTextView.text.toString() == holder.itemView.context.getString(R.string.event_withdraw)
            val eventId = event.id
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: return@setOnClickListener
            val userProfileUrl = currentUser.photoUrl.toString()
            toggleAttendingEvent(userId, eventId, isAttending,
                onSuccess = {
                    val updatedCount =
                        if (isAttending) event.currentAttendeesCount - 1 else event.currentAttendeesCount + 1
                    event.currentAttendeesCount = updatedCount
                    event.attendedPeopleProfilePictureUrls = if (isAttending) {
                        event.attendedPeopleProfilePictureUrls.filter { it != userProfileUrl }
                            .toMutableList()
                    } else {
                        event.attendedPeopleProfilePictureUrls.toMutableList()
                            .apply { add(userProfileUrl) }
                    }
                    holder.openSpotsTextView.text =
                        "${event.spots - updatedCount} ${holder.itemView.context.getString(R.string.spots)}"
                    holder.attendButtonTextView.text =
                        holder.itemView.context.getString(if (isAttending) R.string.event_attend else R.string.event_withdraw)
                    refreshAttendedPeopleLinearLayout(
                        holder,
                        event.attendedPeopleProfilePictureUrls
                    )
                    updateAttendButtonAndSpotsUI(holder, event)
                },
                onFailure = { exception ->
                    Log.e("EventsAdapter", "Error toggling attendance", exception)
                }
            )
        }

        val attendedPeopleLinearLayout =
            holder.itemView.findViewById<LinearLayout>(R.id.attendedPeopleLinearLayout)
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
            }
            ImageUtils.loadProfileImage(holder.itemView.context, url, imageView)
            attendedPeopleLinearLayout.addView(imageView)
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
            holder.attendButtonTextView.visibility =
                if (event.attendedPeopleProfilePictureUrls.contains(FirebaseAuth.getInstance().currentUser?.photoUrl.toString())) View.VISIBLE else View.GONE
        } else {
            holder.openSpotsTextView.text =
                "${availableSpots} ${holder.itemView.context.getString(R.string.spots)}"
            holder.attendButtonTextView.visibility = View.VISIBLE
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

    private fun refreshAttendedPeopleLinearLayout(
        holder: ViewHolder,
        profilePictureUrls: List<String>
    ) {
        val attendedPeopleLinearLayout =
            holder.itemView.findViewById<LinearLayout>(R.id.attendedPeopleLinearLayout)
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
            }
            ImageUtils.loadProfileImage(holder.itemView.context, url, imageView)
            attendedPeopleLinearLayout.addView(imageView)
        }
        if (profilePictureUrls.size > maxProfilesToShow) {
            val remainingAttendeesCount = profilePictureUrls.size - maxProfilesToShow
            val moreAttendeesTextView = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "+$remainingAttendeesCount more"
                textSize = 16f
                gravity = Gravity.CENTER_VERTICAL
            }
            attendedPeopleLinearLayout.addView(moreAttendeesTextView)
        }
    }

}


