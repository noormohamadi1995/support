package com.example.support.new_model

import com.google.gson.annotations.SerializedName

class UploadImageModel {

    @SerializedName("status")
    var status: String? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("uploaded_image")
    var uploaded_image: String? = null

}