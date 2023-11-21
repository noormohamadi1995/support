package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class OnlineAdminResponse(
    @SerializedName("status")
    val status : Boolean? = null,
    @SerializedName("message")
    val message : String? = null
)
