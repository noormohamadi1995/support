package com.example.support.ticket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.example.support.R
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_model.AddTicketModel
import com.pnikosis.materialishprogress.ProgressWheel

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainTicketAddActivity : AppCompatActivity() {
    var etComment: TextView? = null
    var btnAdd: Button? = null
    private var mProgressView :  ProgressWheel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ticket_add)
        val mToolbar = findViewById<View>(R.id.mToolbar) as Toolbar
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etComment = findViewById<View>(R.id.ET_Comment) as TextView
        btnAdd = findViewById<View>(R.id.Btn_addTicket) as Button
        mProgressView = findViewById<View>(R.id.progress_wheel) as ProgressWheel


        btnAdd?.setOnClickListener {
            val routes = ApiPanelController.client.create(PanelRoutes::class.java)

            val sharedPreferences = getSharedPreferences("Shtoken", 0)
            val token = sharedPreferences.getString("token", null)

            if(etComment?.text.toString().trim() == ""){
                Toast.makeText(
                    this@MainTicketAddActivity,
                    "عنوان تیکت را وارد کنید",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener;
            }
            mProgressView?.visibility = View.VISIBLE
            val call = routes.addTicket(token, etComment?.text.toString())

            call.enqueue(object : Callback<AddTicketModel> {
                override fun onResponse(
                    call: Call<AddTicketModel>,
                    response: Response<AddTicketModel>
                ) {
                    mProgressView?.visibility = View.GONE
                    Toast.makeText(
                        this@MainTicketAddActivity,
                        response.body()?.message ?: "",
                        Toast.LENGTH_SHORT
                    ).show()

                    onBackPressed()
                }

                override fun onFailure(call: Call<AddTicketModel>, t: Throwable) {
                    mProgressView?.visibility = View.GONE
                    Log.i("ss",t.message.toString())
                }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
