package com.example.closebysocialize

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.dataClass.Users

class UserAdapter(private var users: List<Users>, private val taggedUsers: MutableList<String>, private val editText: EditText) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun updateData(newUsers: List<Users>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount() = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)

        fun bind(user: Users) {
            nameTextView.text = user.name
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedUser = users[position]
                    if (!taggedUsers.contains(clickedUser.id)) {
                        taggedUsers.add(clickedUser.id)

                        val existingTextParts = editText.text.toString().split(",")
                        val existingText = existingTextParts.dropLast(1).joinToString(",")

                        val newText = "$existingText  ${clickedUser.name}, "
                        editText.setText(newText)
                        editText.setSelection(editText.text.length)
                    }
                }
            }
        }
    }
}
