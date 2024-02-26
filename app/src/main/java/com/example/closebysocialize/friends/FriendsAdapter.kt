package com.example.closebysocialize.friends

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsAdapter(var friends: List<Friend>, var selectionListener: FriendSelectionListener?) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    interface FriendSelectionListener {
        fun onSelectionChanged()
    }

    class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        private val selectionIndicator: ImageView = view.findViewById(R.id.selectionIndicator)
        val profileImageView: ImageView = view.findViewById(R.id.friendProfilePictureImageView)

        fun bind(friend: Friend) {
            nameTextView.text = friend.name
            if (friend.isSelected) {
                selectionIndicator.visibility = View.VISIBLE
                itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                selectionIndicator.visibility = View.GONE
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
        holder.itemView.setOnClickListener {
            friend.isSelected = !friend.isSelected
            notifyItemChanged(position)
            selectionListener?.onSelectionChanged()
        }
        friend.profileImageUrl?.let { url ->
            Glide.with(holder.itemView.context)
                .load(url)
                .circleCrop()
                .into(holder.profileImageView)
        }

    }

    override fun getItemCount() = friends.size


    fun updateData(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
    }

}