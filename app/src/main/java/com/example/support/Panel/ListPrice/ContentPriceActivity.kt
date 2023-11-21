package com.example.support.Panel.ListPrice

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.bumptech.glide.Glide
import android.net.Uri
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import com.example.support.R
import com.example.support.new_api.ApiController
import com.example.support.new_api.Routes
import com.example.support.new_model.CheckReferralCodeResponse
import com.example.support.new_model.OffCodeModel

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat

class ContentPriceActivity : AppCompatActivity() {

    var Ig_price: ImageView? = null
    var Btn_price: Button? = null
    var Tv_name: TextView? = null
    var Tv_price: TextView? = null
    var Tv_price_off: TextView? = null
    var Tv_content: TextView? = null
    var view_price: View? = null
    private var effectCode : String? = null
    private var morefCode : String? = null

    private var progressDialog : android.app.ProgressDialog? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content__pice)

        Ig_price = findViewById<View>(R.id.Ig_price) as ImageView
        Tv_name = findViewById<View>(R.id.Tv_name) as TextView
        Tv_price = findViewById<View>(R.id.Tv_price) as TextView
        Tv_price_off = findViewById<View>(R.id.Tv_price_off) as TextView
        Tv_content = findViewById<View>(R.id.Tv_content) as TextView
        Btn_price = findViewById<View>(R.id.btn_price) as Button
        view_price = findViewById(R.id.view_price)
        val btnEnableOffCode = findViewById<Button>(R.id.btnEnableOffCode)
        val offCodeLayout = findViewById<LinearLayout>(R.id.offCodeLayout)
        val referralCodLayout = findViewById<LinearLayout>(R.id.referralCodeLayout)
        val tvOffCode = findViewById<TextView>(R.id.tvOffcode)
        val tvReferralCode = findViewById<TextView>(R.id.tvReferralCode)
        val btnRemoveOffCode = findViewById<Button>(R.id.btnDeleteOffCode)
        val btnRemoveReferralCode = findViewById<Button>(R.id.btnDeleteReferralCode)
        val btnEnableReferralCode = findViewById<Button>(R.id.btnEnableReferralCode)

        btnRemoveOffCode.setOnClickListener {
            offCodeLayout.visibility = View.GONE
            tvOffCode.text = ""
            btnEnableOffCode.visibility = View.VISIBLE
            btnEnableReferralCode.visibility = View.VISIBLE
            effectCode = null
        }

        btnRemoveReferralCode.setOnClickListener {
            referralCodLayout.visibility = View.GONE
            tvReferralCode.text = ""
            btnEnableReferralCode.visibility = View.VISIBLE
            btnEnableOffCode.visibility = View.VISIBLE
            morefCode = null
        }

        btnEnableReferralCode.setOnClickListener {
            val dialog = OffCodeBottomSheetDialog()
            val sharedPreferences = getSharedPreferences("Shtoken",0)
            val token = sharedPreferences.getString("token","")
            val idVip = intent.getStringExtra("IdPost")
            val beforeEnable = intent.extras?.getString("price_Post")?.toLongOrNull()
            dialog.arguments = bundleOf(
                OffCodeBottomSheetDialog.TOKEN to token,
                OffCodeBottomSheetDialog.ID to idVip,
                OffCodeBottomSheetDialog.BEFORE_PRICE to beforeEnable,
                OffCodeBottomSheetDialog.TYPE to "referral"
            )
            dialog.show(supportFragmentManager,"dialog_off_code")
        }
        btnEnableOffCode.setOnClickListener {
            val dialog = OffCodeBottomSheetDialog()
            val sharedPreferences = getSharedPreferences("Shtoken",0)
            val token = sharedPreferences.getString("token","")
            val idVip = intent.getStringExtra("IdPost")
            val beforeEnable = intent.extras?.getString("price_Post")?.toLongOrNull()
            dialog.arguments = bundleOf(
                OffCodeBottomSheetDialog.TOKEN to token,
                OffCodeBottomSheetDialog.ID to idVip,
                OffCodeBottomSheetDialog.BEFORE_PRICE to beforeEnable,
                OffCodeBottomSheetDialog.TYPE to "off"
            )
            dialog.show(supportFragmentManager,"dialog_off_code")
        }

        progressDialog = android.app.ProgressDialog(this)
        progressDialog?.setMessage("لطفا منتظر بمانبد...")
        progressDialog?.setCancelable(false)

        showInformation()

        supportFragmentManager.setFragmentResultListener(
            OffCodeBottomSheetDialog.OFF_CODE_REQUEST,
            this
        ){requestKey, bundle ->
            if (requestKey == OffCodeBottomSheetDialog.OFF_CODE_REQUEST){
                effectCode = bundle.getString(OffCodeBottomSheetDialog.OFF_CODE, null)
                morefCode = bundle.getString(OffCodeBottomSheetDialog.MOREF_CODE,null)
                val price = bundle.getString(OffCodeBottomSheetDialog.PRICE,null)
                val percent = bundle.getString(OffCodeBottomSheetDialog.PERCENT,null)
                val type = bundle.getString(OffCodeBottomSheetDialog.TYPE, null)
                price?.apply {
                    val percentRest = if (percent?.isNotEmpty() == true) " درصد تخفیف:".plus(percent) else ""
                    offCodeLayout.visibility = if (effectCode?.isNotEmpty() == true) View.VISIBLE else View.GONE
                    tvOffCode.text = effectCode?.let {
                        "کد تخفیف: ".plus(it).plus(percentRest)
                    } ?: ""
                    referralCodLayout.visibility = if (morefCode?.isNotEmpty() == true) View.VISIBLE else View.GONE
                    tvReferralCode.text = morefCode?.let { "کدمعرف: ".plus(it).plus(percentRest) } ?: ""

                    if (type == "off") {
                        btnEnableOffCode.visibility = if (effectCode?.isEmpty() == true) View.VISIBLE else View.GONE
                        btnEnableReferralCode.visibility = View.GONE
                    }else{
                        btnEnableOffCode.visibility = View.GONE
                        btnEnableReferralCode.visibility = if (morefCode?.isEmpty() == true) View.VISIBLE else View.GONE
                    }

                   val p = this.toLongOrNull()
                    val beforeEnable = intent.extras?.getString("price_Post")?.toLongOrNull()
                    if (p != null && beforeEnable != null){
                        if (p >= beforeEnable){
                            Tv_price_off?.text = "قیمت بعد از تخفیف: رایگان"
                        }
                        else {
                            Tv_price_off?.text = "قیمت بعد از نخفیف: " + formatPrice(p?.toDouble() ?: 0.0) + "تومان"
                        }
                    }else{
                        Tv_price_off?.text = "قیمت بعد از نخفیف: " + formatPrice(p?.toDouble() ?: 0.0) + "تومان"
                    }

                    Tv_price_off?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun formatPrice(price: Double): String {
        val formatter = DecimalFormat("#,###.##")
        return formatter.format(price)
    }

    @SuppressLint("SetTextI18n")
    private fun showInformation() {
        val intent = intent
        Ig_price?.let {
            Glide.with(this).load(intent.getStringExtra("Imageurl")).into(it)
        }
        Tv_name?.text = intent.getStringExtra("NamePost")
        Tv_price?.text = formatPrice(intent.getStringExtra("price_Post")?.trim()?.toDouble() ?: 0.0)+" تومان"
        Tv_content?.text = intent.getStringExtra("Content_Post")
        Btn_price?.setOnClickListener {
            progressDialog?.show()
            val sharedPreferences = getSharedPreferences("Shtoken", 0)
            val token = sharedPreferences.getString("token", null)
            val idVip = intent.getStringExtra("IdPost")

            val route = ApiController.client.create(Routes::class.java)
            if (morefCode?.isNotEmpty() == true){
                route.checkReferralCode(
                    token = token,
                    idVip = idVip,
                    referralCode = morefCode
                ).enqueue(object : Callback<CheckReferralCodeResponse>{
                    override fun onResponse(
                        call: Call<CheckReferralCodeResponse>,
                        response: Response<CheckReferralCodeResponse>
                    ) {
                        progressDialog?.dismiss()
                        if (response.isSuccessful){
                            if (response.body()?.status == "ok"){
                                val i = Intent(Intent.ACTION_VIEW)
                                i.data = Uri.parse(response.body()?.payUrl)
                                startActivity(i)
                            }else if (response.body()?.status == "error"){
                                Toast.makeText(
                                    this@ContentPriceActivity,
                                    response.body()?.error ?: "",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<CheckReferralCodeResponse>, t: Throwable) {
                        Toast.makeText(
                            this@ContentPriceActivity,
                            "خطا در ارسال اطلاعات....",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialog?.dismiss()
                    }
                })
            }else {
                route.checkOffCode(
                    token,
                    idVip,
                    effectCode).enqueue(object : Callback<OffCodeModel> {
                    override fun onResponse(
                        call: Call<OffCodeModel>,
                        response: Response<OffCodeModel>
                    ) {
                        progressDialog?.dismiss()
                        val offCodeModel = response.body()

                        if (offCodeModel?.status == "ok") {
                            progressDialog?.dismiss()
                            if (response.isSuccessful){
                                if (response.body()?.status == "ok"){
                                    val i = Intent(Intent.ACTION_VIEW)
                                    i.data = Uri.parse(offCodeModel.payUrl)
                                    startActivity(i)
                                }else if (response.body()?.status == "error"){
                                    Toast.makeText(
                                        this@ContentPriceActivity,
                                        response.body()?.error ?: "",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<OffCodeModel?>, t: Throwable) {
                        Toast.makeText(
                            this@ContentPriceActivity,
                            "خطا در ارسال اطلاعات....",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialog?.dismiss()
                    }
                })
            }
        }

    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
}

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}
