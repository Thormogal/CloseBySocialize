package com.example.closebysocialize.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CommentAdapter(private val comments: MutableList<Comment>, private val fragment: Fragment) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    interface CommentInteractionListener {
        fun onReply(commentId: String)
    }
    companion object {
        private const val COMMENT_TYPE = 0
        private const val REPLY_TYPE = 1
    }
    var listener: CommentInteractionListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commentsUserName: TextView = view.findViewById(R.id.commentsUserName)
        val commentsText: TextView = view.findViewById(R.id.commentsText)
        val commentsProfilePicture: ImageView = view.findViewById(R.id.commentsProfilePicture)
        val commentTimestamp: TextView = view.findViewById(R.id.commentsTimestamp)
        val replyText: TextView = view.findViewById(R.id.replyTextView)
    }
    override fun getItemViewType(position: Int): Int {
        return if (comments[position].parentId == null) COMMENT_TYPE else REPLY_TYPE
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == COMMENT_TYPE) R.layout.item_comment else R.layout.item_comment // TODO change if it should look different
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        val currentComment = comments[position]
        val isReply = comment.parentId != null
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (isReply) {
            layoutParams.marginStart = 50
        } else {
            layoutParams.marginStart = 0
        }
        layoutParams.marginStart = if (isReply) 100
        else 0
        holder.itemView.layoutParams = layoutParams
        val imageSize = if (isReply) 20 else 40
        val sizePx = imageSize.dpToPx(holder.itemView.context)
        holder.commentsProfilePicture.layoutParams.width = sizePx
        holder.commentsProfilePicture.layoutParams.height = sizePx

        holder.commentsUserName.text = currentComment.displayName
        holder.commentsUserName.visibility = View.VISIBLE
        Glide.with(fragment)
            .load(currentComment.profileImageUrl)
            .circleCrop()
            .placeholder(R.drawable.profile_image_round)
            .error(R.drawable.profile_image_round)
            .into(holder.commentsProfilePicture)
        holder.commentsProfilePicture.visibility = View.VISIBLE

        holder.commentsText.text = currentComment.commentText
        holder.commentTimestamp.text = formatTimestamp(currentComment.timestamp)
        holder.replyText.setOnClickListener {
            listener?.onReply(currentComment.id)
        }
    }




    private fun formatTimestamp(date: Date?): String {
        date ?: return fragment.getString(R.string.date_unknown)
        val diff = Date().time - date.time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30
        val years = days / 365
        return when {
            years > 0 -> fragment.resources.getQuantityString(R.plurals.years_ago, years.toInt(), years)
            months > 0 -> fragment.resources.getQuantityString(R.plurals.months_ago, months.toInt(), months)
            weeks > 0 -> fragment.resources.getQuantityString(R.plurals.weeks_ago, weeks.toInt(), weeks)
            days > 0 -> fragment.resources.getQuantityString(R.plurals.days_ago, days.toInt(), days)
            hours > 0 -> fragment.resources.getQuantityString(R.plurals.hours_ago, hours.toInt(), hours)
            minutes > 0 -> fragment.resources.getQuantityString(R.plurals.minutes_ago, minutes.toInt(), minutes)
            else -> fragment.getString(R.string.just_now)
        }
    }




    fun updateComments(newComments: List<Comment>) {
        val sortedComments = newComments.sortedByDescending { it.timestamp }
        comments.clear()
        comments.addAll(sortedComments)
        notifyDataSetChanged()
    }


    fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
    override fun getItemCount() = comments.size
}