package com.example.closebysocialize.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Comment
import com.example.closebysocialize.message.FireBaseMessagingService.Companion.TAG
import com.example.closebysocialize.utils.CommentDiffCallback
import com.example.closebysocialize.utils.ImageUtils
import com.example.closebysocialize.utils.TimeUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date

class CommentAdapter(
    private val comments: MutableList<Comment>,
    private val context: Context,
    private val fragment: Fragment,
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
        val likeText: TextView = view.findViewById(R.id.likeTextView)
        val likeCounter: TextView = view.findViewById(R.id.likedAmountTextView)
        val likeIconImageView: ImageView = view.findViewById(R.id.likedImageView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (comments[position].parentId == null) COMMENT_TYPE else REPLY_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout =
            if (viewType == COMMENT_TYPE) R.layout.item_comment else R.layout.item_comment
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        val isReply = comment.parentId != null
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (isReply) {
            layoutParams.marginStart = 50
        } else {
            layoutParams.marginStart = 0
        }
        layoutParams.marginStart = if (isReply) 160 else 5
        holder.itemView.layoutParams = layoutParams
        val imageSize = if (isReply) 20 else 40
        val sizePx = imageSize.dpToPx(holder.itemView.context)
        holder.commentsProfilePicture.layoutParams.width = sizePx
        holder.commentsProfilePicture.layoutParams.height = sizePx
        holder.commentsUserName.text = comment.displayName
        holder.commentsUserName.visibility = View.VISIBLE
        ImageUtils.loadProfileImage(holder.itemView.context, comment.profileImageUrl, holder.commentsProfilePicture)
        holder.commentsProfilePicture.visibility = View.VISIBLE

        holder.commentsText.text = comment.commentText
        holder.commentTimestamp.text = TimeUtils.formatTimestampEventChat(context, comment.timestamp)


        holder.replyText.setOnClickListener {
            listener?.onReply(comment.id)
        }
        holder.likeCounter.text = comment.likes.toString()
        holder.likeCounter.visibility = if (comment.likes > 0) View.VISIBLE else View.INVISIBLE
        holder.likeIconImageView.visibility =
            if (comment.likes > 0) View.VISIBLE else View.INVISIBLE
        holder.likeText.setOnClickListener {
            if (comment.isLiked) {
                comment.likes--
                comment.isLiked = false
            } else {
                comment.likes++
                comment.isLiked = true
            }
            holder.likeCounter.text = comment.likes.toString()
            holder.likeCounter.visibility = if (comment.likes > 0) View.VISIBLE else View.INVISIBLE
            holder.likeIconImageView.visibility =
                if (comment.likes > 0) View.VISIBLE else View.INVISIBLE
            toggleLikeStatus(comment.id, comment.isLiked)
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

    private fun toggleLikeStatus(commentId: String, liked: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val eventRef = FirebaseFirestore.getInstance().collection("events").document(eventId!!)
        val commentRef = eventRef.collection("comments").document(commentId)

        val userLikeRef = commentRef.collection("likes").document(userId)

        if (liked) {
            userLikeRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "Like removed successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error removing like", e)
                }
        } else {
            val likeStatus = hashMapOf(
                "liked" to true
            )
            userLikeRef.set(likeStatus)
                .addOnSuccessListener {
                    Log.d(TAG, "Like added successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding like", e)
                }
        }
    }


    fun updateComments(newComments: List<Comment>) {
        val diffCallback = CommentDiffCallback(this.comments, newComments)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.comments.clear()
        this.comments.addAll(newComments)
        diffResult.dispatchUpdatesTo(this)
    }


    fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

    override fun getItemCount() = comments.size
}