package com.example.support.new_model

import com.google.gson.annotations.SerializedName

class PostsModel {
    @SerializedName("status")
    var status: String? = null

    @SerializedName("listpost")
    var listPosts: List<ListPost>? = null

    inner class ListPost {
        @SerializedName("id")
        var id: String? = null

        @SerializedName("idpost")
        var idpost: String? = null

        @SerializedName("title")
        var title: String? = null

        @SerializedName("image")
        var image: String? = null

        @SerializedName("content")
        var content: String? = null

        @SerializedName("date")
        var date: String? = null



        @SerializedName("text")
        var text: String? = null
    }
}