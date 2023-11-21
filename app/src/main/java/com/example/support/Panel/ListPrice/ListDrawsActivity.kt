package com.example.support.Panel.ListPrice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.support.Panel.DrawListAdapter
import com.example.support.R
import com.example.support.databinding.ActivityListDrawsBinding
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_model.ListUserWithDrawResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListDrawsActivity : AppCompatActivity() {
    private var mBinding : ActivityListDrawsBinding? = null
    private var progressDialog : android.app.ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       mBinding = DataBindingUtil.setContentView(this,R.layout.activity_list_draws)
        mBinding?.drawsList?.layoutManager = LinearLayoutManager(this)
        mBinding?.drawsList?.adapter = DrawListAdapter()
        progressDialog = android.app.ProgressDialog(this)
        progressDialog?.setMessage("لطفا منتظر بمانبد...")
        progressDialog?.setCancelable(false)

        getDraws()
    }

    private fun getDraws() = mBinding?.apply {
        progressDialog?.show()
        val sharedPreferences = getSharedPreferences("Shtoken",0)
        val token = sharedPreferences.getString("token","")
        val rout = ApiPanelController.client.create(PanelRoutes::class.java)
        rout.getUserDraws(token)
            .enqueue(object : Callback<ListUserWithDrawResponse>{
                override fun onResponse(
                    call: Call<ListUserWithDrawResponse>,
                    response: Response<ListUserWithDrawResponse>
                ) {
                    progressDialog?.dismiss()
                    if (response.isSuccessful && response.code() == 200){
                        (drawsList.adapter as DrawListAdapter).submitList(
                            response.body()?.userWithDraws ?: listOf()
                        )
                    }else{
                        Toast.makeText(this@ListDrawsActivity,"خطا در دریافت اطلاعات", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ListUserWithDrawResponse>, t: Throwable) {
                    Log.e("drawList","error",t)
                    progressDialog?.dismiss()
                    Toast.makeText(this@ListDrawsActivity,"خطا در دریافت اطلاعات", Toast.LENGTH_SHORT).show()

                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}