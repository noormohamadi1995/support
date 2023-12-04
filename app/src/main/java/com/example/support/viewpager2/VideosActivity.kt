package com.example.support.viewpager2

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.support.R
import com.example.support.databinding.ActivityViewpager2ViedoBinding
import com.example.support.new_api.ApiController
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_api.Routes
import com.example.support.new_model.AppSettingsModel
import com.example.support.new_model.GetVideoModel
import com.example.support.viewpager2.video.story.Util
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class VideosActivity : AppCompatActivity() {
    private val exoPlayerItems = ArrayList<ExoPlayerItem>()
    private lateinit var mBinding : ActivityViewpager2ViedoBinding

    companion object{
        const val CURRENT_POSITION = "position"
    }

    val listener by lazy {
        object : OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            override fun onOrientationChanged(orientation: Int) {
                val defaultPortrait = 0
                val upsideDownPortrait = 180
                val rightLandscape = 90
                val leftLandscape = 270

                when {
                    isWithinOrientationRange(orientation, defaultPortrait) -> {
                        setLandscapeOrientation(false)
                        updateList(false)
                    }
                    isWithinOrientationRange(orientation, leftLandscape) -> {
                        setLandscapeOrientation(true)
                        updateList(true)
                    }
                    isWithinOrientationRange(orientation, upsideDownPortrait) -> {
                        setLandscapeOrientation(false)
                        updateList(false)
                    }
                    isWithinOrientationRange(orientation, rightLandscape) -> {
                        setLandscapeOrientation(true)
                        updateList(true)
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_viewpager2_viedo)
        if (listener.canDetectOrientation()) {
            listener.enable();
        }
        mBinding.viewPager.adapter = VideoAdapter(
            onRate = {id,type->
                setReaction(id,type)
            },
            onShare = {title,desc,url->
                sendMessageWithImage(title,desc,url)
            },
            videoPreparedListener = object : OnMediaPlayerPreparedListener{
                override fun onMediaPlayerPrepared(exoPlayerItem: ExoPlayerItem) {
                    exoPlayerItems.add(exoPlayerItem)
                }

                override fun onMediaPlayerReleased(exoPlayerItem: ExoPlayerItem) {
                    for (i in exoPlayerItems.indices) {
                        val exo = exoPlayerItems[i]
                        if (exo.exoPlayer == exoPlayerItem.exoPlayer && exo.position == exoPlayerItem.position) {
                            exoPlayerItems.removeAt(i)
                            break
                        }
                    }
                }
            },
            fullScreen = {
                setLandscapeOrientation(it)
                updateList(it)
            }
        )

        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT

        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val token = sharedPreferences.getString("token", null)


        val routes = ApiController.client.create(Routes::class.java)
        val calls = routes.getSettings()
        calls.enqueue(object : Callback<AppSettingsModel?> {
            override fun onResponse(
                call: Call<AppSettingsModel?>,
                response: Response<AppSettingsModel?>
            ) {
                if(response.isSuccessful && response.code() == 200){
                    response.body()?.let {
                        val editor = sharedPreferences.edit()
                        editor.putString("app_dowload_url",it.link)
                        editor.apply()
                    }
                }
            }

            override fun onFailure(call: Call<AppSettingsModel?>, t: Throwable) {
                Log.e("VideoActivity",t.toString())
            }
        })

        val route = ApiPanelController.client.create(PanelRoutes::class.java)

        route.getVideos(token).enqueue(object : Callback<GetVideoModel> {
            override fun onResponse(
                call: Call<GetVideoModel>,
                response: Response<GetVideoModel>
            ) {
                if (response.isSuccessful && response.code() == 200){
                    response.body()?.videos?.sortedByDescending { it.sort }?.map {
                        VideoItem(
                            id = it.id ?: -1,
                            title = it.videoTitle ?: "",
                            description = it.videoDescription ?: "",
                            url = it.videoURL ?: "",
                            likes = it.likes ?: 0,
                            views = it.views ?: 0,
                            web = it.web ?: "",
                            infoId = it.videoInfo?.id ?: -1,
                            info = it.videoInfo,
                            infoLike = it.videoInfo?.like ?: 0,
                            infoView = it.videoInfo?.view ?: 0,
                            userId = it.videoInfo?.userId ?: -1,
                            type = Util.checkMimeType(it.videoURL ?: "")
                        )
                    }?.let {
                        (mBinding.viewPager.adapter as? VideoAdapter)?.submitList(
                            it
                        )
                        val position = intent?.extras?.getInt(CURRENT_POSITION)
                        mBinding.viewPager.setCurrentItem(position ?: 0,true)
                    }
                }
            }

            override fun onFailure(call: Call<GetVideoModel>, t: Throwable) {
                Log.e("Error",t.toString())
            }
        })
    }

    private fun setReaction(video_id: Int?, _type: String) {
        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val token = sharedPreferences.getString("token", null)
        val route = ApiPanelController.client.create(PanelRoutes::class.java)

        route.setReaction(token,_type,video_id).enqueue(object : Callback<VideoReactionModel> {
            override fun onResponse(
                call: Call<VideoReactionModel>,
                response: Response<VideoReactionModel>
            ) {
                Log.i("ssd_reaction", Gson().toJson(response.body()))
            }

            override fun onFailure(call: Call<VideoReactionModel>, t: Throwable) {
                Log.e("ssd_reaction", t.toString())
            }
        })
    }

    private fun sendMessageWithImage(
        title: String,
        description: String,
        videoURL: String
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"

        val nl = "\n"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            title + nl + nl + description + nl + nl + "لینک دنلود تصویر" + nl + nl + videoURL + nl + nl
        )
        startActivity(Intent.createChooser(shareIntent, "اشتراک ویدیو"))
    }

    private fun updateList(isFullScreen : Boolean) = mBinding.apply{
        val adapter = viewPager.adapter as VideoAdapter
        adapter.currentList.filterIsInstance<VideoItem>().forEach {
            it.isFullScreen.tryEmit(isFullScreen)
        }
    }

    private fun setLandscapeOrientation(isLandscape : Boolean){
        requestedOrientation = if (isLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    private fun isWithinOrientationRange(
        currentOrientation: Int, targetOrientation: Int, epsilon: Int = 10
    ): Boolean {
        return currentOrientation > targetOrientation - epsilon
                && currentOrientation < targetOrientation + epsilon
    }


    override fun onDestroy() {
        exoPlayerItems.forEach { item ->
            val player = item.exoPlayer
            player.stop()
            player.release()
        }
        listener.disable()
        super.onDestroy()
    }

    override fun onStop() {
        exoPlayerItems.forEach { item ->
            val player = item.exoPlayer
            player.stop()
            player.release()
        }
        listener.disable()
        super.onStop()
    }
}
