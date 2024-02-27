package com.example.closebysocialize.message

import com.example.closebysocialize.dataClass.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import java.text.SimpleDateFormat
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
        if (getItemViewType(position) == SENT_MESSAGE) {
            (holder as SentMessageViewHolder).bind(message)
        } else {
            (holder as ReceivedMessageViewHolder).bind(message)
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

    inner class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.senderText)
        private val messageTimestamp: TextView = view.findViewById(R.id.messageSenderTimestamp)

        fun bind(message: Message) {
            messageText.text = message.content
            messageTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(message.timestamp.toDate())
        }
    }

    inner class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.receivedText)
        private val messageTimestamp: TextView = view.findViewById(R.id.messageReceivedTimestamp)
        fun bind(message: Message) {
            messageText.text = message.content
            messageTimestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(message.timestamp.toDate())
        }
    }
    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

}
