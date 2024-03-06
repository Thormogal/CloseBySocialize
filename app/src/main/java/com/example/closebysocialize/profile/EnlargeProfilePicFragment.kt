package com.example.closebysocialize.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.closebysocialize.R

class EnlargeProfilePicFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_enlarge_profile_pic, container, false)
        val imageView = view.findViewById<ImageView>(R.id.dialogImageView)
        val imageUrl = arguments?.getString("imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("EnlargeProfilePicFragment", "Failed to load image", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("EnlargeProfilePicFragment", "Image loaded successfully")
                    return false
                }
            })
            .error(R.drawable.error_placeholder)
            .into(imageView)

        view.setOnClickListener { dismiss() }
        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.parseColor("#99000000")))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    companion object {
        fun newInstance(imageUrl: String): EnlargeProfilePicFragment {
            val fragment = EnlargeProfilePicFragment()
            val args = Bundle()
            args.putString("imageUrl", imageUrl)
            fragment.arguments = args
            return fragment
        }
    }
}

