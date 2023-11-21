package com.example.support.Panel

import CircleCropWithBorder
import android.R.attr
import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.support.BuildConfig
import com.example.support.Dialog.Dialog_Exit
import com.example.support.MainActivity
import com.example.support.Panel.ListPrice.Activity_Price
import com.example.support.Panel.ListPrice.WalletUserActivity
import com.example.support.R
import com.example.support.RegisterUserActivity
import com.example.support.network.NetworkChangeReceiver
import com.example.support.new_api.ApiController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_api.Routes
import com.example.support.new_model.*
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream


class Panel_user : AppCompatActivity() {

    var Im_profile: ImageView? = null

    var Im_exit: ImageView? = null
    var Ig_panel: ImageView? = null

    var Im_back: ImageView? = null
    var Tv_Name: TextView? = null
    var Tv_family: TextView? = null
    var Tv_information: TextView? = null
    var Btn_price: Button? = null
    var Btn_editProfile: Button? = null
    var TxtDescription: TextView? = null
    var CardDescription: CardView? = null
    var linearAccountContent: LinearLayout? = null
    private var tvPhone : TextView? = null
    var wallet: CardView? = null
    var card_profile: CardView? = null
    var tvMoref : TextView? = null
    var morefCard : CardView? = null
    var dialogExite : Dialog_Exit? = null
    var btnShare : ImageView? = null

    var uploadedImage_url: String? = ""
    private val networkChangeReceiver = NetworkChangeReceiver()

