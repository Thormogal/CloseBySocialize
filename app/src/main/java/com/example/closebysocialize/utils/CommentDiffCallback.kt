package com.example.closebysocialize.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.closebysocialize.dataClass.Comment

class CommentDiffCallback(private val oldList: List<Comment>, private val newList: List<Comment>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
