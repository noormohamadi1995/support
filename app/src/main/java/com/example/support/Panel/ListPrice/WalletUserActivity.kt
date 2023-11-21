package com.example.support.Panel.ListPrice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.support.Dialog.WalletDialog
import com.example.support.Panel.DrawListAdapter
import com.example.support.R
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_api.Routes
import com.example.support.new_model.AtLeastModel
import com.example.support.new_model.WalletAmountResponseModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class WalletUserActivity : AppCompatActivity() {
    var tvAmount : TextView? = null
    var btnGetPrice : Button? = null
    var btnRequests : Button? = null
    var tvAtLeast : TextView? = null

    private val refreshRunnable = object : Runnable {
        override fun run() {
            showWallet()
            getAtLeastPrice()
            handler.postDelayed(this, 2000)
        }
    }

    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        tvAmount = findViewById(R.id.wallet_inventory)
        btnGetPrice = findViewById(R.id.Btn_price)
        btnRequests = findViewById(R.id.btnRequests)
        tvAtLeast = findViewById(R.id.atLeastPrice)

        btnGetPrice?.setOnClickListener {
            val dialog = WalletDialog()
            dialog.show(supportFragmentManager,"wallet_dialog")
        }

        btnRequests?.setOnClickListener {
            Intent(this,ListDrawsActivity::class.java).apply {
                startActivity(this)
            }
        }

        showWallet()
        getAtLeastPrice()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(refreshRunnable, 2000)
    }

    private fun showWallet(){
        val sharedPreferences = getSharedPreferences("Shtoken",0)
        val token = sharedPreferences.getString("token","")
        val route = ApiPanelController.client.create(PanelRoutes::class.java)
        route.getWalletAmount(token).enqueue(object : Callback<WalletAmountResponseModel>{
            override fun onResponse(
                call: Call<WalletAmountResponseModel>,
                response: Response<WalletAmountResponseModel>
            ) {
                if (response.isSuccessful && response.code() == 200){
                    val editor = sharedPreferences.edit()
                    editor.putLong("walletAmount",response.body()?.walletAmount?.toLongOrNull() ?: 0.toLong())
                    editor.apply()
                    val price = formatPrice(response.body()?.walletAmount?.toLongOrNull() ?: 0.toLong())
                    response.body()?.walletAmount?.let {
                        tvAmount?.text = price.plus(getFormatMoneyPrice(it.toLong()))
                    }
                }else {
                    tvAmount?.text = "0".plus(" تومان")
                    Toast.makeText(this@WalletUserActivity,response.body()?.message ?: "",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WalletAmountResponseModel>, t: Throwable) {
                Log.e("getWallet Error","error",t)
                Toast.makeText(this@WalletUserActivity,"خطا در دریافت اطلاعات",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getAtLeastPrice(){
        val token = getSharedPreferences("Shtoken",0).getString("token","")
        val route = ApiPanelController.client.create(PanelRoutes::class.java)
        route.getAtLeastPrice(token)
            .enqueue(object : Callback<AtLeastModel>{
                override fun onResponse(
                    call: Call<AtLeastModel>,
                    response: Response<AtLeastModel>
                ) {
                    if (response.isSuccessful){
                        if (response.body()?.status == "ok"){
                            response.body()?.setting?.minWithDrawAmount?.let {
                                val price = formatPrice(it.toLong())
                                tvAtLeast?.text = "حداقل میزان برداشت: ".plus(price.plus(getFormatMoneyPrice(price = it.toLong() )))
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<AtLeastModel>, t: Throwable) {
                    Log.e("at least","error",t)
                }
            })
    }

    private fun formatPrice(price: Long): String {
        val formatter = DecimalFormat("#,###.##")
        return formatter.format(price)
    }

    private fun getFormatMoneyPrice(price: Long) : String{
        return if (price >= 1000000){
            " میلیون تومان"
        }else " تومان"
    }
}