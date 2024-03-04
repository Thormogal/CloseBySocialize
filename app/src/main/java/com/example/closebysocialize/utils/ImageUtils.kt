package com.example.closebysocialize.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.closebysocialize.R

object ImageUtils {
    fun loadProfileImage(context: Context, imageUrl: String?, imageView: ImageView) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imageUrl)
                .circleCrop()
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.avatar_dark)
        }
    }
}