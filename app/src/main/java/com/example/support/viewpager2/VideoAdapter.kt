package com.example.support.viewpager2

import CircleCropWithBorder
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.example.support.R
import com.example.support.databinding.VideoContainerBinding
import com.example.support.viewpager2.video.story.Util


class VideoAdapter(
    private val onRate : (id : Int,type : String) -> Unit,
    private val onShare : (title : String,desc : String,url : String) -> Unit
) : androidx.recyclerview.widget.ListAdapter<VideoItem,VideoAdapter.VideoViewHolder>(
    object : DiffUtil.ItemCallback<VideoItem>(){
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem) = oldItem == newItem

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem) = oldItem == newItem
    }
){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            VideoContainerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoViewHolder(val binding : VideoContainerBinding) : RecyclerView.ViewHolder(binding.root) {
        var hashMapLike: HashMap<Int, Boolean> = HashMap()
        var profile = ""
        init {
            val sharedPreferences = itemView.context.getSharedPreferences("Shtoken", 0)
            profile = sharedPreferences.getString("profile", "")!!
        }

        private fun detectMediaType(url: String): String {
            return when (url.substringAfterLast(".", "")) {
                in listOf("mp4", "avi", "mov", "mkv", "wmv") -> {
                    "video"
                }
                in listOf("jpg", "jpeg", "png", "gif", "bmp") -> {
                    "image"
                }
                else -> {
                    "unknown"
                }
            }
        }

        fun bind(item : VideoItem) {
            Log.e("item",item.id.toString() + " " + item.web)
            binding.video = item
            binding.txtViews.text = Util.formatNumber(item.views)
            binding.txtLikes.text = Util.formatNumber(item.likes)
            Glide.with(itemView.context)
                .load(profile)
                .placeholder(R.drawable.we)
                .error(R.drawable.we)
                .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY))
                .into(binding.imgProfile)

            if (item.info?.like != null) {
                val isLike = item.info.like == 1
                if (isLike)
                    Glide.with(itemView.context).load(R.drawable.ic_baseline_favorite_24).into(binding.imgLike)
                else
                    Glide.with(itemView.context).load(R.drawable.ic_baseline_favorite_border_24)
                        .into(binding.imgLike)
                if (!hashMapLike.containsKey(item.id))
                    hashMapLike[item.id] =  item.info.like == 1

            } else {
                if (!hashMapLike.containsKey(item.id))
                    hashMapLike[item.id] = false
            }

            if (item.info != null) {
                if (item.info.view != null && item.info.view == 0) {
                    onRate.invoke(item.id,"view")
                    binding.txtViews.text = Util.formatNumber((item.views + 1))
                }
            } else {
                onRate.invoke(item.id,"view")
                binding.txtViews.text = Util.formatNumber((item.views + 1))
            }

            if (item.web.isNotEmpty()){
                binding.BtnWeb.visibility = View.VISIBLE
            }else
                binding.BtnWeb.visibility = View.GONE

            binding.imgShare.setOnClickListener {
                onShare.invoke(item.title,item.description,item.url)
            }

            binding.imgLike.setOnClickListener {
                val is_like = hashMapLike[item.id]

                if (is_like == true) {
                    Glide.with(itemView.context).load(R.drawable.ic_baseline_favorite_border_24)
                        .into(binding.imgLike)

                    binding.txtLikes.text = Util.formatNumber((item.likes - 1))

                    item.likes = item.likes - 1

                    hashMapLike[item.id] = false

                    onRate.invoke(item.id,"like")
                } else {
                    Glide.with(itemView.context).load(R.drawable.ic_baseline_favorite_24)
                        .into(binding.imgLike)

                    binding.txtLikes.text = Util.formatNumber(item.likes + 1)

                    item.likes = item.likes + 1

                    hashMapLike[item.id] = true
                    onRate.invoke(item.id,"like")
                }
            }

            binding.BtnWeb.setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(item.web))
                itemView.context.startActivity(i)
            }

            if (detectMediaType(item.url) == "image") {
                binding.imageView.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE

                Glide.with(itemView.context)
                    .load(item.url)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            // Handle the error case when the image loading fails
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressBar.visibility = View.GONE
                            return false
                        }
                    }).into(binding.imageView)

            } else if (detectMediaType(item.url) == "video") {
                binding.imageView.visibility = View.GONE
                binding.videoView.visibility = View.VISIBLE

                binding.videoView.setVideoPath(item.url)
                binding.videoView.setOnPreparedListener { mediaPlayer ->
                    binding.progressBar.visibility = View.GONE
                    mediaPlayer.start()

                    val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
                    val screenRatio = binding.videoView.width / binding.videoView.height.toFloat()
                    val scaleX = videoRatio / screenRatio
                    if (scaleX >= 50f) {
                        binding.videoView.scaleX = scaleX
                    } else {
                        binding.videoView.scaleY = 1f / scaleX
                    }
                }

                binding.videoView.setOnTouchListener { view, motionEvent ->
                    if (binding.videoView.isPlaying) {
                        binding.videoView.pause()
                        false
                    } else {
                        binding.videoView.start()
                        false
                    }
                }

            }
        }
    }

}