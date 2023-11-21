package com.example.support.new_model

import com.google.gson.annotations.SerializedName


data class TicketMessagesModel(
    @SerializedName("status")
    var status: String? = null,

    @SerializedName("error")
    var error: String? = null,

    @SerializedName("ticket_messages")
    var ticket_messages: List<TicketMessageData> =  ArrayList()
)

data class TicketMessageData(
        @SerializedName("id")
        var id: Int? = null,
        @SerializedName("comment")
        var comment: String? = null,

      @SerializedName("image")
        var image: String? = null,

        @SerializedName("is_admin")
        var is_admin: Int? = null,

        @SerializedName("ticketId")
        var ticket_id: Int? = null,

        @SerializedName("created")
        var created_at: String? = null,

       @SerializedName("is_visited")
        val isVisited : Int? = null

    )