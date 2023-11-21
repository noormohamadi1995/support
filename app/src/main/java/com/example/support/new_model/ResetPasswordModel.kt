package com.v2ray.rocket.new_model

import com.google.gson.annotations.SerializedName

class ResetPasswordModel {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("error")
    var error: String? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("famili")
    var famili: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("change_password")
    var change_password: Boolean? = null

    @SerializedName("token")
    var token: String? = null


    @SerializedName("profile")
    var profile: String? = null

    @SerializedName("referral_code")
    var referral_code: String? = null
}