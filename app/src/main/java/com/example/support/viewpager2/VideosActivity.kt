package com.example.support.viewpager2

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.support.R
import com.example.support.new_api.ApiController
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_api.Routes
import com.example.support.new_model.AppSettingsModel
import com.example.support.new_model.GetVideoModel
import com.example.support.new_model.VideosData
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class VideosActivity : AppCompatActivity() {
    private var viewPager : ViewPager2? = null
    companion object{
        const val CURRENT_POSITION = "position"
    }
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager2_viedo)
        viewPager = findViewById(R.id.viewPager)

        viewPager?.adapter = VideoAdapter(
            onRate = {id,type->
                setReaction(id,type)
            },
            onShare = {title,desc,url->
                sendMessageWithImage(title,desc,url)
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
                    response.body()?.videos?.map {
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
                            userId = it.videoInfo?.userId ?: -1
                        )
                    }?.let {
                        Log.e("list",it.toString())
                        (viewPager?.adapter as? VideoAdapter)?.submitList(
                            it
                        )
                        val position = intent?.extras?.getInt(CURRENT_POSITION)
                        viewPager?.setCurrentItem(position ?: 0,true)
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

}
