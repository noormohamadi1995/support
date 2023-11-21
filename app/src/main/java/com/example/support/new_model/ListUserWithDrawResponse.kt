package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class ListUserWithDrawResponse(
    @SerializedName("status") val status : String? = null,
    @SerializedName("error") val error : String? = null,
    @SerializedName("message") val message : String? = null,
    @SerializedName("userwithdraw") val userWithDraws : List<UserWithDraw> = listOf()
)

data class UserWithDraw(
    @SerializedName("id") val id : Long? = null,
    @SerializedName("user_id") val userId : Long? = null,
    @SerializedName("amount") val amount : String? = null,
    @SerializedName("card_number") val cardNumber : String? = null,
    @SerializedName("card_owener") val owener : String? = null,
    @SerializedName("sheba_number") val shebaNumber : String? = null,
    @SerializedName("description") val description : String? = null,
    @SerializedName("status") val status : String? = null,
    @SerializedName("created_at") val createdAt : String? = null,
    @SerializedName("updated_at") val updatedAt : String? = null
)
