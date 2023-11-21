package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class CheckReferralCodeResponse(
    @SerializedName("status") val status : String? = null,
    @SerializedName("error") val error : String? = null,
    @SerializedName("message") val message : String? = null,
    @SerializedName("referralCode") val referralCode : String? = null,
    @SerializedName("referralCode_Amount") val amount : Long? = null,
    @SerializedName("description") val description : String? = null,
    @SerializedName("pay_url") val payUrl : String? = null,
    @SerializedName("eshtrak_amount") val eshtrakAmount : String? = null,
)
