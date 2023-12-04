package com.example.support.viewpager2

import CircleCropWithBorder
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.example.support.R
import com.example.support.databinding.VideoContainerBinding
import com.example.support.viewpager2.video.story.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class VideoAdapter(
    private val onRate : (id : Int,type : String) -> Unit,
    private val onShare : (title : String,desc : String,url : String) -> Unit,
    private val videoPreparedListener: OnMediaPlayerPreparedListener,
    private val fullScreen : (Boolean) -> Unit
) : ListAdapter<VideoItem, VideoAdapter.VideoViewHolder>(
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

    override fun onViewAttachedToWindow(holder: VideoViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttach()
    }

    override fun onViewDetachedFromWindow(holder: VideoViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetach()
    }

    inner class VideoViewHolder(val binding : VideoContainerBinding) : RecyclerView.ViewHolder(binding.root) {
        private var hashMapLike: HashMap<Int, Boolean> = HashMap()
        private var exoPlayer: ExoPlayer? = null
        private var mVideo : VideoItem? = null
        private var fullScreenBtn: ImageView? = null
        private var job: Job? = null
        private var profile = ""
        init {
            val sharedPreferences = itemView.context.getSharedPreferences("Shtoken", 0)
            profile = sharedPreferences.getString("profile", "")!!
        }

        fun bind(item : VideoItem) {
            job?.cancel()
            job = CoroutineScope(Dispatchers.Main).launch {
                item.isFullScreen.collectLatest {
                    if (it) {
                        changeFullScreenButtonDrawable()
                    } else {
                        reloadFullScreenBtn()
                    }
                }
            }

            binding.video = item
            mVideo = item
            binding.imageView.visibility = View.GONE
            binding.playerView.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            fullScreenBtn = binding.playerView.findViewById(R.id.exo_fullscreen_icon)
            fullScreenBtn?.imageTintList = ColorStateList.valueOf(Color.WHITE)

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

            fullScreenBtn?.setOnClickListener {
                if (item.isFullScreen.value) {
                    item.isFullScreen.tryEmit(false)
                    fullScreen.invoke(false)
                    reloadFullScreenBtn()
                } else {
                    fullScreen.invoke(true)
                    item.isFullScreen.tryEmit(true)
                    changeFullScreenButtonDrawable()
                }
            }

            if (item.type == "image") {
                binding.imageView.visibility = View.VISIBLE
                binding.playerView.visibility = View.GONE

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

            } else if (item.type == "video") {
                binding.imageView.visibility = View.GONE
                binding.playerView.visibility = View.VISIBLE
            }
        }

        fun onDetach(){
            if (mVideo?.type == "video"){
                binding.relativeBottom.visibility = View.GONE
                binding.imgProfile.visibility = View.GONE
                exoPlayer?.let {
                    videoPreparedListener.onMediaPlayerReleased(
                        ExoPlayerItem(
                            it,
                            absoluteAdapterPosition
                        )
                    )
                }

                exoPlayer?.stop()
                exoPlayer?.release()
                exoPlayer = null
            }
        }

        fun onAttach(){
            if (mVideo?.isFullScreen?.value == true) {
                changeFullScreenButtonDrawable()
            } else {
                reloadFullScreenBtn()
            }
            mVideo?.takeIf { it.type == "video" }?.let {
                binding.relativeBottom.visibility = if (it.isFullScreen.value) View.GONE else View.VISIBLE
                binding.imgProfile.visibility = if (it.isFullScreen.value) View.GONE else View.VISIBLE
                exoPlayer = ExoPlayer.Builder(binding.root.context).build()
                exoPlayer?.let {
                    videoPreparedListener.onMediaPlayerPrepared(
                        ExoPlayerItem(
                            it,
                            absoluteAdapterPosition
                        )
                    )
                }

                exoPlayer?.addListener(object : Player.Listener {
                    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                        if (playbackState == Player.STATE_BUFFERING) {
                            binding.progressBar.visibility = View.VISIBLE
                        } else if (playbackState == Player.STATE_READY) {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                })
                binding.playerView.player = exoPlayer
                exoPlayer?.repeatMode = Player.REPEAT_MODE_ONE

                exoPlayer?.setMediaItem(MediaItem.fromUri(it.url))
                exoPlayer?.prepare()
                exoPlayer?.seekTo(0, 0)
                exoPlayer?.playWhenReady = true
            }
        }

        private fun reloadFullScreenBtn() {
            fullScreenBtn?.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.baseline_fullscreen_24
                )
            )
            binding.imgProfile.visibility = View.VISIBLE
            binding.relativeBottom.visibility = View.VISIBLE
        }

        private fun changeFullScreenButtonDrawable(){
            fullScreenBtn?.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    R.drawable.baseline_fullscreen_exit_24
                )
            )
            binding.relativeBottom.visibility = View.GONE
            binding.imgProfile.visibility = View.GONE
        }

    }
}

interface OnMediaPlayerPreparedListener {
    fun onMediaPlayerPrepared(exoPlayerItem: ExoPlayerItem)
    fun onMediaPlayerReleased(exoPlayerItem: ExoPlayerItem)
}
