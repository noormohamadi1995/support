package com.example.support.new_model

import com.google.gson.annotations.SerializedName

class PriceListModel {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("list")
    var list: List<ListPrice>? = ArrayList()

    inner class ListPrice {
        @SerializedName("idvip")
        var idvip: String? = null

        @SerializedName("name")
        var name: String? = null

        @SerializedName("Imageurl")
        var Imageurl : String? = null

        @SerializedName("price")
        var price: String? = null

        @SerializedName("days")
        var days = 0




    }
}