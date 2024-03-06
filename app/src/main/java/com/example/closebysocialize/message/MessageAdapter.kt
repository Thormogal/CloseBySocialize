package com.example.closebysocialize.message

import android.content.Context
import com.example.closebysocialize.dataClass.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.FragmentUtils
import com.example.closebysocialize.utils.ImageUtils
import com.example.closebysocialize.utils.TimeUtils
import java.util.Date

class MessageAdapter(
    private val context: Context,
    private val messages: MutableList<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val SENT_MESSAGE = 1
        private const val RECEIVED_MESSAGE = 2
    }

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
        val nextMessage = messages.getOrNull(position + 1)
        val isLastMessageBySender = nextMessage == null || nextMessage.senderId != message.senderId

        val showProfilePicture = when (holder) {
            is ReceivedMessageViewHolder -> isLastMessageBySender
            else -> false
        }

        if (getItemViewType(position) == SENT_MESSAGE) {
            (holder as SentMessageViewHolder).bind(message, showTimestamp = true)
        } else {
            (holder as ReceivedMessageViewHolder).bind(
                message, showTimestamp = true, showProfilePicture = showProfilePicture
            )
        }

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
                message, showTimestamp, showProfilePicture
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
            messageTimestamp.text = TimeUtils.formatTimestamp(context, message.timestamp)
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
            messageText.text = message.content
            messageTimestamp.text = TimeUtils.formatTimestamp(context, message.timestamp)
            messageTimestamp.visibility = if (showTimestamp) View.VISIBLE else View.GONE

            FirestoreUtils.fetchProfileImageUrl(message.senderId,
                context,
                onSuccess = { profileImageUrl ->
                    ImageUtils.loadProfileImage(context, profileImageUrl, profilePicture)
                },
                onFailure = {
                })

            profilePicture.visibility = if (showProfilePicture) View.VISIBLE else View.INVISIBLE

            profilePicture.setOnClickListener {
                if (context is AppCompatActivity) {
                    FragmentUtils.openUserProfile(context, message.senderId)
                }
            }
        }

    }

}