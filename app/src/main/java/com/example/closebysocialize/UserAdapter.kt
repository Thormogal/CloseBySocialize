
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Users
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class UserAdapter(private var users: List<Users>, private val taggedUsers: MutableList<String>, private val eventGuests: EditText, private val chipGroup: ChipGroup) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

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



        holder.itemView.setOnClickListener {
            if (!taggedUsers.contains(user.id)) {
                taggedUsers.add(user.id)
                val chip = Chip(holder.itemView.context)

                chip.text = user.name
                chip.isCloseIconVisible = true
                chip.setChipBackgroundColorResource(R.color.primary_blue)
                chip.setOnCloseIconClickListener {
                    taggedUsers.remove(user.id)
                    users = users.filter { it.id != user.id }
                    chipGroup.removeView(chip)
                    notifyDataSetChanged()
                    holder.userName.setBackgroundColor(Color.TRANSPARENT)

                }
                chipGroup.addView(chip)
                holder.userName.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.primary_blue))
            }
        }
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userName)

    }
}