package com.example.support.storyView

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.support.R
import com.example.support.viewpager2.video.story.Util


class StoryView(
    context: Context,
    private val storyDataModel: StoryDataModel,
    private val passedInContainerView: ViewGroup,
    private var storyCallback: StoryViewCallBack,
    private val storyCloseCallBack: () -> Unit,
    private val showMore: (String) -> Unit,
    @DrawableRes private var mProgressDrawable: Int = R.drawable.green_lightgrey_drawable
) : ConstraintLayout(context) {
    private var currentlyShownIndex = 0
    private lateinit var currentView: View
    private var libSliderViewList = mutableListOf<MyProgressBar>()
    private lateinit var view: View
    private var pausedState: Boolean = false
    lateinit var gestureDetector: GestureDetector

    init {
        initView()
        init()
    }

    private fun init() {
        view.findViewById<TextView>(R.id.storyTitle).text = storyDataModel.title
        Glide.with(context)
            .load(storyDataModel.img)
            .circleCrop()
            .into(view.findViewById(R.id.storyImg))

        storyDataModel.items.forEachIndexed { index, sliderView ->
            val myProgressBar = MyProgressBar(
                context,
                index,
                sliderView.durationInSeconds,
                object : ProgressTimeWatcher {
                    override fun onEnd(indexFinished: Int) {
                        Log.e("index", indexFinished.toString())
                        //currentlyShownIndex = indexFinished + 1
                        next()
                    }
                },
                storyDataModel = storyDataModel.items[index],
                mProgressDrawable
            )
            libSliderViewList.add(myProgressBar)
            view.findViewById<LinearLayout>(R.id.linearProgressIndicatorLay).addView(myProgressBar)
        }
        //start()
    }

    fun callPause(pause: Boolean) {
        try {
            if (pause) {
                if (!pausedState) {
                    this.pausedState = !pausedState
                    pause(false)
                }
            } else {
                if (pausedState) {
                    this.pausedState = !pausedState
                    resume()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        view = View.inflate(context, R.layout.progress_story_view, this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        gestureDetector = GestureDetector(context, SingleTapConfirm())

        val touchListener = object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (gestureDetector.onTouchEvent(event)) {
                    // single tap
                    if (v?.id == view.findViewById<FrameLayout>(R.id.rightLay).id) {
                        next()
                    } else if (v?.id == view.findViewById<FrameLayout>(R.id.leftLay).id) {
                        prev()
                    }
                    return true
                } else {
                    // your code for move and drag
                    return when (event.action) {
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
        view.findViewById<ImageView>(R.id.btnClose).setOnClickListener {
            storyCloseCallBack.invoke()
        }
        //view.container.setOnTouchListener(touchListener)

        this.layoutParams = params
        passedInContainerView.addView(this)
    }

    private fun showLikeCount() {
        val item = libSliderViewList[currentlyShownIndex].storyDataModel
        view.findViewById<TextView>(R.id.txtLikes).text = Util.formatNumber(item.likes)
        val btnLike = view.findViewById<ImageView>(R.id.btnLike)

        view.findViewById<ImageView>(R.id.btnLike).setImageDrawable(
            ContextCompat.getDrawable(
                btnLike.context,
                if (item.isLike?.not() == true) {
                    R.drawable.ic_baseline_favorite_border_24
                } else R.drawable.ic_baseline_favorite_24
            )
        )
    }

    private fun changeLikeCount(id: Int) {
        val item = libSliderViewList[currentlyShownIndex].storyDataModel
        if (item.id == id) {
            storyCallback.setReaction(id, "like", this)

            val txtLike = view.findViewById<TextView>(R.id.txtLikes)
            val btnLike = view.findViewById<ImageView>(R.id.btnLike)

            if (item.isLike == true) {
                item.likes = item.likes - 1
            } else {
                item.likes = item.likes + 1
            }

            btnLike.setImageDrawable(
                ContextCompat.getDrawable(
                    btnLike.context,
                    if (item.isLike?.not() == true) R.drawable.ic_baseline_favorite_24
                    else R.drawable.ic_baseline_favorite_border_24
                )
            )
            txtLike.text = Util.formatNumber((item.likes))
            item.isLike = item.isLike?.not()
        }

    }

    private fun changeViewCount() {
        val item = libSliderViewList[currentlyShownIndex].storyDataModel
        val txtCount = view.findViewById<TextView>(R.id.txtViews)
        if (item.isView?.not() == true) {
            item.viewsCount = item.viewsCount + 1
            item.isView = true
            txtCount.text = Util.formatNumber(item.viewsCount)
            storyCallback.setReaction(item.id, "view", this)
        } else {
            txtCount.text = Util.formatNumber(item.viewsCount)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun show() {
        val item = libSliderViewList[currentlyShownIndex].storyDataModel
        showLikeCount()
        changeViewCount()

        if (item.web.isNotEmpty()) {
            view.findViewById<Button>(R.id.btnShowMore).visibility = View.VISIBLE
        } else view.findViewById<Button>(R.id.btnShowMore).visibility = View.GONE

        view.findViewById<ProgressBar>(R.id.loaderProgressbar).visibility = View.GONE
        if (currentlyShownIndex != 0) {
            for (i in 0..0.coerceAtLeast(currentlyShownIndex - 1)) {
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

        currentView = storyDataModel.items[currentlyShownIndex].view

        libSliderViewList[currentlyShownIndex].startProgress()

        pause(true)
        val url = item.url
        if (currentView is ImageView) {
            view.findViewById<ImageView>(R.id.ivImage).visibility = View.VISIBLE
            view.findViewById<VideoView>(R.id.videoView).visibility = View.GONE
            Glide.with(currentView.context)
                .load(url)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resume()
                        return false
                    }

                })
                .into(view.findViewById(R.id.ivImage))
        } else if (currentView is VideoView) {
            view.findViewById<ImageView>(R.id.ivImage).visibility = View.GONE
            view.findViewById<VideoView>(R.id.videoView).visibility = View.VISIBLE
            val videoView = view.findViewById<VideoView>(R.id.videoView)
            val uri = Uri.parse(url)

            videoView.setVideoURI(uri)

            videoView.requestFocus()
            videoView.start()

            videoView.setOnInfoListener(object : MediaPlayer.OnInfoListener {
                override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        editDurationAndResume(currentlyShownIndex, (videoView.duration) / 1000)
                        return true
                    }
                    return false
                }
            })
        }

        view.findViewById<ImageView>(R.id.btnLike).setOnClickListener {
            changeLikeCount(item.id)
        }

        view.findViewById<Button>(R.id.btnShowMore).setOnClickListener {
            showMore.invoke(item.web)
        }
    }

    fun start() {
        show()
    }

    fun editDurationAndResume(index: Int, newDurationInSecons: Int) {
        view.findViewById<ProgressBar>(R.id.loaderProgressbar).visibility = View.GONE
        libSliderViewList[index].editDurationAndResume(newDurationInSecons)
    }

    private fun pause(withLoader: Boolean) {
        if (withLoader) {
            view.findViewById<ProgressBar>(R.id.loaderProgressbar).visibility = View.VISIBLE
        }
        libSliderViewList[currentlyShownIndex].pauseProgress()
        if (storyDataModel.items[currentlyShownIndex].view is VideoView) {
            (storyDataModel.items[currentlyShownIndex].view as VideoView).pause()
        }
    }

    fun resume() {
        view.findViewById<ProgressBar>(R.id.loaderProgressbar).visibility = View.GONE
        libSliderViewList[currentlyShownIndex].resumeProgress()
        if (storyDataModel.items[currentlyShownIndex].view is VideoView) {
            (storyDataModel.items[currentlyShownIndex].view as VideoView).start()
        }
    }

    fun next() {
        try {
            if (currentView == storyDataModel.items[currentlyShownIndex].view) {
                currentlyShownIndex++
                if (storyDataModel.items.size <= currentlyShownIndex) {
                    finish()
                    return
                } else {
                    show()
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
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
            if (currentView == storyDataModel.items[currentlyShownIndex].view) {
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