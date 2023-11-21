package com.example.support

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.example.support.Dialog.GetCodeDialog
import com.example.support.databinding.ActivityLoginActivityBinding
import com.example.support.new_api.ApiController
import com.example.support.new_api.Routes
import com.example.support.new_model.LoginModel
import com.example.support.new_model.ResponseActiveCode
import com.google.firebase.iid.FirebaseInstanceId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var mBinding : ActivityLoginActivityBinding? = null
    var tokenfirebase: String? = ""
    private var dialog : ProgressDialog? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this,R.layout.activity_login_activity)
        dialog = ProgressDialog(this)
        dialog?.setCancelable(false)
        dialog?.setMessage("در حال بارگزاری اطلاعات...")
        title = "ورود"


        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    tokenfirebase = task.result.token
                    Log.i("cjmohammad", "Firebase Token: $tokenfirebase")
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        task.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        mBinding?.apply {

            btnResetPassword.setOnClickListener {
                val dialog = GetCodeDialog()
                dialog.arguments = bundleOf(
                    "type" to "reset"
                )
                dialog.show(supportFragmentManager,"get_code_dialog")
            }

            btnRegister.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterUserActivity::class.java)
                startActivity(intent)
                finish()
            }

            btnLogin.setOnClickListener {
                etUsernameLay.error = ""
                etPasswordLay.error = ""
                val username = etUsername.text.toString()
                val password = etPassword.text.toString()
                if (username.isEmpty()) {
                    etUsernameLay.error = "لطفا نام کاربری را وارد کنید...."
                    etUsername.requestFocus()
                } else if (password.isEmpty()) {
                    etPasswordLay.error = "لطفا نام کاربری را وارد کنید...."
                    etPassword.requestFocus()
                } else {
                    dialog?.show()
                    val routes = ApiController.client.create(Routes::class.java)
                    val call = routes.login(username, password, tokenfirebase)
                    call.enqueue(object : Callback<LoginModel?> {
                        override fun onResponse(
                            call: Call<LoginModel?>,
                            response: Response<LoginModel?>
                        ) {
                            val loginModel = response.body()
                            dialog?.dismiss()
                            if (loginModel?.status == "ok") {
                                val sharedPreferences = getSharedPreferences("Shtoken", 0)
                                val editor = sharedPreferences.edit()
                                editor.putString("tokenfirebase", tokenfirebase)
                                editor.putString("token", loginModel.token)
                                editor.putString("name", loginModel.name)
                                editor.putString("family", loginModel.famili)
                                editor.putString("profile", loginModel.profile)
                                editor.putString("referralCode", loginModel.referral_code)
                                editor.putBoolean("isActive", loginModel.isActive ?: false)
                                editor.putString("phone", loginModel.mobile)
                                editor.putString("username", loginModel.username)
                                editor.apply()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finishAffinity()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    loginModel?.error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginModel?>, t: Throwable) {
                            dialog?.dismiss()
                            Toast.makeText(
                                this@LoginActivity,
                                "خطا در ارسال اطلاعات....",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                    )
                }
            }

            btnLoginOtp.setOnClickListener {
                val dialog = GetCodeDialog()
                dialog.arguments = bundleOf(
                    "type" to "login"
                )
                dialog.show(supportFragmentManager,"get_code_dialog")
            }
        }

        supportFragmentManager.setFragmentResultListener(GetCodeDialog.REQUEST,this){requestKey, bundle ->
            if (requestKey == GetCodeDialog.REQUEST){
                val phone = bundle.getString(GetCodeDialog.PHONE,"")
                val type = bundle.getString(GetCodeDialog.TYPE,"")
                dialog?.show()
                sendCode(phone,type)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun sendCode(phone : String,type : String) = mBinding?.apply{
        val routes = ApiController.client.create(Routes::class.java)
        routes.resendCode(phone).enqueue(object : Callback<ResponseActiveCode>{
            override fun onResponse(
                call: Call<ResponseActiveCode>,
                response: Response<ResponseActiveCode>
            ) {
                dialog?.dismiss()
                if (response.isSuccessful && response.code() == 200){
                    if (response.body()?.status == "ok"){
                        Toast.makeText(
                            this@LoginActivity,
                            response.body()?.message ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = if (type == "login")
                            Intent(this@LoginActivity, ConfirmCodeActivity::class.java)
                        else Intent(this@LoginActivity, ResetPasswordActivity::class.java)
                        intent.putExtra(ConfirmCodeActivity.PHONE,phone)
                        intent.putExtra(ConfirmCodeActivity.TYPE,ConfirmType.OTP.typ)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(
                            this@LoginActivity,
                            response.body()?.error ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseActiveCode>, t: Throwable) {
                dialog?.dismiss()
                Toast.makeText(
                    this@LoginActivity,
                    "در دریافت اطلاعات خطایی رخ داده است",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("error","resend code",t)
            }
        })
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}