package com.example.golfcherries.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.closebysocialize.ChatFragment
import com.example.closebysocialize.Comment
import com.example.closebysocialize.R

class CommentAdapter(val comments: MutableList<Comment>, val fragment: ChatFragment) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val commentsUserName: TextView = view.findViewById(R.id.commentsUserName)
        val commentsText: TextView = view.findViewById(R.id.commentsText)
        val commentsProfilePicture: ImageView = view.findViewById(R.id.commentsProfilePicture)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.commentsUserName.text = comment.displayName
        holder.commentsText.text = comment.commentText

        Glide.with(fragment)
            .load(comment.profileImageUrl)
            .circleCrop()
            .into(holder.commentsProfilePicture)
    }

    fun updateComments(newComments: List<Comment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }


    override fun getItemCount() = comments.size
}