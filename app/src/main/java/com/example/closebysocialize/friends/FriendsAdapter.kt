import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Friend

class FriendsAdapter(private var friends: List<Friend>, private val showActions: Boolean = true) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

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

            itemView.setOnClickListener {
                listener?.onMessageClick(friend)
            }

            binIcon.setOnClickListener {
                listener?.onBinClick(friend)
            }
            itemView.setOnClickListener {
                listener?.onFriendClick(friend)
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
    }

    override fun getItemCount() = friends.size

    fun updateData(newFriends: List<Friend>) {
        friends = newFriends
        notifyDataSetChanged()
    }
}