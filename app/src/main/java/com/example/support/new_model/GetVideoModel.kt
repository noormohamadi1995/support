package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class GetVideoModel(
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("videos")
    val videos: List<VideosData> = listOf()
)

    data class VideosData(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("video_url") val videoURL: String? = null,
        @SerializedName("videoTitle") val videoTitle: String? = null,
        @SerializedName("videoDescription") val videoDescription: String? = null,
        @SerializedName("likes") val likes: Int? = null,
        @SerializedName("views") val views: Int? = null,
        @SerializedName("created_at") val createdAt: String? = null,
        @SerializedName("updated_at") val updatedAt: String? = null,
        @SerializedName("video_info") val videoInfo: VideoInfo? = null,
        @SerializedName("web") val web: String? = null,
        @SerializedName("image_url") val image : String? = null,
        @SerializedName("sort") val sort : Int? = null
    )


    data class VideoInfo(
        @SerializedName("id") val id: Int? = null,
        @SerializedName("user_id") val userId: Int? = null,
        @SerializedName("video_id") val videoId: Int? = null,
        @SerializedName("like") val like: Int? = null,
        @SerializedName("view") val view: Int? = null,
        @SerializedName("created_at") val createdAt: String? = null,
        @SerializedName("updated_at") val updatedAt: String? = null
    )
