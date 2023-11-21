package com.example.support.storyView

import android.view.View

data class StoryDataModel(
    val title : String,
    val img : String,
    val items : List<StoryViewDataModel> = listOf()
)
data class StoryViewDataModel(
    val id : Int,
    var viewsCount : Int,
    var likes : Int,
    val view: View,
    val durationInSeconds: Int,
    val url : String,
    val web : String,
    var isLike : Boolean? = null,
    var isView : Boolean? = null
    )