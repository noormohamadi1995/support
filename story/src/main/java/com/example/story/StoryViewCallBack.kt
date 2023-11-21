package com.example.story

import android.view.View

interface StoryViewCallBack{
    fun done()

    fun onNextCalled(view: View, storyView : StoryView, index: Int)
}
