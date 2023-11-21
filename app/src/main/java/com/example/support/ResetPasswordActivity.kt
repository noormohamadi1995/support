package com.example.support

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.google.gson.Gson
import android.util.Log
import com.v2ray.rocket.new_model.ResetPasswordModel
import android.view.View
import android.widget.*
import com.example.support.Panel.Panel_user
import com.example.support.new_api.ApiController
import com.example.support.new_api.Routes
import com.example.support.new_model.LoginModel
import com.example.support.new_model.ResponseActiveCode
import com.google.firebase.iid.FirebaseInstanceId
import com.pnikosis.materialishprogress.ProgressWheel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {
    var etPassword: EditText? = null
    var etCode: EditText? = null
    var btnReset: Button? = null

    var tokenfirebase :String? = ""
    private var dialog : ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_activity)
        dialog = ProgressDialog(this)
        dialog?.setCancelable(false)
        dialog?.setMessage("در حال بارگزاری اطلاعات...")
        title = "ریست پسورد"

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    tokenfirebase = task.result.token
                    Log.i("cjmohammad", "Firebase Token: $tokenfirebase")
                } else {
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        task.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            etPassword = findViewById<View>(R.id.ET_new_password) as EditText
            etCode = findViewById<View>(R.id.ET_security_question) as EditText
            btnReset = findViewById<View>(R.id.btn_recovery) as Button

            btnReset?.setOnClickListener {
                val password = etPassword?.text.toString().trim()
                val code = etCode?.text?.toString()?.trim()
            if (password.isEmpty()) {
                etPassword?.error = "لطفا نام کاربری را وارد کنید...."
                etPassword?.requestFocus()
            }else  if (code?.isEmpty() == true) {
                etCode?.error = "لطفا کد وارد شده را وارد کنید...."
                etCode?.requestFocus()
            }
            else {
                dialog?.show()
                confirmCode()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun confirmCode() {
        val phone = intent.extras?.getString(ConfirmCodeActivity.PHONE,"")
        val code = etCode?.text?.toString()?.trim()
        val password = etPassword?.text?.toString()?.trim()
        val routes = ApiController.client.create(Routes::class.java)
        routes.resetPasswordCode(
            phone = phone,
            code = code,
            password = password
        ).enqueue(object : Callback<LoginModel>{
            override fun onResponse(
                call: Call<LoginModel>,
                response: Response<LoginModel>
            ) {
                dialog?.dismiss()
                if (response.isSuccessful && response.code() == 200){
                    if (response.body()?.status == "ok"){
                        val loginModel = response.body()
                        val sharedPreferences = getSharedPreferences("Shtoken", 0)
                        val editor = sharedPreferences.edit()
                        editor.putString("tokenfirebase", tokenfirebase)
                        editor.putString("token", loginModel?.token)
                        editor.putString("name", loginModel?.name)
                        editor.putString("family", loginModel?.famili)
                        editor.putString("profile", loginModel?.profile)
                        editor.putString("referralCode", loginModel?.referral_code)
                        editor.putBoolean("isActive", loginModel?.isActive ?: false)
                        editor.putString("phone",loginModel?.mobile)
                        editor.putString("username",loginModel?.username)
                        editor.apply()
                        val intent = Intent(this@ResetPasswordActivity, MainActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }else{
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            response.body()?.error ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginModel>, t: Throwable) {
                dialog?.dismiss()
                Toast.makeText(
                    this@ResetPasswordActivity,
                     "خطا در دریافت اطلاعات",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}