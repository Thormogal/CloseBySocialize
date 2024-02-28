package com.example.closebysocialize.chat

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

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commentsUserName: TextView = view.findViewById(R.id.commentsUserName)
        val commentsText: TextView = view.findViewById(R.id.commentsText)
        val commentsProfilePicture: ImageView = view.findViewById(R.id.commentsProfilePicture)
        val commentTimestamp: TextView = view.findViewById(R.id.commentsTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentComment = comments[position]
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



    override fun getItemCount() = comments.size
}