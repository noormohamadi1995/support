package com.example.support.viewpager2.video.story

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.support.databinding.StoryVideoLayoutBinding

class VideoStoryVideo(context : Context) : View(context) {
    private var videoDuration : VideoDuration? = null
    private val mBinding = StoryVideoLayoutBinding.inflate(LayoutInflater.from(context))
    fun playVideo(url : String){
        val uri = Uri.parse(url)

        mBinding.videoView.setVideoURI(uri)

        mBinding.videoView.requestFocus()
        mBinding.videoView.start()

        mBinding.videoView.setOnInfoListener(object : MediaPlayer.OnInfoListener {
            override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                   videoDuration?.currentDuration((mBinding.videoView.duration) / 1000)
                    return true
                }
                return false
            }
        })
    }

    fun setVideoDuration(videoDuration: VideoDuration){
        this.videoDuration = videoDuration
    }
}

interface VideoDuration{
    fun currentDuration(duration : Int)
}