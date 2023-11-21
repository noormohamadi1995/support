package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class CheckUserModel (
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("diff_day")
    val diff_day: String? = null,

    @SerializedName("diff_hour")
    val diff_hour: String? = null,

    @SerializedName("diff_minute")
    val diff_minute: String? = null,

    @SerializedName("diff_second")
    val diff_second: String? = null,
//
//    @SerializedName("accounts")
//    var Accounts: ArrayList<CheckUserAccountModel>? = null

    @SerializedName("comment")
    val comment: String? = null,

    @SerializedName("referral_code")
    val referral_code: String? = null
)