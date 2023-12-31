package com.example.support.storyView

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.example.support.R

class MyProgressBar(
    context: Context,
    private var index: Int,
    var durationInSeconds: Int,
    private val timeWatcher: ProgressTimeWatcher,
     val storyDataModel: StoryViewDataModel,
    @DrawableRes private var mProgressDrawable: Int = R.drawable.green_lightgrey_drawable
) : ProgressBar(
    context,
    null,
    0,
    android.R.style.Widget_ProgressBar_Horizontal
) {
    private var objectAnimator = ObjectAnimator.ofInt(this, "progress", this.progress, 100)
    private var hasStarted: Boolean = false

    init {
        initView()
    }

    private fun initView() {

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        )

        params.marginEnd = 5f.toPixel(context)

        //val progressBar = MyProgressBar(context, durationInSeconds * 1000)
        // textView.text = label.trim()

        this.max = 100

        this.progress = 0

        this.layoutParams = params

        this.progressDrawable = ContextCompat.getDrawable(context, mProgressDrawable)

    }

    fun startProgress() {
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                timeWatcher.onEnd(index)
            }

            override fun onAnimationCancel(animation: Animator) {
                animation.apply { removeAllListeners() }
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })
        objectAnimator.apply {
            duration = (durationInSeconds * 1000).toLong()
            start()
        }

        hasStarted = true
    }

    fun cancelProgress() {
        objectAnimator.apply {
            cancel()
        }
    }

    fun pauseProgress() {
        objectAnimator.apply {
            pause()
        }
    }

    fun resumeProgress() {
        if (hasStarted) {
            objectAnimator.apply {
                resume()
            }
        }
    }

    fun editDurationAndResume(newDurationInSecons: Int){
        this.durationInSeconds = newDurationInSecons
        cancelProgress()
        startProgress()
    }
}