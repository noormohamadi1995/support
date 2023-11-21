package com.example.support.viewpager2

import com.example.support.new_model.VideoInfo

data class VideoItem(
    val id : Int,
    val title : String,
    val description : String,
    var likes : Int,
    val views : Int,
    val url : String,
    val info : VideoInfo? = null,
    val web : String,
    val infoId : Int,
    val userId : Int,
    val infoLike : Int,
    val infoView : Int,
)
