package com.example.support.ticket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.support.R
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_model.OnlineAdminResponse
import com.example.support.new_model.TicketChangeModel
import com.example.support.new_model.TicketsModel
import com.google.gson.Gson
import com.pnikosis.materialishprogress.ProgressWheel

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainTicketActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null
    var adapter: TicketAdapter? = null
    var progressWheel: ProgressWheel? = null
    var relativeLayout: RelativeLayout? = null

    var scrollPosition = 0
    private val handler = Handler(Looper.getMainLooper())

    var linearLayout: LinearLayoutManager? = null

    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (isChange){
                checkAdminOnline(true)
                checkChange(1)
            }else{
                checkAdminOnline()
                checkChange(0)
            }
            Log.i("run_ticket", "refreshRunnable")
            handler.postDelayed(this, 8000)
        }
    }


    override fun onPause() {
        super.onPause()
        Log.i("run_ticket", "onPause")
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ticket)

        progressWheel = findViewById<View>(R.id.progress_wheel) as ProgressWheel

        relativeLayout = findViewById<View>(R.id.relative_add_ticket) as RelativeLayout?

        recyclerView = findViewById<View>(R.id.recycler_view_ticket) as RecyclerView

        relativeLayout?.setOnClickListener {
            val intent = Intent(this, MainTicketAddActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        checkAdminOnline()
        handler.postDelayed(refreshRunnable, 8000)
    }

    var isChange = false


    fun checkChange(_is_refresh: Int) {
        val routes = ApiPanelController.client.create(PanelRoutes::class.java)

        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val toekn = sharedPreferences.getString("token", null)

        val call = routes.checkTicket(toekn, _is_refresh)

        call?.enqueue(object : Callback<TicketChangeModel?> {
            override fun onResponse(
                call: Call<TicketChangeModel?>,
                response: Response<TicketChangeModel?>
            ) {
                Log.i("checkChange",Gson().toJson(response.body()))

                if (response.body() != null)
                    isChange = response.body()?.is_change == true
            }

            override fun onFailure(call: Call<TicketChangeModel?>, t: Throwable) {
//                progressWheel!!.visibility = View.GONE
//                progerssProgressDialog.dismiss()
            }
        })

    }

    fun checkAdminOnline(is_resume: Boolean? = null){
        val routes = ApiPanelController.client.create(PanelRoutes::class.java)
        routes.checkAdminOnline().enqueue(object : Callback<OnlineAdminResponse>{
            override fun onResponse(
                call: Call<OnlineAdminResponse>,
                response: Response<OnlineAdminResponse>
            ) {
                Log.e("ticket",response.body()?.toString() ?: "")
                if (response.isSuccessful && response.code() == 200){
                    response.body()?.status?.let {
                        setupRecyclerview(progressWheel, is_resume = is_resume ?: false, isOnline = it)
                    }
                }else{
                    setupRecyclerview(progressWheel, is_resume = is_resume ?: false, isOnline = false)
                }
            }

            override fun onFailure(call: Call<OnlineAdminResponse>, t: Throwable) {
                Log.e("check online","online",t)
                setupRecyclerview(progressWheel, is_resume = is_resume ?: false,isOnline = false)
            }

        })
    }

    fun setupRecyclerview(progressWheel: ProgressWheel?, is_resume: Boolean = false,isOnline : Boolean? = null) {
        val routes = ApiPanelController.client.create(PanelRoutes::class.java)

        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val toekn = sharedPreferences.getString("token", null)


        routes.getTickets(toekn).enqueue(object : Callback<TicketsModel> {
            override fun onResponse(
                call: Call<TicketsModel>,
                response: Response<TicketsModel>
            ) {
                progressWheel?.visibility = View.GONE
                response.body()?.tickets?.forEach{
                    Log.e("ffff",it.toString())
                }

                if (!is_resume) {
                    linearLayout = LinearLayoutManager(this@MainTicketActivity)
                    recyclerView?.layoutManager = linearLayout
                }

                scrollPosition = linearLayout?.findFirstVisibleItemPosition() ?: 0
                val ticketsModel = response.body()


                if (!is_resume) {
                    Log.i("run_ticket", "adapter")
                    adapter =
                        TicketAdapter(this@MainTicketActivity, ticketsModel?.tickets ?: listOf(),isOnline ?: false)
                    recyclerView?.adapter = adapter
                } else {
                    adapter =
                        TicketAdapter(this@MainTicketActivity, ticketsModel?.tickets ?: listOf(),isOnline ?: false)

                    if (recyclerView?.adapter == null) {
                        Log.i("run_ticket", "adapter_null")
                        recyclerView?.adapter = adapter

                        linearLayout?.scrollToPosition(scrollPosition)
                    } else {
                        Log.i("run_ticket", "adapter_notifyDataSetChanged")
                        recyclerView?.adapter = adapter

                        recyclerView?.adapter?.notifyDataSetChanged()
                        linearLayout?.scrollToPosition(scrollPosition)
                    }
                }
            }

            override fun onFailure(call: Call<TicketsModel>, t: Throwable) {
                progressWheel?.visibility = View.GONE
    //                progerssProgressDialog.dismiss()

            }
        })



    }
}