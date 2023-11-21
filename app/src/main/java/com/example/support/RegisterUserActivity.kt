package com.example.support

import CircleCropWithBorder
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.support.new_api.ApiController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_api.Routes
import com.example.support.new_model.RegisterModel
import com.example.support.new_model.UploadImageModel
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class RegisterUserActivity : AppCompatActivity() {
    var ET_name: EditText? = null
    var ET_family: EditText? = null
    var ET_username: EditText? = null
    var ET_password: EditText? = null
    var ET_secure_question: EditText? = null
    var btnSend: Button? = null
    var imageView_upload: ImageView? = null
    var button_upload: Button? = null
    private var mToolbar : androidx.appcompat.widget.Toolbar? = null

    var uploadedImage_url: String? = ""

    var tokenfirebase :String? = null
    private var dialog : ProgressDialog? = null


    companion object {
        val IMAGE_REQUEST_CODE = 100
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register__user)
        dialog = ProgressDialog(this)
        dialog?.setCancelable(false)
        dialog?.setMessage("در حال بارگزاری اطلاعات...")
        title = "ثبت نام"

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    tokenfirebase = task.result.token
                    Log.i("cjmohammad", "Firebase Token: $tokenfirebase")
                } else {
                    Toast.makeText(
                        this@RegisterUserActivity,
                        task.exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }




        ET_name = findViewById<View>(R.id.ET_name) as EditText
        ET_family = findViewById<View>(R.id.ET_family) as EditText
        ET_username = findViewById<View>(R.id.ET_username) as EditText
        ET_password = findViewById<View>(R.id.ET_password) as EditText
        ET_secure_question = findViewById<View>(R.id.ET_secure_question) as EditText
        btnSend = findViewById<View>(R.id.btnSend) as Button
        imageView_upload = findViewById<View>(R.id.imageView_upload) as ImageView
        button_upload = findViewById<View>(R.id.button_upload) as Button
        mToolbar = findViewById(R.id.mToolbar)
        setSupportActionBar(mToolbar)

        Glide.with(this)
            .load(R.drawable.baseline_person_24)
            .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY))
            .into(imageView_upload!!)

        button_upload!!.setOnClickListener {
            pickImageGallery()
        }
        buttonSend()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun buttonSend() {
        btnSend?.setOnClickListener { view: View? ->
            val strname = ET_name?.text.toString()
            val strfamily = ET_family?.text.toString()
            val strusername = ET_username?.text.toString()
            val strpassword = ET_password?.text.toString()
            val mobile = ET_secure_question?.text.toString()
            val imageView_upload = imageView_upload?.imageAlpha.toString()

            if(strname.isEmpty()) {
                ET_name!!.error = "نام خود را وارد کنید..."
                ET_name!!.requestFocus()
            } else if (strfamily.isEmpty()) {
                ET_family!!.error = "نام خانوادگی را وارد کنید..."
                ET_family!!.requestFocus()
            } else if (strusername.isEmpty()) {
//                Toast.makeText(this, "b4", Toast.LENGTH_SHORT).show()
                ET_username!!.error = "نام کاربری را وارد کنید..."
                ET_username!!.requestFocus()
            } else if (mobile.isEmpty()) {
//                Toast.makeText(this, "b5", Toast.LENGTH_SHORT).show()
                ET_secure_question!!.error = "جواب سوال امنیتی را وارد کنید..."
                ET_secure_question!!.requestFocus()
            } else if (mobile.length != 11) {
//                Toast.makeText(this, "b6", Toast.LENGTH_SHORT).show()
                ET_secure_question!!.error = "شماره موبایل باید ۱۱ رقمی باشد"
                ET_secure_question!!.requestFocus()
            } else if (strpassword.isEmpty()) {
//                Toast.makeText(this, "b7", Toast.LENGTH_SHORT).show()
                ET_password!!.error = "رمز عبور را وارد کنید..."
                ET_password!!.requestFocus()

            } else if (imageView_upload.isEmpty()) {

//            } else if (!is_uploaded_image) {
//                Toast.makeText(this, "انتخاب عکس مناسب!!", Toast.LENGTH_SHORT).show()
            } else {
                dialog?.show()
                Log.i("cjmohammad", "start register")
                val routes = ApiController.client.create(Routes::class.java)

                val call =
                    routes.register(
                        tokenfirebase,
                        strname,
                        strfamily,
                        strusername,
                        strpassword,
                        mobile,
                        uploadedImage_url!!
                    )
                call.enqueue(object : Callback<RegisterModel> {
                    override fun onResponse(
                        call: Call<RegisterModel>,
                        response: Response<RegisterModel>
                    ) {
                        dialog?.dismiss()
                        val registerModel = response.body()
                        if (registerModel?.status == "ok") {
                            val intent = Intent(this@RegisterUserActivity, ConfirmCodeActivity::class.java)
                            intent.putExtra(ConfirmCodeActivity.PHONE,mobile)
                            intent.putExtra(ConfirmCodeActivity.TYPE,ConfirmType.REGISTER.typ)
                            intent.putExtra(ConfirmCodeActivity.USERNAME,strusername)
                            intent.putExtra(ConfirmCodeActivity.PASSWORD,strpassword)
                            startActivity(intent)
                            finish()

                        } else {
                            Toast.makeText(
                                this@RegisterUserActivity,
                                registerModel?.error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterModel>, t: Throwable) {
                        Log.i("cjmohammad", t.toString())
                        Toast.makeText(
                            this@RegisterUserActivity,
                            "خطا در ارسال اطلاعات....",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog?.dismiss()
                    }
                })
            }
        }
    }


    fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)

    }

//    var is_uploaded_image = false

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {


            Glide.with(this)
                .load(data!!.data)
                .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY))
                .into(imageView_upload!!)

            if (data.data != null) {
                lifecycleScope.launch {
                    try {
                        val response = uploadImage(data.data!!)
                        if (response.status.equals("ok")) {
                            uploadedImage_url = response.uploaded_image
//                            is_uploaded_image = true
                        }
                        Log.d(
                            "uplaod",
                            "Image upload success: , message: ${response.message}, url: ${response.status}"
                        )
                    } catch (e: Exception) {
                        Log.e("upload", "Image upload failed", e)
                    }
                }
            }
        }
    }


    @SuppressLint("Range")
    fun Uri.uriToFile(context: Context): File? {
        val cursor = context.contentResolver.query(this, null, null, null, null)
        cursor?.moveToFirst()
        val filePath = cursor?.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor?.close()
        return filePath?.let { File(it) }
    }


    suspend fun uploadImage(imageUri: Uri): UploadImageModel {
        val imageFile_s: File? = imageUri.uriToFile(this)
        val imageFile = imageFile_s?.let {
            val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", imageFile_s.name, requestBody)
        }

        val apiService = ApiController.client.create(PanelRoutes::class.java)
        return apiService.uploadImage(imageFile)!!
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()

    }
}

