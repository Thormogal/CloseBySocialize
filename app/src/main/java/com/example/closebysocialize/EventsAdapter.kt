package com.example.closebysocialize

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class EventsAdapter(private var eventsList: List<Event>) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    var chatImageViewClickListener: ((String) -> Unit)? = null

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
        val editImageView: ImageView = view.findViewById(R.id.editImageView)
        val deleteImageView: ImageView = view.findViewById(R.id.deleteImageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventsList[position]
        Glide.with(holder.itemView.context)
            .load(event.profileImageUrl)
            .circleCrop()
            .into(holder.authorProfilePictureImageView)
        holder.cityTextView.text = event.city
        holder.eventTypeTextView.text = event.eventType
        holder.eventNameTextView.text = event.title
        holder.locationTextView.text = event.location
        holder.dayTextView.text = event.day
        holder.timeTextView.text = event.time
        holder.dateTextView.text = event.date
        holder.usernameTextView.text = event.author
        holder.descriptionTextView.text = event.description
        holder.openSpotsTextView.text = event.spots
        holder.chatImageView.setOnClickListener {
            chatImageViewClickListener?.invoke(event.id)
        }
        holder.attendButtonTextView.setOnClickListener {
            val attendText = holder.itemView.context.getString(R.string.event_attend)
            if (holder.attendButtonTextView.text.toString().equals(attendText, ignoreCase = true)) {
                holder.attendButtonTextView.text = holder.itemView.context.getString(R.string.event_withdraw)
            } else {
                holder.attendButtonTextView.text = attendText
            }
        }


        val currentUserId = getCurrentUserId()
        if (event.authorId == currentUserId && currentUserId != null) {
            holder.editImageView.visibility = View.VISIBLE
            holder.deleteImageView.visibility = View.VISIBLE
        } else {
            holder.editImageView.visibility = View.GONE
            holder.deleteImageView.visibility = View.GONE
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
    private fun getCurrentUserId(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.uid
    }
    fun View.dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}


