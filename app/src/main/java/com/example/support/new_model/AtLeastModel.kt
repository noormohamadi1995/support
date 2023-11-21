package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class AtLeastModel(
    @SerializedName("status") val status : String? = null,
    @SerializedName("error") val error : String? = null,
    @SerializedName("message") val message : String? = null,
    @SerializedName("setting") val setting : SettingModel? = null
)

data class SettingModel(
    @SerializedName("id") val id : Long? = null,
    @SerializedName("min_withdraw_amount") val minWithDrawAmount : String? = null,
    @SerializedName("created_at") val createdAt : String? = null,
    @SerializedName("updated_at") val updatedAt : String? = null
)