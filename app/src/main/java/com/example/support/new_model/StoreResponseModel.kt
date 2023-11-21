package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class StoreResponseModel(
    @SerializedName("status") val status: String? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null
)
