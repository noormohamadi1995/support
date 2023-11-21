package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class LoginModel(
    @SerializedName("status")
    var status: String? = null,

    @SerializedName("error")
    var error: String? = null,

    @SerializedName("token")
    var token: String? = null,

    @SerializedName("name")
    var name: String? = null,

    @SerializedName("famili")
    var famili: String? = null,

    @SerializedName("profile")
    var profile: String? = null,

    @SerializedName("referral_code")
    var referral_code: String? = null,

    @SerializedName("is_active")
    val isActive : Boolean? = null,

    @SerializedName("mobile") val mobile : String? = null,

    @SerializedName("username") val username : String? = null
)