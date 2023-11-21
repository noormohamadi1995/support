package com.example.support.Panel.ListPrice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import android.util.Log
import android.view.View
import com.example.support.Adapter_0.Adpater_Recyclerview_Price
import com.example.support.R
import com.example.support.new_api.ApiController
import com.example.support.new_api.Routes
import com.example.support.new_model.PriceListModel
import com.pnikosis.materialishprogress.ProgressWheel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Activity_Price : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    var adpater_recyclerview_price: Adpater_Recyclerview_Price? = null
    var progressWheel: ProgressWheel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity__price)
        progressWheel = findViewById<View>(R.id.progress_wheel) as ProgressWheel
        Setup_Recyclerview(progressWheel)

    }

    fun Setup_Recyclerview(progressWheel: ProgressWheel?) {
        recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView
        val routes = ApiController.client.create(Routes::class.java)
        val call = routes.getlist()
        call!!.enqueue(object : Callback<PriceListModel?> {
            override fun onResponse(
                call: Call<PriceListModel?>,
                response: Response<PriceListModel?>
            ) {
                progressWheel!!.visibility = View.GONE
                recyclerView!!.layoutManager = LinearLayoutManager(this@Activity_Price)
                val priceListModel = response.body()
                Log.i("cjmohammad", "Price model: " + Gson().toJson(priceListModel))
                adpater_recyclerview_price =
                    Adpater_Recyclerview_Price(this@Activity_Price, priceListModel!!.list)
                recyclerView!!.adapter = adpater_recyclerview_price
            }

            override fun onFailure(call: Call<PriceListModel?>, t: Throwable) {
                progressWheel!!.visibility = View.GONE
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()


    }
}