    var hand = Handler()
    var apiDelayed = 1 * 5000
    var runnable: Runnable? = null
    private var dialog : android.app.ProgressDialog? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_user)

        dialog = android.app.ProgressDialog(this)
        dialog?.setCancelable(false)
        dialog?.setMessage("در حال بارگزاری اطلاعات...")

        Im_profile = findViewById<View>(R.id.img_profile) as ImageView

        Im_exit = findViewById<View>(R.id.Im_exit) as ImageView
        Im_back = findViewById<View>(R.id.Im_back) as ImageView
        Btn_editProfile = findViewById<View>(R.id.btn_edit_profile) as Button
        Ig_panel = findViewById<View>(R.id.Ig_panel) as ImageView
        Btn_price = findViewById<View>(R.id.Btn_price) as Button
        Tv_Name = findViewById<View>(R.id.Tv_Name) as TextView
        Tv_family = findViewById<View>(R.id.Tv_family) as TextView
        Tv_information = findViewById<View>(R.id.Tv_information) as TextView
        TxtDescription = findViewById<View>(R.id.account_description) as TextView
        CardDescription = findViewById<View>(R.id.cardview_description) as CardView
        card_profile = findViewById<View>(R.id.card_profile) as CardView
        linearAccountContent = findViewById<View>(R.id.linear_account_content) as LinearLayout
        wallet = findViewById<View>(R.id.liner4_wallet) as CardView
        tvMoref = findViewById(R.id.tv_moref_code)
        morefCard = findViewById(R.id.card_moref)
        tvPhone = findViewById(R.id.tv_phone)
        btnShare = findViewById(R.id.btnShare)

        ShowProfile()

        Btn_editProfile?.setOnClickListener {
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
            if (data?.data != null) {
                lifecycleScope.launch {
                    dialog?.show()
                    try {
                        val response = uploadImage(data.data!!)
                        dialog?.dismiss()
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
                        dialog?.dismiss()
                        Log.e("upload", "Image upload failed", e)
                    }
                }
            }
        }
    }

    private fun changeProfile(_token: String, _profile: String) {
        val routes = ApiController.client.create(Routes::class.java)

        val call =
            routes.changeProfile(_token, _profile)
        call.enqueue(object : Callback<ChangeProfileModel?> {
            override fun onResponse(
                call: Call<ChangeProfileModel?>,
                response: Response<ChangeProfileModel?>
            ) {
                Log.i("cjmohammad", response.body().toString())
                val changeProfileModel = response.body()
                Log.i("cjmohammad", Gson().toJson(changeProfileModel))
                if (changeProfileModel?.status == "ok") {
                    val sharedPreferences = getSharedPreferences("Shtoken", 0)
                    val editor = sharedPreferences.edit()
                    editor.putString("profile", uploadedImage_url)
                    editor.apply()

                    Glide.with(this@Panel_user)
                        .load(uploadedImage_url)
                        .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY))
                        .into(Im_profile!!)

                } else {
                    Toast.makeText(
                        this@Panel_user,
                        changeProfileModel?.error ?: "",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ChangeProfileModel?>, t: Throwable) {
                Log.i("cjmohammad", t.toString())
                Toast.makeText(
                    this@Panel_user,
                    "خطا در ارسال اطلاعات....",
                    Toast.LENGTH_SHORT
                ).show()
//                progress_wheel!!.visibility = View.GONE
            }
        })
    }

    private suspend fun uploadImage(imageUri: Uri): UploadImageModel {
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
        val toekn = sharedPreferences.getString("token", null)
        ShwoInformation_esh(toekn)
        val name = sharedPreferences.getString("name", null)
        val family = sharedPreferences.getString("family", null)
        val username = sharedPreferences.getString("username", null)
        val profile = sharedPreferences.getString("profile", "")
        val referralCode = sharedPreferences.getString("referralCode",null)
        val phone = sharedPreferences.getString("phone",null)


        if (Im_profile?.equals("")?.not() == true) {

            card_profile?.visibility = View.GONE
            Im_profile?.visibility = View.VISIBLE

            Glide.with(this)
                .load(profile)
                .placeholder(R.drawable.ic_customer)
                .error(R.drawable.we)
//                .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY))
                .into(Im_profile!!)
//            Glide.with(this).load(profile).into(Im_profile!!);
        }


        Log.i("cjmohammad", name + family + username)
        if (name != null) {
            Tv_Name?.text = " نام شما : $name $family"
        }
        if (username != null) {
            Tv_family?.text = " نام کاربری  : $username"
        }

        if (phone != null){
            tvPhone?.text = " شماره تماس : $phone"
        }
        if (referralCode != null) {
            tvMoref?.text =  " کد شما  : $referralCode"
        }

        if (referralCode?.isNotEmpty() == true){
            tvMoref?.text =  " کد شما  : $referralCode"
            tvMoref?.setOnClickListener {
                val clipboard =
                    getSystemService(Context.CLIPBOARD_SERVICE) as? (ClipboardManager)
                val clip = android.content.ClipData.newPlainText("Copied Text",referralCode)
                clipboard?.apply {
                    setPrimaryClip(clip)
                    Toast.makeText(this@Panel_user,"کپی شد",Toast.LENGTH_SHORT).show()
                }
            }
            btnShare?.setOnClickListener {
                try {
                    val route = ApiController.client.create(Routes::class.java)
                    route.getSettings().enqueue(object : Callback<AppSettingsModel>{
                        @SuppressLint("NewApi", "UseCompatLoadingForDrawables")
                        override fun onResponse(
                            call: Call<AppSettingsModel>,
                            response: Response<AppSettingsModel>
                        ) {
                            if (response.isSuccessful && response.code() == 200){
                                dialog?.dismiss()
                                val bitmap = getDrawable(R.drawable.paper_plane)?.toBitmap()
                                val uri = bitmap?.let { it1 -> getImageToShare(it1) }
                                val shareIntent = Intent(Intent.ACTION_SEND)
                                shareIntent.type = "image/*"
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "my app")
                                val referralMessage = "نام برنامه:".plus(getString(R.string.app_name)).plus("\n")
                                    .plus("کد معرف:").plus(referralCode).plus("\n")
                                    .plus("در موقع خرید با ثبت کد معرف مبلغ کمتری پرداخت کنید.")
                                    .plus("\n").plus("لینک دانلود مستقیم: ").plus(response.body()?.link)
                                shareIntent.putExtra(Intent.EXTRA_TEXT, referralMessage)
                                Log.e("uri",uri.toString())
                                shareIntent.putExtra(Intent.EXTRA_STREAM,uri)

                                startActivity(Intent.createChooser(shareIntent, "choose one"))
                                //getReferralCode(referralCode,response.body()?.link)
                            }
                        }

                        override fun onFailure(call: Call<AppSettingsModel>, t: Throwable) {
                            Log.e("setting","",t)
                            dialog?.dismiss()
                        }

                    })
                } catch (e: Exception) {
                   e.printStackTrace()
                }
            }
        }
    }

    private fun getImageToShare(bitmap: Bitmap): Uri? {
        val imageFolder = File(cacheDir, "images")
        var uri: Uri? = null
        try {
            imageFolder.mkdirs()
            val file = File(imageFolder, "logo.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            uri = if (Build.VERSION.SDK_INT >= 24)
                    FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
            else Uri.fromFile(file)
        } catch (e:Exception) {
           e.printStackTrace()
        }
        return uri
    }


    fun ShwoInformation_esh(strtoken: String?) {
        val routes = ApiController.client.create(Routes::class.java)
        val call = routes.checkUser(strtoken)
        call.enqueue(object : Callback<CheckUserModel> {
            @SuppressLint("SetTextI18n", "SuspiciousIndentation")
            override fun onResponse(
                call: Call<CheckUserModel>,
                response: Response<CheckUserModel>
            ) {
                val checkUserModel = response.body()
                val checkerro = checkUserModel?.status
                Log.i("cjmohammad_check", Gson().toJson(checkUserModel))
                if (checkerro == "error") {
                    Tv_information?.text = " سطح اشتراک : " + "کاربر معمولی"
                    Btn_price?.text = "خرید اشتراک"
                } else {
                    //   Tv_information!!.text = " تعداد روز های باقی مانده : " + checkUserModel.day

                    var accounts_str = ""


//                    val checkUserAccCount =  checkUserModel.Accounts!!.size
//
//                    for((index, accItem) in checkUserModel.Accounts!!.withIndex()){
//                        var is_nextLine:Boolean? = null
//                        is_nextLine = checkUserAccCount != (index + 1)
//
//                        accounts_str+= accItem.diff_day+" روز "+accItem.diff_hour+" ساعت "+accItem.diff_minute+" دقیقه "+accItem.diff_second+" ثانیه"
//                        if(is_nextLine)
//                            accounts_str +="\n"
//                    }
//
//                    Tv_information!!.text = accounts_str
                    Tv_information?.text =
                        checkUserModel?.diff_day + " روز " + checkUserModel?.diff_hour + " ساعت " + checkUserModel?.diff_minute + " دقیقه " + checkUserModel?.diff_second + " ثانیه"
                    Btn_price?.text = "ارتقاء حساب کابری"
                }


                if (checkUserModel?.comment != null && checkUserModel.comment != "") {
                    TxtDescription?.text = checkUserModel.comment

                } else {
                    linearAccountContent?.visibility = View.GONE
                }

             val Btn_price_back_list = findViewById<Button>(R.id.Btn_price_back_list)
                Btn_price_back_list!!.setOnClickListener {
                    val clipboard: ClipboardManager =
                        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.text = checkUserModel?.comment
//                    Toast.makeText(this@Panel_user, "کپی شد", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@Panel_user, MainActivity::class.java)
                    startActivity(intent)

                }

            }

            override fun onFailure(call: Call<CheckUserModel?>, t: Throwable) {
                Tv_information?.text = "خطا در دریافت اطلاعات...."
            }
        })

        Btn_price?.setOnClickListener {
            val intent = Intent(this@Panel_user, Activity_Price::class.java)
            startActivity(intent)

        }

        Im_exit?.setOnClickListener {
             dialogExite = Dialog_Exit(this, this)
            dialogExite?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogExite?.window?.setGravity(Gravity.NO_GRAVITY)
            runOnUiThread {
                dialogExite?.show()
            }
        }


        Im_back?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


       wallet?.setOnClickListener {
            val intent = Intent(this, WalletUserActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
            super.onResume()
            val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            registerReceiver(networkChangeReceiver, intentFilter)

            hand.postDelayed(Runnable { //do your function;
                hand.postDelayed(runnable!!, apiDelayed.toLong())
                ShowProfile()
//            if (mainViewModel.isRunning.value == true) {
//                startUsageUpdates()
//            }
            }.also { runnable = it }, apiDelayed.toLong())

        }

    override fun onPause() {
            super.onPause()
            unregisterReceiver(networkChangeReceiver)
            hand.removeCallbacks(runnable!!)
        }

    override fun onDestroy() {
        dialogExite?.dismiss()
        super.onDestroy()
    }
}