package com.example.support.new_model

import com.google.gson.annotations.SerializedName

class ChangeProfileModel {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("error")
    var error: String? = null

    @SerializedName("message")
    var message: String? = null

}