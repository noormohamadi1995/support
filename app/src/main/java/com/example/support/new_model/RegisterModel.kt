package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class RegisterModel (
    @SerializedName("status")
    var status: String? = null,

    @SerializedName("error")
    var error: String? = null,

    @SerializedName("token")
    var token: String? = null,

    @SerializedName("name")
    var name: String? = null,

    @SerializedName("family")
    var family: String? = null,

    @SerializedName("username")
    var username: String? = null,

    @SerializedName("referral_code")
    var referral_code: String? = null,

    @SerializedName("is_active")
    val isActive : Boolean? = null

//    @SerializedName("wallet_amount")
//    var walletAmount : Long? = null
)