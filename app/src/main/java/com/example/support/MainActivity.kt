package com.example.support

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.support.Dialog.Dialog_Meassge
import com.example.support.Panel.ListPrice.Activity_Price
import com.example.support.Panel.Panel_user
import com.example.support.new_api.ApiController.client
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_api.Routes
import com.example.support.new_model.CheckUserModel
import com.example.support.new_model.TicketsModel
import com.example.support.notification.Configuration.Companion.TOPIC_GLOBAL
import com.example.support.ticket.MainTicketActivity
import com.example.support.update.UpdateApplication
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.example.support.new_model.DataModel
import com.example.support.viewpager2.VideosActivity
import com.example.support.viewpager2.video.VideoListActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    var tvSupport: Button? = null
    var tvUnReadMessage: TextView? = null
    private var mCard : CardView? = null
    private var showProfile : ImageView? = null
    private var btn_open : Button? = null
    var fristrun: SharedPreferences? = null

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("Shtoken", 0)
    }

    private val refreshRunnable = object : Runnable {
        override fun run() {
            val checkUser = sharedPreferences.getString("token", null)
            if (checkUser != null) {
                unReadMessages()
            }
            handler.postDelayed(this, 2000)
        }
    }

    private val handler = Handler(Looper.getMainLooper())

    var check = 0
    var checkesh = 0


    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chectoken()
        Checkuser()
        tvSupport = findViewById<View>(R.id.Tv_support) as Button
        tvUnReadMessage = findViewById(R.id.tvUnReadMessage)
        mCard = findViewById(R.id.cardview)
        showProfile = findViewById(R.id.btnshowprofile)
        btn_open = findViewById<View>(R.id.fab) as Button

        tvSupport?.setOnClickListener {
            val checkUser = sharedPreferences.getString("token", null)
            if (checkUser == null) {
                val dialogMessage = Dialog_Meassge(this)
                dialogMessage.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogMessage.window?.setGravity(Gravity.NO_GRAVITY)
                dialogMessage.show()
            } else {
                val intent = Intent(this, MainTicketActivity::class.java)
                startActivity(intent)
            }
        }
            val bt_viedot = findViewById<ImageView>(R.id.bt_viedot)
            bt_viedot?.setOnClickListener {
                val intent = Intent(this, VideoListActivity::class.java)
                startActivity(intent)

        }

        btn_open?.setOnClickListener {
            if (check == 1) {
                if (checkesh == 1) {
                    Toast.makeText(this@MainActivity, "اشتراک تون فعال شده", Toast.LENGTH_SHORT).show()
                } else {
                    val intentesh = Intent(this, Activity_Price::class.java)
                    startActivity(intentesh)
                }
            } else {
                val dialog_meassge = Dialog_Meassge(this)
                dialog_meassge.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                dialog_meassge.window?.setGravity(Gravity.NO_GRAVITY)
                dialog_meassge.show()
            }
        }

        val firebase = findViewById<TextView>(R.id.firebaseId)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            firebase.append(it.token)
        }

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_GLOBAL)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Log.d(TAG, "Global topic subscription successful")
                else
                    Log.e(
                        TAG,
                        "Global topic subscription failed. Error: " + task.exception?.localizedMessage
                    )
            }

        client.create(Routes::class.java)
            .getUpdateLinks()
            .enqueue(object : Callback<List<DataModel>> {
            override fun onResponse(
                call: Call<List<DataModel>>,
                response: Response<List<DataModel>>
            ) {
                if (response.isSuccessful && response.code() == 200){
                    if (response.body()?.isNotEmpty() == true){
                        response.body()?.getOrNull(0)?.let {data->
                            Log.d("test", data.toString())
                            val check = data.checkdownload
                            if (check == "1") {
                                fristrun = getSharedPreferences("updateapplication", 0)
                                val checksh = fristrun?.getString("check", null)
                                if (checksh == null) {
                                    val updateApplication = UpdateApplication()
                                    val bundle = Bundle()
                                    bundle.putSerializable("update",data)
                                    updateApplication.arguments = bundle
                                    updateApplication.show(supportFragmentManager,"update_dialog")
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<DataModel>>, t: Throwable) {
                Log.d("test", t.message.toString())
            }
        })

        showProfile?.setOnClickListener {
            val sharedPreferences = getSharedPreferences("Shtoken", 0)
            val checkUser = sharedPreferences.getString("token", null)
            if (checkUser == null) {
                val dialogMessage = Dialog_Meassge(this)
                dialogMessage.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                dialogMessage.window?.setGravity(Gravity.NO_GRAVITY)
                dialogMessage.show()
            } else {
                val intent = Intent(this, Panel_user::class.java)
                startActivity(intent)
            }
        }

        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val profile = sharedPreferences.getString("profile", "")
        if (profile?.isNotEmpty() == true) {
            Log.i("nav_profile", "if runned")
            Glide.with(this)
                .load(profile)
                .circleCrop()
                .error(R.drawable.ic_waiting)
                .placeholder(R.drawable.ic_customer)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        Log.i("nav_profile", "onResourceReady")
                        showProfile?.setImageDrawable(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        showProfile?.setImageDrawable(placeholder)

                        Log.i("nav_profile", "onLoadCleared")
                    }
                })

        }

        supportFragmentManager.setFragmentResultListener("update_application",this){request,bundle->
           Log.e("request",request)
            if (request == "update_application"){
               val file = bundle.getString("file_path")
                Log.e("file", file ?: "")
             AlertDialog.Builder(this)
                   .setMessage("می خواهید این برنامه را نصب کنید؟")
                   .setPositiveButton("بله"
                   ) { p0, _ ->
                       p0.dismiss()
                       try {
                           Log.e("uri",file?.getUriFromFile(this)?.toString() ?: "")
                           file?.getUriFromFile(this)?.let{
                               val mime = contentResolver.getType(it)
                               Log.e("mime",mime ?: "")
                               val intent = Intent(Intent.ACTION_VIEW)
                               intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                               intent.setDataAndType(it, mime)
                               startActivity(intent)
                           }
                       }catch (e : Exception){
                           e.printStackTrace()
                       }
                   }
                   .setNegativeButton("خیر"){ p0, _ ->
                       p0.dismiss()
                   }
                   .setCancelable(false)
                 .show()
           }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val profile = sharedPreferences.getString("profile", "")
        if (!profile.equals("")) {
            Log.i("nav_profile", "if runned")
            val menuItem = menu.findItem(R.id.action_settings)
            Glide.with(this)
                .load(profile)
                .circleCrop()
                .error(R.drawable.ic_waiting)
                .placeholder(R.drawable.ic_customer)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        Log.i("nav_profile", "onResourceReady")
                        menuItem?.icon = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        menuItem?.icon = placeholder

                        Log.i("nav_profile", "onLoadCleared")
                    }
                }
                )
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val sharedPreferences = getSharedPreferences("Shtoken", 0)
                val checkUser = sharedPreferences.getString("token", null)
                if (checkUser == null) {
                    val dialogMessage = Dialog_Meassge(this)
                    dialogMessage.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
                    dialogMessage.window?.setGravity(Gravity.NO_GRAVITY)
                    dialogMessage.show()
                } else {
                    val intent = Intent(this, Panel_user::class.java)
                    startActivity(intent)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun unReadMessages() {
        val checkUser = sharedPreferences.getString("token", null)
        val route = ApiPanelController.client.create(PanelRoutes::class.java)
        route.getTickets(checkUser).enqueue(object : Callback<TicketsModel> {
            override fun onResponse(call: Call<TicketsModel>, response: Response<TicketsModel>) {
                if (response.isSuccessful && response.code() == 200) {
                    response.body()?.tickets?.let {
                        val unreadCount = it.filter { it.status.toString() == "waiting_customer" }
                            .takeIf { it.isNotEmpty() }
                            ?.map { it.unread_count }
                            ?.maxBy { it ?: 0 }
                            ?.takeIf { it > 0 }
                        if (unreadCount != null) {
                            mCard?.visibility = View.VISIBLE
                            tvUnReadMessage?.text = "تعداد پیام های خوانده نشده: ".plus(unreadCount)
                        } else {
                            mCard?.visibility = View.GONE
                            tvUnReadMessage?.text = ""
                        }
                    }
                }
            }

            override fun onFailure(call: Call<TicketsModel>, t: Throwable) {
            }

        })






    }

    fun chectoken() {
        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val toekn = sharedPreferences.getString("token", null)
        if (toekn != null) {
            ShwoInformation_esh(toekn)
        } else {
            checkesh = 0
        }
    }

    fun ShwoInformation_esh(strtoken: String?) {
            val routes = client.create(Routes::class.java)
            val call = routes.checkUser(strtoken)
            call.enqueue(object : Callback<CheckUserModel?> {
                override fun onResponse(
                    call: Call<CheckUserModel?>,
                    response: Response<CheckUserModel?>
                ) {
                    val checkUserModel = response.body()
                    val checkerro = checkUserModel!!.status
                    if (checkerro == "error") {
                        checkesh = 0
                        btn_open?.text = "برای استفاده اشتراک تهیه کنید"



                    } else {

                        checkesh = 1
                   btn_open?.text = "اشتراک شما فعال می باشد"



                    }
                }

                override fun onFailure(call: Call<CheckUserModel?>, t: Throwable) {
                    checkesh = 0
                }
            })
        }

    fun Checkuser() {
            val sharedPreferences = getSharedPreferences("Shtoken", 0)
            val toekn = sharedPreferences.getString("token", null)
            if (toekn == null) {
                check = 0
                btn_open?.text = "برای استفاده اشتراک تهیه کنید"

            } else {

            check = 1
            btn_open?.text = "اشتراک شما فعال می باشد"

            }


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
        handler.postDelayed(refreshRunnable, 2000)
        super.onResume()
    }

}