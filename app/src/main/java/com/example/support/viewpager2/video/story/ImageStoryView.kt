package com.example.support.viewpager2.video.story

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.support.databinding.StoryImageLayoutBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso

class ImageStoryView(
    private val context : Context
) : View(
    context,
    null,
    0
) {
    private val mBinding = StoryImageLayoutBinding.inflate(LayoutInflater.from(context))
    private var imageLoadStatus : ImageLoadStatus? = null

     fun loadImageUrl( url : String) {
         Log.e("imageUrl",url)
         Picasso.get()
             .load("https://i.pinimg.com/564x/14/90/af/1490afa115fe062b12925c594d93a96c.jpg")
             .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
             .into(mBinding.image, object : Callback {
                 override fun onSuccess() {
                    imageLoadStatus?.onSuccess()
                 }

                 override fun onError(e: Exception?) {
                     imageLoadStatus?.onError(e)
                 }
             })
    }

     fun loadImageRes(@DrawableRes imgRes : Int) {
        Glide.with(context)
            .load(imgRes)
            .into(mBinding.image)
    }

    fun onSuccessImageLod(imageLoadStatus: ImageLoadStatus){
        this.imageLoadStatus = imageLoadStatus
    }
}

interface ImageLoadStatus{
    fun onSuccess()
    fun onError(e : Exception?)
}