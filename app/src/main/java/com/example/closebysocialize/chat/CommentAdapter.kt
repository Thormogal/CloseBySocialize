package com.example.closebysocialize.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Comment
import com.example.closebysocialize.message.FireBaseMessagingService.Companion.TAG
import com.example.closebysocialize.utils.CommentDiffCallback
import com.example.closebysocialize.utils.CommentsUtils
import com.example.closebysocialize.utils.ImageUtils
import com.example.closebysocialize.utils.TimeUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CommentAdapter(
    private val comments: MutableList<Comment>,
    private val context: Context,
    private val eventId: String?
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    private var commentListenerRegistration: ListenerRegistration? = null

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
        private val likeText: TextView = view.findViewById(R.id.likeTextView)
        private val likeCounter: TextView = view.findViewById(R.id.likedAmountTextView)
        private val likeIconImageView: ImageView = view.findViewById(R.id.likedImageView)

        fun applyLayoutChanges(isReply: Boolean) {
            itemView.layoutParams = (itemView.layoutParams as ViewGroup.MarginLayoutParams).apply {
                marginStart = if (isReply) 160 else 5
            }
        }

        fun setupLikeFeature(comment: Comment, eventId: String?) {
            likeCounter.text = comment.likes.toString()
            likeCounter.visibility = if (comment.likes > 0) View.VISIBLE else View.INVISIBLE
            likeIconImageView.visibility = if (comment.likes > 0) View.VISIBLE else View.INVISIBLE

            likeText.setOnClickListener {
                val newLikedStatus = !comment.isLiked
                comment.isLiked = newLikedStatus
                if (newLikedStatus) {
                    comment.likes += 1
                } else {
                    if (comment.likes > 0) comment.likes -= 1
                }
                updateLikeUI(comment)
                CommentsUtils.toggleLikeStatus(eventId ?: return@setOnClickListener,
                    comment.id,
                    newLikedStatus,
                    onSuccess = {
                        Log.d(TAG, "Like status toggled successfully.")
                    },
                    onFailure = { exception ->
                        comment.isLiked = !newLikedStatus
                        if (newLikedStatus) {
                            if (comment.likes > 0) comment.likes -= 1
                        } else {
                            comment.likes += 1
                        }
                        updateLikeUI(comment)
                        Log.e(TAG, "Failed to toggle like status", exception)
                    }
                )
            }
        }

        private fun updateLikeUI(comment: Comment) {
            likeCounter.text = comment.likes.toString()
            likeCounter.visibility = comment.likes.takeIf { it > 0 }?.let { View.VISIBLE } ?: View.INVISIBLE
            likeIconImageView.visibility = comment.isLiked.takeIf { it }?.let { View.VISIBLE } ?: View.INVISIBLE

        }

        fun adjustSize(isReply: Boolean) {
            val size = if (isReply) 20 else 40
            commentsProfilePicture.layoutParams.width = size.dpToPx(itemView.context)
            commentsProfilePicture.layoutParams.height = size.dpToPx(itemView.context)
        }

        private fun Int.dpToPx(context: Context): Int =
            (this * context.resources.displayMetrics.density).toInt()
    }

    override fun getItemViewType(position: Int): Int {
        return if (comments[position].parentId == null) COMMENT_TYPE else REPLY_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]

        holder.applyLayoutChanges(comment.parentId != null)
        holder.adjustSize(comment.parentId != null)
        holder.commentsUserName.text = comment.displayName
        ImageUtils.loadProfileImage(
            holder.itemView.context,
            comment.profileImageUrl,
            holder.commentsProfilePicture
        )
        holder.commentsText.text = comment.commentText
        holder.commentTimestamp.text =
            TimeUtils.formatTimestampEventChat(context, comment.timestamp)
        holder.setupLikeFeature(comment, eventId)
        holder.replyText.setOnClickListener {
            listener?.onReply(comment.id)
        }

    }

    fun setupRealtimeListener() {
        eventId?.let { eventId ->
            val eventRef = FirebaseFirestore.getInstance().collection("events").document(eventId)
                .collection("comments")
            commentListenerRegistration = eventRef.addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e(TAG, "Error listening for comments", exception)
                    return@addSnapshotListener
                }
                snapshot?.let { snapshot ->
                    val newComments = snapshot.documents.mapNotNull { document ->
                        document.toObject(Comment::class.java)
                    }
                    updateComments(newComments)
                }
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        commentListenerRegistration?.remove()
    }

    fun updateComments(newComments: List<Comment>) {
        val diffCallback = CommentDiffCallback(this.comments, newComments)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.comments.clear()
        this.comments.addAll(newComments)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = comments.size
}