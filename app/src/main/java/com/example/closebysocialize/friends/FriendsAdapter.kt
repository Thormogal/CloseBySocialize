import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Friend
import com.example.closebysocialize.dataClass.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsAdapter(private val context: Context, private var friends: List<Friend>, private val showActions: Boolean = true) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    interface FriendClickListener {
        fun onMessageClick(friend: Friend)
        fun onBinClick(friend: Friend)
        fun onFriendClick(friend: Friend)
    }



    var listener: FriendClickListener? = null

    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        private val profileImageView: ImageView =
            view.findViewById(R.id.friendProfilePictureImageView)
        private val messageIcon: ImageView = view.findViewById(R.id.messageIcon)
        private val binIcon: ImageView = view.findViewById(R.id.binIcon)
        private val acceptIcon: ImageView = view.findViewById(R.id.acceptIcon)
        private val declineIcon: ImageView = view.findViewById(R.id.declineIcon)


        fun bind(friend: Friend) {
            nameTextView.text = friend.name
            Glide.with(itemView.context)
                .load(friend.profileImageUrl)
                .into(profileImageView)

            if (showActions) {
                messageIcon.visibility = View.VISIBLE
                binIcon.visibility = View.VISIBLE
            } else {
                messageIcon.visibility = View.GONE
                binIcon.visibility = View.GONE
            }

            if (friend.isRequest) {
                acceptIcon.visibility = View.VISIBLE
                declineIcon.visibility = View.VISIBLE
            } else {
                acceptIcon.visibility = View.GONE
                declineIcon.visibility = View.GONE
            }

            itemView.setOnClickListener {
                if (friend.isRequest) {
                    listener?.onFriendClick(friend)
                } else {
                    listener?.onMessageClick(friend)
                }
            }

            binIcon.setOnClickListener {
                listener?.onBinClick(friend)
            }

            acceptIcon.setOnClickListener {
                acceptFriendRequest(context, friend.requestId, friend.user)
            }

            declineIcon.setOnClickListener {
                rejectFriendRequest(friend.requestId)
            }
        }
    }
    private fun acceptFriendRequest(context: Context, requestId: String, user: Users) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        val friendData = hashMapOf(
            "id" to user.id,
            "email" to user.email,
            "profileImageUrl" to user.profileImageUrl,
            "name" to user.name
        )
        db.collection("users")
            .document(currentUser.uid)
            .collection("friends")
            .document(user.id)
            .set(friendData)
            .addOnSuccessListener {
                Toast.makeText(context, "Friend added successfully", Toast.LENGTH_SHORT).show()
                db.collection("friend_requests")
                    .document(requestId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to accept friend request", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add friend", Toast.LENGTH_SHORT).show()
            }
    }


    private fun rejectFriendRequest(requestId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("friend_requests")
            .document(requestId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Friend request rejected", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to reject friend request", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend)
    }

    override fun getItemCount() = friends.size

    fun updateData(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}
