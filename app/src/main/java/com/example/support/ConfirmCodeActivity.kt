package com.example.support

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.example.support.Dialog.GetCodeDialog
import com.example.support.Panel.Panel_user
import com.example.support.databinding.ActivityConfirmCodeBinding
import com.example.support.new_api.ApiController
import com.example.support.new_api.Routes
import com.example.support.new_model.LoginModel
import com.example.support.new_model.ResponseActiveCode
import com.google.firebase.iid.FirebaseInstanceId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

class ConfirmCodeActivity : AppCompatActivity() {
    private var mBinding : ActivityConfirmCodeBinding? = null
    private var phone : String? = null
    private var dialog : ProgressDialog? = null
    private var tokenfirebase :String? = null


    private val timer = object : CountDownTimer(60000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val totalSecond: Long = millisUntilFinished / 1000
            val minutes: Long = totalSecond % 3600 / 60
            val seconds: Long = totalSecond % 60

            val timeString = "$minutes:$seconds"
            val type: String =
                if (minutes > 0) getString(R.string.minutes) else getString(R.string.second)
            mBinding?.txtTimer?.text =
                getString(R.string.resend_code_title, timeString).plus(" ").plus(type)
        }

        override fun onFinish() {
            mBinding?.timerLayout?.visibility = View.GONE
            mBinding?.btnResendCode?.visibility = View.VISIBLE
        }

    }

    companion object{
        const val PHONE = "phone"
        const val TYPE = "type"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        const val OTPTYPE = "otp"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_confirm_code)
        timer.start()
        phone = intent.extras?.getString(PHONE) ?: ""
        mBinding?.txtTitle?.text = getString(R.string.verify_code_title,phone)
        dialog = ProgressDialog(this)
        dialog?.setCancelable(false)
        dialog?.setMessage("در حال بارگزاری اطلاعات...")
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    tokenfirebase = task.result.token
                } else {
                    Toast.makeText(
                        this@ConfirmCodeActivity,
                        task.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        setUpView()
    }

    private fun setUpView() = mBinding?.apply {
        val type = intent.extras?.getString(TYPE,"")
        edtCode.doAfterTextChanged { it ->
            btnRegister.isEnabled = it.toString().isNotEmpty() && it.toString().length == 6
            it.toString().takeIf { it.isNotEmpty() && it.length == 6 }?.let {
                if (type == ConfirmType.OTP.typ){
                    loginOtp(it)
                }else confirmCode(it)
            }
        }

        btnResendCode.setOnClickListener {
            dialog?.show()
            resendCode(phone ?: "")
        }

        btnEditMobile.setOnClickListener {
            val dialog = GetCodeDialog()
            dialog.arguments = bundleOf(
                "type" to "login"
            )
            dialog.show(supportFragmentManager,"get_code_dialog")
        }

        supportFragmentManager.setFragmentResultListener(GetCodeDialog.REQUEST,this@ConfirmCodeActivity){requestKey, bundle ->
            if (requestKey == GetCodeDialog.REQUEST){
                val phone = bundle.getString(GetCodeDialog.PHONE,"")
                dialog?.show()
                resendCode(phone)
            }
        }
    }

    private fun resendCode(phone : String) = mBinding?.apply{
        val routes = ApiController.client.create(Routes::class.java)
        routes.resendCode(phone).enqueue(object : Callback<ResponseActiveCode>{
            override fun onResponse(
                call: Call<ResponseActiveCode>,
                response: Response<ResponseActiveCode>
            ) {
                dialog?.dismiss()
                if (response.isSuccessful && response.code() == 200){
                    if (response.body()?.status == "ok"){
                        btnResendCode.visibility = View.GONE
                        timerLayout.visibility = View.VISIBLE
                        timer.start()
                        Toast.makeText(
                            this@ConfirmCodeActivity,
                            response.body()?.message ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        Toast.makeText(
                            this@ConfirmCodeActivity,
                            response.body()?.error ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseActiveCode>, t: Throwable) {
                dialog?.dismiss()
                Toast.makeText(
                    this@ConfirmCodeActivity,
                    "در دریافت اطلاعات خطایی رخ داده است",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("error","resend code",t)
            }

        })
    }

    private fun confirmCode(code : String) {
        dialog?.show()
        val routes = ApiController.client.create(Routes::class.java)
        routes.confirmCode(phone ?: "",code).enqueue(object : Callback<ResponseActiveCode>{
            override fun onResponse(
                call: Call<ResponseActiveCode>,
                response: Response<ResponseActiveCode>
            ) {
                dialog?.dismiss()
                if (response.isSuccessful && response.code() == 200){
                    if (response.body()?.status == "ok"){
                        Toast.makeText(
                                this@ConfirmCodeActivity,
                                response.body()?.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()
                        login()
                    }else{
                        Toast.makeText(
                                this@ConfirmCodeActivity,
                            response.body()?.error ?: "",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
            }
            override fun onFailure(call: Call<ResponseActiveCode>, t: Throwable) {
                dialog?.dismiss()
                Toast.makeText(
                    this@ConfirmCodeActivity,
                    "در دریافت اطلاعات خطایی رخ داده است",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("error","active code",t)
            }
        }
        )
    }

    private fun login() {
        val username = intent.extras?.getString(USERNAME,"")
        val password = intent.extras?.getString(PASSWORD,"")
        val routes = ApiController.client.create<Routes>()
        routes.login(
           tokenfirebase =  tokenfirebase,
           username =  username,
            password = password
        ).enqueue(object : Callback<LoginModel>{
            override fun onResponse(call: Call<LoginModel>, response: Response<LoginModel>) {
                dialog?.dismiss()
                if (response.isSuccessful && response.code() == 200){
                    val loginModel = response.body()
                    if (loginModel?.status == "ok"){
                        val sharedPreferences = getSharedPreferences("Shtoken", 0)
                        val editor = sharedPreferences.edit()
                        editor.putString("tokenfirebase", tokenfirebase)
                        editor.putString("token",loginModel.token)
                        editor.putString("name", loginModel.name)
                        editor.putString("family", loginModel.famili)
                        editor.putString("profile", loginModel.profile)
                        editor.putString("referralCode",loginModel.referral_code)
                        editor.putBoolean("isActive", loginModel.isActive ?: false)
                        editor.putString("phone", loginModel.mobile)
                        editor.putString("username", loginModel.username)
                        editor.apply()
                        val type = intent.extras?.getString(TYPE)
                        val intent = if (type == ConfirmType.REGISTER.typ)
                            Intent(this@ConfirmCodeActivity, Panel_user::class.java)
                        else Intent(this@ConfirmCodeActivity, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }else{
                        Toast.makeText(this@ConfirmCodeActivity,
                        loginModel?.error ?: "",
                        Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            override fun onFailure(call: Call<LoginModel>, t: Throwable) {
                dialog?.dismiss()
                Toast.makeText(
                    this@ConfirmCodeActivity,
                    "در دریافت اطلاعات خطایی رخ داده است",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun loginOtp(code : String) = mBinding?.apply {
        dialog?.show()
        val phone = intent.extras?.getString(PHONE,"")
        val routes = ApiController.client.create<Routes>()
        routes.loginOtp(
            tokenfirebase =  tokenfirebase,
            mobile =  phone,
            code = code
        ).enqueue(object : Callback<LoginModel>{
            override fun onResponse(call: Call<LoginModel>, response: Response<LoginModel>) {
                dialog?.dismiss()
                if (response.isSuccessful && response.code() == 200){
                    val loginModel = response.body()
                    if (loginModel?.status == "ok"){
                        val sharedPreferences = getSharedPreferences("Shtoken", 0)
                        val editor = sharedPreferences.edit()
                        editor.putString("tokenfirebase", tokenfirebase)
                        editor.putString("token",loginModel.token)
                        editor.putString("name", loginModel.name)
                        editor.putString("family", loginModel.famili)
                        editor.putString("profile", loginModel.profile)
                        editor.putString("referralCode",loginModel.referral_code)
                        editor.putBoolean("isActive", loginModel.isActive ?: false)
                        editor.putString("phone", loginModel.mobile)
                        editor.putString("username", loginModel.username)
                        editor.apply()
                        val type = intent.extras?.getString(TYPE)
                        val intent = if (type == ConfirmType.REGISTER.typ)
                            Intent(this@ConfirmCodeActivity, Panel_user::class.java)
                        else Intent(this@ConfirmCodeActivity, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }else{
                        Toast.makeText(this@ConfirmCodeActivity,
                            loginModel?.error ?: "",
                            Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            override fun onFailure(call: Call<LoginModel>, t: Throwable) {
                dialog?.dismiss()
                Toast.makeText(
                    this@ConfirmCodeActivity,
                    "در دریافت اطلاعات خطایی رخ داده است",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }
}

enum class ConfirmType(val typ : String){
    RESET("reset"),REGISTER("register"),OTP("otp")
}