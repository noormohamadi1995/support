package com.example.support.storyView


interface StoryViewCallBack{
    fun done()

    fun setReaction(id : Int,type : String,storyView: StoryView)
}
