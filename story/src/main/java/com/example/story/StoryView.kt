package com.example.story

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.VideoView
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.bolaware.viewstimerstory.ProgressTimeWatcher
import java.lang.Exception


class StoryView(
    context: Context,
    private var storyViewList: List<StoryViewDataModel>,
    private val passedInContainerView: ViewGroup,
    private var storyCallback: StoryViewCallBack,
    @DrawableRes private var mProgressDrawable: Int = R.drawable.green_lightgrey_drawable
) : ConstraintLayout(context) {
    private var currentlyShownIndex = 0
    private lateinit var currentView: View
    private var libSliderViewList = mutableListOf<MyProgressBar>()
    private lateinit var view: View
    private var pausedState : Boolean = false
    lateinit var gestureDetector: GestureDetector

    init {
        initView()
        init()
    }

    private fun init() {
        storyViewList.forEachIndexed { index, sliderView ->
            val myProgressBar = MyProgressBar(
                context,
                index,
                sliderView.durationInSeconds,
                object : ProgressTimeWatcher {
                    override fun onEnd(indexFinished: Int) {
                        currentlyShownIndex = indexFinished + 1
                        next()
                    }
                },
                mProgressDrawable)
            libSliderViewList.add(myProgressBar)
            view.findViewById<LinearLayout>(R.id.linearProgressIndicatorLay).addView(myProgressBar)
        }
        //start()
    }

    fun callPause(pause : Boolean){
        try {
            if(pause){
                if(!pausedState){
                    this.pausedState = !pausedState
                    pause(false)
                }
            } else {
                if(pausedState){
                    this.pausedState = !pausedState
                    resume()
                }
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    private fun initView() {
        view = View.inflate(context, R.layout.progress_story_view, this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        gestureDetector = GestureDetector(context, SingleTapConfirm())

        val touchListener = object  : OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (gestureDetector.onTouchEvent(event)) {
                    // single tap
                    if(v?.id == view.findViewById<FrameLayout>(R.id.rightLay).id){
                        next()
                    } else if(v?.id == view.findViewById<FrameLayout>(R.id.leftLay).id){
                        prev()
                    }
                    return true
                } else {
                    // your code for move and drag
                    return when(event.action){
                        MotionEvent.ACTION_DOWN -> {
                            callPause(true)
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            callPause(false)
                            true
                        }

                        else -> false
                    }
                }
            }
        }

//        view.leftLay.setOnClickListener { prev() }
//        view.rightLay.setOnClickListener { next() }
        view.findViewById<FrameLayout>(R.id.leftLay).setOnTouchListener(touchListener)
        view.findViewById<FrameLayout>(R.id.rightLay).setOnTouchListener(touchListener)
        //view.container.setOnTouchListener(touchListener)

        this.layoutParams = params
        passedInContainerView.addView(this)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun show() {
        view.findViewById<ProgressBar>(R.id.loaderProgressbar).visibility = View.GONE
        if (currentlyShownIndex != 0) {
            for (i in 0..Math.max(0, currentlyShownIndex - 1)) {
                libSliderViewList[i].progress = 100
                libSliderViewList[i].cancelProgress()
            }
        }

        if (currentlyShownIndex != libSliderViewList.size - 1) {
            for (i in currentlyShownIndex + 1..<libSliderViewList.size) {
                libSliderViewList[i].progress = 0
                libSliderViewList[i].cancelProgress()
            }
        }

        currentView = storyViewList[currentlyShownIndex].view

        libSliderViewList[currentlyShownIndex].startProgress()

        storyCallback.onNextCalled(currentView, this, currentlyShownIndex)

        view.findViewById<LinearLayout>(R.id.currentlyDisplayedView).removeAllViews()
        view.findViewById<LinearLayout>(R.id.currentlyDisplayedView).addView(currentView)
        val params = LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT, 1f
        )
        //params.gravity = Gravity.CENTER_VERTICAL
        if(currentView is ImageView) {
            (currentView as ImageView).scaleType = ImageView.ScaleType.FIT_CENTER
            (currentView as ImageView).adjustViewBounds = true
        }
        currentView.layoutParams = params
    }

    fun start() {
//            Handler().postDelayed({
//                show()
//            }, 2000)
        show()
    }

    fun editDurationAndResume(index: Int, newDurationInSecons : Int){
        view.findViewById<ProgressBar>(R.id.loaderProgressbar).visibility = View.GONE
        libSliderViewList[index].editDurationAndResume(newDurationInSecons)
    }

    fun pause(withLoader : Boolean) {
        if(withLoader){
            view.findViewById<ProgressBar>(R.id.loaderProgressbar).visibility = View.VISIBLE
        }
        libSliderViewList[currentlyShownIndex].pauseProgress()
        if(storyViewList[currentlyShownIndex].view is VideoView){
            (storyViewList[currentlyShownIndex].view as VideoView).pause()
        }
    }

    fun resume() {
        view.findViewById<ProgressBar>(R.id.loaderProgressbar).visibility = View.GONE
        libSliderViewList[currentlyShownIndex].resumeProgress()
        if(storyViewList[currentlyShownIndex].view is VideoView){
            (storyViewList[currentlyShownIndex].view as VideoView).start()
        }
    }

    private fun stop() {

    }

    fun next() {
        try {
            if (currentView == storyViewList[currentlyShownIndex].view) {
                currentlyShownIndex++
                if (storyViewList.size <= currentlyShownIndex) {
                    finish()
                    return
                }
            }
            show()
        } catch (e: IndexOutOfBoundsException) {
            finish()
        }
    }

    private fun finish() {
        storyCallback.done()
        for (progressBar in libSliderViewList) {
            progressBar.cancelProgress()
            progressBar.progress = 100
        }
    }

    fun prev() {
        try {
            if (currentView == storyViewList[currentlyShownIndex].view) {
                currentlyShownIndex--
                if (0 > currentlyShownIndex) {
                    currentlyShownIndex = 0
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            currentlyShownIndex -= 2
        } finally {
            show()
        }
    }

    private inner class SingleTapConfirm : SimpleOnGestureListener() {

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            return true
        }
    }


}