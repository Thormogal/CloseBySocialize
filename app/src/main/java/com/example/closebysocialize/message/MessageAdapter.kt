package com.example.closebysocialize.message

import android.content.Context
import android.util.Log
import com.example.closebysocialize.dataClass.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import com.example.closebysocialize.utils.FragmentUtils
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessageAdapter(
    private val context: Context,
    private val messages: MutableList<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SENT_MESSAGE = 1
    private val RECEIVED_MESSAGE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENT_MESSAGE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sent_message, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_received_message, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val previousMessage = messages.getOrNull(position - 1)
        val showTimestamp = when {
            position == 0 -> true
            previousMessage == null -> true
            message.senderId != previousMessage.senderId -> true
            significantTimeGap(message.timestamp, previousMessage.timestamp) -> true
            else -> false
        }

        if (getItemViewType(position) == SENT_MESSAGE) {
            (holder as SentMessageViewHolder).bind(message, showTimestamp)
        } else {
            (holder as ReceivedMessageViewHolder).bind(
                message,
                showTimestamp,
                showProfilePicture = true
            )
        }
    }


    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            SENT_MESSAGE
        } else {
            RECEIVED_MESSAGE
        }
    }

    private fun formatTimestamp(date: Date?): String {
        date ?: return "Date unknown"
        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { time = date }
        val formatterTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        if (messageDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            messageDate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
        ) {
            return formatterTime.format(date)
        }
        val yesterday = now.apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (messageDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
            messageDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Yesterday, ${formatterTime.format(date)}"
        }
        val aWeekAgo = now.apply { add(Calendar.DAY_OF_YEAR, -7) }
        if (messageDate.after(aWeekAgo)) {
            val formatterDay = SimpleDateFormat("EEEE", Locale.getDefault())
            return formatterDay.format(date)
        }
        val formatterDate = SimpleDateFormat("dd MMMM HH:mm", Locale.getDefault())
        return formatterDate.format(date)
    }

    private fun significantTimeGap(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) {
            return false
        }
        val gap = 15 * 60 * 1000
        return Math.abs(date1.time - date2.time) > gap
    }

    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    inner class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.senderText)
        private val messageTimestamp: TextView = view.findViewById(R.id.messageSenderTimestamp)

        fun bind(message: Message, showTimestamp: Boolean) {
            messageText.text = message.content
            messageTimestamp.text = formatTimestamp(message.timestamp)
            messageTimestamp.visibility = if (showTimestamp) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                messageTimestamp.visibility =
                    if (messageTimestamp.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }
    }

    inner class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.receivedText)
        private val messageTimestamp: TextView = view.findViewById(R.id.messageReceivedTimestamp)
        private val profilePicture: ImageView = view.findViewById(R.id.receivedProfilePicture)

        fun bind(message: Message, showTimestamp: Boolean, showProfilePicture: Boolean) {
            Log.d("MessageAdapter", "Binding message from senderId: ${message.senderId}")

            messageText.text = message.content
            messageTimestamp.text = formatTimestamp(message.timestamp)
            messageTimestamp.visibility = if (showTimestamp) View.VISIBLE else View.GONE
            fetchProfileImageUrl(message.senderId) { profileImageUrl ->
                if (profileImageUrl.isNotEmpty()) {
                    Glide.with(context)
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(profilePicture)
                } else {
                    Glide.with(context)
                        .load(R.drawable.avatar_dark)
                        .circleCrop()
                        .into(profilePicture)
                }
            }
            profilePicture.visibility = if (showProfilePicture) View.VISIBLE else View.INVISIBLE

            profilePicture.setOnClickListener {
                if (context is AppCompatActivity) {
                    FragmentUtils.openUserProfile(context as AppCompatActivity, message.senderId)
                }
            }
        }
    }

    private fun fetchProfileImageUrl(senderId: String, callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(senderId).get().addOnSuccessListener { document ->
            val profileImageUrl = document.getString("profileImageUrl") ?: ""
            callback(profileImageUrl)
        }.addOnFailureListener {
            Log.e("MessageAdapter", "Failed to fetch profile image URL", it)
        }
    }
}