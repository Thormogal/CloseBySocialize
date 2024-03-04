import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Friend
import com.example.closebysocialize.utils.ImageUtils

class FriendsAdapter(
    private val context: Context,
    private var friends: List<Friend>,
    private val showActions: Boolean = true
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

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
        private val binIcon: ImageView = view.findViewById(R.id.binIcon)

        fun bind(friend: Friend) {
            nameTextView.text = friend.name
            ImageUtils.loadProfileImage(itemView.context, friend.profileImageUrl, profileImageView)
            binIcon.visibility = if (showActions) View.VISIBLE else View.GONE
            itemView.setOnClickListener {
                listener?.onFriendClick(friend)
            }
            binIcon.setOnClickListener {
                listener?.onBinClick(friend)
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
