package com.example.support.viewpager2.video.story

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.support.R
import com.example.support.databinding.ActivityShowStoryBinding
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_model.GetStory
import com.example.support.new_model.StoryGallery
import com.example.support.new_model.StoryReaction
import com.example.support.storyView.StoryDataModel
import com.example.support.storyView.StoryView
import com.example.support.storyView.StoryViewCallBack
import com.example.support.storyView.StoryViewDataModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ShowStoryActivity : AppCompatActivity(), StoryViewCallBack {
    private lateinit var mBinding: ActivityShowStoryBinding
    private val sharedPreferences : SharedPreferences by lazy {
        getSharedPreferences("Shtoken", MODE_PRIVATE)
    }
    private var dialog : android.app.ProgressDialog? = null

    companion object {
        const val STORY_ID = "id"
        const val STORY_TITLE = "title"
        const val STORY_IMAGE = "image"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_show_story)
        dialog = android.app.ProgressDialog(this)
        dialog?.setCancelable(false)
        dialog?.setMessage("در حال بارگزاری اطلاعات...")
        listObjectsStory()
    }

    private fun listObjectsStory() {
        intent?.extras?.let {
            val id = it.getInt(STORY_ID)
            val title = it.getString(STORY_TITLE)
            val image = it.getString(STORY_IMAGE)
            val token = sharedPreferences.getString("token",null)
            dialog?.show()
            val routes = ApiPanelController.client.create(PanelRoutes::class.java)
            routes.showStory(
                token = token,
                id = id
            ).enqueue(object : Callback<GetStory>{
                override fun onResponse(call: Call<GetStory>, response: Response<GetStory>) {
                    if (response.isSuccessful && response.code() == 200 && response.body()?.status == "ok") {
                        response.body()?.story?.storyGalleries?.sortedByDescending { it.id}?.map {
                            if (Util.checkMimeType(it.storyUrl ?: "") == "image") {
                                StoryViewDataModel(
                                    id = it.id ?: -1,
                                    view = ImageView(this@ShowStoryActivity),
                                    durationInSeconds = 10,
                                    url = it.storyUrl ?: "",
                                    viewsCount = it.views ?: 0,
                                    likes = it.likes ?: 0,
                                    web = it.web ?: "",
                                    isLike = it.isLike,
                                    isView = it.isView

                                )
                            } else {
                                StoryViewDataModel(
                                    id = it.id ?: -1,
                                    view = VideoView(this@ShowStoryActivity),
                                    durationInSeconds = 60,
                                    url = it.storyUrl ?: "",
                                    viewsCount = it.views ?: 0,
                                    likes = it.likes ?: 0,
                                    web = it.web ?: "",
                                    isLike = it.isLike,
                                    isView = it.isView
                                )

                            }
                        }?.let {
                            StoryDataModel(
                                title = title ?: "",
                                img = image ?: "",
                                items = it
                            )
                        }?.let {
                            if (it.items.isNotEmpty()){
                                StoryView(
                                    this@ShowStoryActivity,
                                    storyDataModel = it,
                                    mBinding.container,
                                    this@ShowStoryActivity,
                                    storyCloseCallBack = { finish() },
                                    showMore = {
                                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                                        startActivity(i)
                                    }
                                ).start()
                            }else{
                                Toast.makeText(this@ShowStoryActivity,"",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    if (dialog?.isShowing == true)
                        dialog?.dismiss()

                }

                override fun onFailure(call: Call<GetStory>, t: Throwable) {
                    Log.e("error",t.toString())
                    if (dialog?.isShowing == true)
                        dialog?.dismiss()
                }

            })
        }
    }

    override fun done() {
        finish()
    }

    override fun setReaction(id: Int, type: String,storyView: StoryView) {
        val route = ApiPanelController.client.create(PanelRoutes::class.java)
        val token = sharedPreferences.getString("token",null)

        route.setStoryReaction(
            token = token,
            galleryId = id,
            type = type
        ).enqueue(object : Callback<StoryReaction>{

            override fun onResponse(call: Call<StoryReaction>, response: Response<StoryReaction>) {
                if (response.isSuccessful && response.code() == 200 &&
                        response.body()?.status == "ok" && type == "like"){
                    Toast.makeText(this@ShowStoryActivity,response.body()?.message ?: "",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StoryReaction>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }

}