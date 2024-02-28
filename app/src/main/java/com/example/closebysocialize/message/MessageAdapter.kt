package com.example.closebysocialize.message

import com.example.closebysocialize.dataClass.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MessageAdapter(private val messages: MutableList<Message>, private val currentUserId: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val SENT_MESSAGE = 1
    private val RECEIVED_MESSAGE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENT_MESSAGE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_message, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_message, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val nextMessageSenderId = messages.getOrNull(position + 1)?.senderId
        val isLastMessageFromSender = nextMessageSenderId != message.senderId || position == messages.size - 1

        if (getItemViewType(position) == SENT_MESSAGE) {
            (holder as SentMessageViewHolder).bind(message, isLastMessageFromSender)
        } else {
            (holder as ReceivedMessageViewHolder).bind(message, isLastMessageFromSender)
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
        date ?: return "Timestamp unavailable"
        val now = Calendar.getInstance()
        val messageDate = Calendar.getInstance().apply { time = date }
        val format: SimpleDateFormat = when {
            isSameDay(now, messageDate) -> SimpleDateFormat("HH:mm", Locale.getDefault())
            isWithinLastWeek(now, messageDate) -> SimpleDateFormat("E HH:mm", Locale.getDefault())
            else -> SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        }
        return format.format(date)
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isWithinLastWeek(now: Calendar, messageDate: Calendar): Boolean {
        val oneWeekAgo = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }
        return messageDate.after(oneWeekAgo) && messageDate.before(now)
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
                messageTimestamp.visibility = if (messageTimestamp.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }
    }

    inner class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.receivedText)
        private val messageTimestamp: TextView = view.findViewById(R.id.messageReceivedTimestamp)
        private val profilePicture: ImageView = view.findViewById(R.id.receivedProfilePicture)

        fun bind(message: Message, showProfilePicture: Boolean) {
            messageText.text = message.content
            messageTimestamp.text = formatTimestamp(message.timestamp)
            profilePicture.visibility = if (showProfilePicture) View.VISIBLE else View.INVISIBLE

        }
    }


}
