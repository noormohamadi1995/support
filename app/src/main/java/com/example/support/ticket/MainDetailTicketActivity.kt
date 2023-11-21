package com.example.support.ticket

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.support.R
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_model.*
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.pnikosis.materialishprogress.ProgressWheel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainDetailTicketActivity : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    var ticketDetailAdapter: TicketDetailAdapter? = null
    var progressWheel: ProgressWheel? = null
    var etComment: EditText? = null
    var btnUploadImage: Button? = null
    var txtCheckAdminStatus : TextView? = null
    private var fileUri : Uri? = null
    private var dialog : ProgressDialog? = null
    private var ticketType : String? = null

    private val handler = Handler(Looper.getMainLooper())
    var isChange = false

    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (isChange) {
                checkChange(ticketId, 1)
            } else {
                checkChange(ticketId, 0)
            }
            setupRecyclerview(progressWheel, 1)
            Log.i("run_ticket", "refreshRunnable")
            checkAdminOnline()
            handler.postDelayed(this, 5000)
        }
    }

    private val imageCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.data?.let {
                        Toast.makeText(
                            this,
                            "لطفا متن خود را بنویسید",
                            Toast.LENGTH_SHORT
                        ).show()
                        fileUri = it
                    }
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onPause() {
        super.onPause()
        Log.i("run_ticket", "onPause")
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerview(progressWheel, 1)
        handler.postDelayed(refreshRunnable, 5000)
    }

    fun checkChange(_ticket_id: Int, _is_refresh: Int) {
        val routes = ApiPanelController.client.create(PanelRoutes::class.java)

        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val toekn = sharedPreferences.getString("token", null)

        val call = routes.checkTicketMessage(toekn, _ticket_id, _is_refresh)

        call?.enqueue(object : Callback<TicketChangeMessageModel?> {
            override fun onResponse(
                call: Call<TicketChangeMessageModel?>,
                response: Response<TicketChangeMessageModel?>
            ) {
                Log.i("checkChange", Gson().toJson(response.body()))

                if (response.body() != null)
                    isChange = response.body()?.is_change == true
            }

            override fun onFailure(call: Call<TicketChangeMessageModel?>, t: Throwable) {
            }
        })

    }

    var ticketId = 0


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_ticket_detail)
        dialog = ProgressDialog(this)
        dialog?.setCancelable(false)
        dialog?.setMessage("درحال ارسال..")

        ticketId = intent.getIntExtra("ticketId", 0)
        txtCheckAdminStatus = findViewById(R.id.te_online)

        val btnAddTicket = findViewById<Button>(R.id.btn_addTicket)
        etComment = findViewById(R.id.ET_Comment)
        btnUploadImage = findViewById(R.id.btn_upload_image)
        progressWheel = findViewById(R.id.progress_wheel)
        val btnticketType = findViewById<Button>(R.id.btn_ticket_type)

        etComment?.doAfterTextChanged {
            btnAddTicket.visibility = if (it.toString().trim().isNotEmpty()) View.VISIBLE else View.GONE
        }

        btnUploadImage?.setOnClickListener {
            val imagePicker = ImagePicker.with(this)
            imagePicker.galleryOnly()
                .crop()
                .createIntent { intent ->
                    imageCaptureLauncher.launch(intent)
                }
        }

        btnticketType.setOnClickListener {
            val dialog = TicketTypeBottomSheetDialog()
            dialog.arguments?.putString(TicketTypeBottomSheetDialog.TYPE,ticketType)
            dialog.show(supportFragmentManager,"ticket_type_dialog")
        }

        supportFragmentManager
            .setFragmentResultListener(TicketTypeBottomSheetDialog.TICKET_TYPE_REQUEST,this) {requestKey, bundle ->
            if (requestKey == TicketTypeBottomSheetDialog.TICKET_TYPE_REQUEST){
                ticketType = bundle.getString(TicketTypeBottomSheetDialog.TYPE)
                val title = bundle.getString(TicketTypeBottomSheetDialog.TITLE)
                Toast.makeText(this, "نوع تیکت:$title",Toast.LENGTH_SHORT).show()
            }
        }

        setupRecyclerview(progressWheel, 1)

        btnAddTicket.setOnClickListener {
                val routes = ApiPanelController.client.create(PanelRoutes::class.java)
                val sharedPreferences = getSharedPreferences("Shtoken", 0)
                val status = when (ticketType) {
                    TicketType.OPEN.type ->  "waiting_admin"
                    TicketType.CLOSE.type ->  "done"
                    else -> ""
                }

                val image = fileUri?.let {
                    val file = it.toFile()
                    val requestFile = it.toFile().asRequestBody(contentResolver.getType(it)?.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", file.name, requestFile)
                }

                val token = sharedPreferences.getString("token",null)?.toRequestBody("text/*".toMediaTypeOrNull())

                val ticketStatus = status.takeIf { it.isNotEmpty() }?.toRequestBody("text/*".toMediaTypeOrNull())

                val comment = etComment?.text?.toString()?.toRequestBody("text/*".toMediaTypeOrNull())

                val ticketId = ticketId.toString().toRequestBody("text/*".toMediaTypeOrNull())

            if (image == null && comment == null){
                Toast.makeText(
                    this,
                    "وارد کردن عکس یا متن پیام الزامی است",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            dialog?.show()
                val call = routes
                    .addMessage(
                        token = token ,
                        ticket_status = ticketStatus,
                        comment = comment,
                        image = image,
                        ticket_id = ticketId
                    )

                call?.enqueue(object : Callback<AddTicketModel?> {
                    override fun onResponse(
                        call: Call<AddTicketModel?>,
                        response: Response<AddTicketModel?>
                    ) {
                        setupRecyclerview(progressWheel, 1)

                        dialog?.dismiss()
                        etComment?.text?.clear()
                        fileUri = null
                        ticketType = null

                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@MainDetailTicketActivity,
                                response.body()?.message ?: "",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainDetailTicketActivity,
                                "پیام نوشته نشده...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<AddTicketModel?>, t: Throwable) {
                        dialog?.dismiss()
                        Log.i("ss", t.message.toString())
                    }
                })
        }
    }

    fun setupRecyclerview(progressWheel: ProgressWheel?, is_visited: Int?) {
        recyclerView = findViewById(R.id.recycler_view_ticket_messages)

        val routes = ApiPanelController.client.create(PanelRoutes::class.java)

        val sharedPreferences = getSharedPreferences("Shtoken", 0)
        val toekn = sharedPreferences.getString("token", null)

        val call = routes.getTicketMessages(toekn, ticketId, is_visited)

        call.enqueue(object : Callback<TicketMessagesModel?> {
            override fun onResponse(
                call: Call<TicketMessagesModel?>,
                response: Response<TicketMessagesModel?>
            ) {
                progressWheel?.visibility = View.GONE

                if (response.isSuccessful && response.code() == 200){
                    val ticketMessages = response.body()

                    if (ticketMessages != null) {
                        val layoutManager = LinearLayoutManager(this@MainDetailTicketActivity)

                        layoutManager.scrollToPosition(ticketMessages.ticket_messages.size - 1);
                        recyclerView?.layoutManager = layoutManager

                        ticketDetailAdapter = TicketDetailAdapter(
                            this@MainDetailTicketActivity,
                            ticketMessages.ticket_messages
                        )
                        /*if (ticketMessages.ticket_messages.isNotEmpty()) {
                            val linearLayout = findViewById<LinearLayout>(R.id.linear_recycler)
                            val layoutParams = linearLayout.layoutParams as LinearLayout.LayoutParams
                            layoutParams.weight = 0.6f // set the weight to 2
                            linearLayout.layoutParams = layoutParams
                        }*/
                        recyclerView?.adapter = ticketDetailAdapter
                    }
                }
            }

            override fun onFailure(call: Call<TicketMessagesModel?>, t: Throwable) {
                Log.e("ticket_messages","messages",t)
                progressWheel?.visibility = View.GONE
            }
        })
    }

    fun checkAdminOnline(){
        val routes = ApiPanelController.client.create(PanelRoutes::class.java)
        routes.checkAdminOnline().enqueue(object : Callback<OnlineAdminResponse>{
            override fun onResponse(
                call: Call<OnlineAdminResponse>,
                response: Response<OnlineAdminResponse>
            ) {
                if (response.isSuccessful && response.code() == 200){
                   response.body()?.status?.let {
                       txtCheckAdminStatus?.text = if (it) "آنلاین" else "آفلاین"
                       txtCheckAdminStatus?.setTextColor(ContextCompat.getColor(
                           this@MainDetailTicketActivity,
                       if (it) R.color.colorAccentByDark else R.color.Tile_content))
                   }
                }
            }

            override fun onFailure(call: Call<OnlineAdminResponse>, t: Throwable) {
                Log.e("check online","online",t)
            }
        })
    }
}