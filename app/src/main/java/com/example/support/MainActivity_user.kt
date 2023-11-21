package com.example.support

import CircleCropWithBorder
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.support.new_api.ApiController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_api.Routes
import com.example.support.new_model.ChangeProfileModel
import com.example.support.new_model.UploadImageModel
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class MainActivity_user : AppCompatActivity() {

    var Tv_Name: TextView? = null
    var Tv_family: TextView? = null
    var Tv_Exit: Button?= null
    var Im_profile: ImageView? = null
    var Btn_editProfile: Button? = null
    var uploadedImage_url: String? = ""


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_user)

        Tv_Name = findViewById<View>(R.id.Tv_Name) as TextView
        Tv_family = findViewById<View>(R.id.Tv_family) as TextView
        Tv_Exit = findViewById<View>(R.id.Tv_Exit) as Button
        Im_profile = findViewById<View>(R.id.img_profile) as ImageView
        Btn_editProfile = findViewById<View>(R.id.btn_edit_profile) as Button

        ShowProfile()

        Btn_editProfile!!.setOnClickListener {
            pickImageGallery()
        }
    }

    fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, RegisterUserActivity.IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RegisterUserActivity.IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {

//            Im_profile!!.setImageURI(data!!.data)

            if (data!!.data != null) {
                lifecycleScope.launch {
                    try {
                        val response = uploadImage(data.data!!)
                        if (response.status.equals("ok")) {

                            uploadedImage_url = response.uploaded_image

                            val sharedPreferences = getSharedPreferences("Shtoken", 0)
                            val token = sharedPreferences.getString("token", "")


                            changeProfile(token!!, uploadedImage_url!!)

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

//        } else if (resultCode == ImagePicker.RESULT_ERROR) {
//            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this, "لغو شد", Toast.LENGTH_SHORT).show()


        }

    }

    fun changeProfile(_token: String, _profile: String) {
        val routes = ApiController.client.create(Routes::class.java)

        val call =
            routes.changeProfile(_token, _profile)
        call!!.enqueue(object : Callback<ChangeProfileModel?> {
            override fun onResponse(
                call: Call<ChangeProfileModel?>,
                response: Response<ChangeProfileModel?>
            ) {
                Log.i("cjmohammad", response.body().toString())
//                progress_wheel!!.visibility = View.GONE

                val changeProfileModel = response.body()
                Log.i("cjmohammad", Gson().toJson(changeProfileModel))
                if (changeProfileModel!!.status == "ok") {
                    val sharedPreferences = getSharedPreferences("Shtoken", 0)
                    val editor = sharedPreferences.edit()
                    editor.putString("profile", uploadedImage_url)
                    editor.commit()

                    Glide.with(this@MainActivity_user)
                        .load(uploadedImage_url)
                        .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY))
                        .into(Im_profile!!)

                } else {
                    Toast.makeText(
                        this@MainActivity_user,
                        changeProfileModel.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ChangeProfileModel?>, t: Throwable) {
                Log.i("cjmohammad", t.toString())
                Toast.makeText(
                    this@MainActivity_user,
                    "خطا در ارسال اطلاعات....",
                    Toast.LENGTH_SHORT
                ).show()
//                progress_wheel!!.visibility = View.GONE
            }
        })
    }

    suspend fun uploadImage(imageUri: Uri): UploadImageModel {

        val imageFile_s: File? = imageUri.uriToFile(this)

        val imageFile = imageFile_s?.let {
            val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, requestBody)
        }

        val apiService = ApiController.client.create(PanelRoutes::class.java)
        return apiService.uploadImage(imageFile)!!
    }

    @SuppressLint("Range")
    fun Uri.uriToFile(context: Context): File? {
        val cursor = context.contentResolver.query(this, null, null, null, null)
        cursor?.moveToFirst()
        val filePath = cursor?.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor?.close()
        return filePath?.let { File(it) }
    }


    @SuppressLint("SetTextI18n")
    private fun ShowProfile() {
        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val name = sharedPreferences.getString("name", null)
        val family = sharedPreferences.getString("family", null)
        val username = sharedPreferences.getString("username", null)
        val profile = sharedPreferences.getString("profile", "")

        if (!Im_profile!!.equals("")) {

//            card_profile!!.visibility = View.GONE
//            Im_profile!!.visibility = View.VISIBLE

            Glide.with(this)
                .load(profile)
                .placeholder(R.drawable.ic_customer)
                .error(R.drawable.ic_customer)
                .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY)).into(Im_profile!!)
//            Glide.with(this).load(profile).into(Im_profile!!);
        }

        Log.i("cjmohammad", name + family)
        if (name != null) {
            Tv_Name!!.text = " نام شما : $name"
        }
        if (family != null) {
            Tv_family!!.text = " نام خانوادگی شما  : $family"
        }
        if (username != null) {


        }

        Tv_Exit!!.setOnClickListener {
            val sharedPreferences = this.getSharedPreferences("Shtoken", 0)
            val editor = sharedPreferences.edit()
            editor.putString("tokenfirebase", null)
            editor.putString("token",null)
            editor.commit()
            this.getSharedPreferences("Shtoken", 0).edit().clear().apply()
            //editor.apply()
            this.finish()
        }


    }
}