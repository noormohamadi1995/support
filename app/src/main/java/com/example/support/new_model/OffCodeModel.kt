package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class OffCodeModel(
    @SerializedName("status") var status: String? = null,
    @SerializedName("error") var error: String? = null,
    @SerializedName("offCode") var offCOde: String? = null,
    @SerializedName("offCode_Amount") var priceAfterOff: Int? = null,
    @SerializedName("amount") val amount : String? = null,
    @SerializedName("message") val message : String? = null,
    @SerializedName("eshtrak_amount") val eshtrakAmount : String? = null,
    @SerializedName("pay_url") val payUrl : String? = null

)