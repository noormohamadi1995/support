package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class WalletAmountResponseModel(
     @SerializedName("status") val status : String? = null,
     @SerializedName("message") val message : String? = null,
     @SerializedName("wallet_amount")val walletAmount : String? = null
)
