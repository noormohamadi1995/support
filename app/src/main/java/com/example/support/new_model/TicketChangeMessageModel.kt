package com.example.support.new_model

import com.google.gson.annotations.SerializedName

class TicketChangeMessageModel {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("error")
    var error: String? = null

    @SerializedName("isChange")
    var is_change: Boolean? = null


}