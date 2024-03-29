package com.example.closebysocialize.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Users

class UserAdapter(private var users: List<Users>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    var onItemClick: ((Users) -> Unit)? = null


    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        fun bind(user: Users, clickListener: ((Users) -> Unit)?) {
            nameTextView.text = user.name

            itemView.setOnClickListener {
                clickListener?.invoke(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], onItemClick)
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<Users>) {
        users = newUsers
        notifyDataSetChanged()
    }
}