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

class FriendsAdapter(var friends: List<Friend>) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

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
            removeFriendFromFirestore(friend.id, position)
            //friend.isSelected = !friend.isSelected
            //notifyItemChanged(position)
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


    private fun removeFriendFromFirestore(friendId: String, position: Int) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val friendDocRef = db.collection("users").document(userId).collection("friends").document(friendId)
        friendDocRef
            .delete()
            .addOnSuccessListener {
                removeFriendAtPosition(position)
                Log.d("FriendsAdapter", "Friend successfully deleted from Firestore")
            }
            .addOnFailureListener { e ->
                Log.w("FriendsAdapter", "Error deleting friend from Firestore", e)
            }
    }


    fun removeFriendAtPosition(position: Int) {
        if (position >= 0 && position < friends.size) {
            friends = friends.toMutableList().apply {
                removeAt(position)
            }
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, friends.size)
        }
    }
}