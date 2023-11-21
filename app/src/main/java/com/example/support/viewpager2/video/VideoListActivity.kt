package com.example.support.viewpager2.video

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.example.support.R
import com.example.support.databinding.ActivityVideoListBinding
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_model.GetVideoModel
import com.example.support.new_model.ListStories
import com.example.support.viewpager2.VideosActivity
import com.example.support.viewpager2.video.story.ShowStoryActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class VideoListActivity : AppCompatActivity() {
    private var mBinding : ActivityVideoListBinding? = null
    private var dialog : android.app.ProgressDialog? = null
    private val sharedPreference : SharedPreferences by lazy {
        getSharedPreferences("Shtoken", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       mBinding = DataBindingUtil.setContentView(this,R.layout.activity_video_list)

        mBinding?.videoList?.layoutManager = GridLayoutManager(this, 4).also {
            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position % 5 == 2)
                      2
                    else
                       1
                }
            }
        }
        mBinding?.videoList?.addItemDecoration(SpaceItemDecoration(0))
        mBinding?.videoList?.adapter = ListVideoAdapter(
            onClickItem = {position->
                Intent(this,VideosActivity::class.java).also {
                    it.putExtra(VideosActivity.CURRENT_POSITION,position)
                    startActivity(it)
                }
            }
        )

        mBinding?.storyList?.layoutManager = LinearLayoutManager(this, HORIZONTAL,false)
        mBinding?.storyList?.adapter = StoryAdapter{item->
            Intent(this,ShowStoryActivity::class.java).also {
                it.putExtra(ShowStoryActivity.STORY_ID,item.id)
                it.putExtra(ShowStoryActivity.STORY_TITLE,item.title)
                it.putExtra(ShowStoryActivity.STORY_IMAGE,item.image)
                startActivity(it)
            }
        }

        dialog = android.app.ProgressDialog(this)
        dialog?.setCancelable(false)
        dialog?.setMessage("در حال بارگزاری اطلاعات...")
        getStories()
        getVideos()
    }

    private fun getVideos() = mBinding?.also {binding->
        dialog?.show()
        val token = sharedPreference.getString("token", null)

        val route = ApiPanelController.client.create(PanelRoutes::class.java)

        route.getVideos(token).enqueue(object : Callback<GetVideoModel> {
            override fun onResponse(
                call: Call<GetVideoModel>,
                response: Response<GetVideoModel>
            ) {
                if (response.isSuccessful && response.code() == 200){
                    response.body()?.videos?.sortedByDescending { it.sort }?.map {
                        VideoListItem(
                            id = it.id ?: -1,
                            title = it.videoTitle ?: "",
                            image = it.image ?: ""
                        )
                    }?.let {
                        (binding.videoList.adapter as? ListVideoAdapter)?.submitList(
                            it
                        )

                        if (dialog?.isShowing == true)
                            dialog?.dismiss()
                    }
                }
            }

            override fun onFailure(call: Call<GetVideoModel>, t: Throwable) {
                Log.e("Error",t.toString())
                if (dialog?.isShowing == true)
                    dialog?.dismiss()
            }
        })
    }

    private fun getStories() = mBinding?.also {binding->
        val routes = ApiPanelController.client.create(PanelRoutes::class.java)
        val token = sharedPreference.getString("token",null)
        routes.getStories(token).enqueue(object : Callback<ListStories>{
            override fun onResponse(call: Call<ListStories>, response: Response<ListStories>) {
                dialog?.dismiss()
                if (response.isSuccessful && response.code() == 200 && response.body()?.status == "ok"){
                    response.body()?.stories?.sortedByDescending { it.sort }?.map {
                        VideoListItem(
                            id = it.id ?: -1,
                            title = it.title ?: "",
                            image = it.storyGalleries.firstOrNull()?.imageUrl ?: ""
                        )
                    }?.let {
                        (binding.storyList.adapter as? StoryAdapter)?.submitList(
                            it
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ListStories>, t: Throwable) {
                Log.e("error story",t.toString())
            }

        })
    }
}