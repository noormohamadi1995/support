package com.example.support.viewpager2

import com.google.gson.annotations.SerializedName

data class VideoReactionModel (
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
)