import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Users
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class UserAdapter(
    private var users: List<Users>,
    private val taggedUsers: MutableList<String>,
    private val eventGuests: EditText,
    private val chipGroup: ChipGroup,
    var callback: UserAdapterCallback? = null

) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var searchText: String = ""


    interface UserAdapterCallback {
        fun onUserRemoved()
    }

    fun setSearchText(text: String) {
        searchText = text
        notifyDataSetChanged()
    }

    fun updateData(newUsers: List<Users>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.userName.text = user.name

        if (user.name.equals(searchText, ignoreCase = true)) {
            holder.userName.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.primary_text
                )
            )
        } else {
            holder.userName.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.primary_text_forgot_password_blue
                )
            )
        }

        holder.itemView.setOnClickListener {
            val userId = user.id
            if (!taggedUsers.contains(userId)) {
                taggedUsers.add(userId)
                val chip = Chip(holder.itemView.context)
                chip.text = user.name
                chip.isCloseIconVisible = true
                chip.setChipBackgroundColorResource(R.color.primary_blue)


                chip.setOnCloseIconClickListener {
                    Log.d("UserAdapter", "Försöker ta bort användare: ${user.name} med ID: ${user.id}")
                    taggedUsers.remove(user.id)
                    chipGroup.removeView(chip)
                    Log.d("UserAdapter", "Användare borttagen: ${user.name}, återstående användare i taggedUsers: $taggedUsers")
                    callback?.onUserRemoved()
                    Log.d("UserAdapter", "Callback för onUserRemoved anropad")
                }

                chipGroup.addView(chip)
                users = users.filter { it.id != user.id }
                notifyDataSetChanged()

            }
        }
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userName)

    }
}