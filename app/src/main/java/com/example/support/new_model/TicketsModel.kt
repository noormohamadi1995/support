package com.example.support.new_model

import com.google.gson.annotations.SerializedName

data class TicketsModel (
    @SerializedName("status")
    var status: String? = null,

    @SerializedName("error")
    var error: String? = null,

    @SerializedName("tickets")
    var tickets: List<TicketData> = listOf()
)

    data class TicketData(
        @SerializedName("id")
        var id: Int? = null,

        @SerializedName("title")
        var title: String? = null,

        @SerializedName("status")
        var status: String? = null,

        @SerializedName("unread_count")
        var unread_count: Int? = null,

        @SerializedName("created")
        var created_at: String? = null,
    )
