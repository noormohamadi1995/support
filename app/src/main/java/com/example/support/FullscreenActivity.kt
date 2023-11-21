package com.example.support

import android.content.Intent
import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.support.Dialog.ProgressDialog
import com.permissionx.guolindev.PermissionX
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Logger
import java.io.File


class FullscreenActivity : AppCompatActivity() {
    private var mProgress: ProgressDialog? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private var fetch : Fetch? = null
    private var btnDownload : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen)
        val imageview = findViewById<ImageView>(R.id.fullScreenImageView)
         btnDownload = findViewById(R.id.btnDownload)
        intent.extras?.let {

            val imageUrl = it.getString("image_url")
            imageUrl?.let {
                val fileName = URLUtil.guessFileName(it, null, null)
                val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + fileName
                btnDownload?.visibility = if (File(file).exists()) View.GONE else View.VISIBLE
                Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageview)

                btnDownload?.setOnClickListener {
                    val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        listOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.READ_MEDIA_AUDIO,
                            android.Manifest.permission.READ_MEDIA_VIDEO
                        )
                    }else listOf(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                       android. Manifest.permission.READ_EXTERNAL_STORAGE
                    )

                    PermissionX
                        .init(this@FullscreenActivity)
                        .permissions(list)
                        .request { allGranted, _, _ ->
                            if (allGranted){
                                download(imageUrl,file)
                            }else{
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                    if (Environment.isExternalStorageManager().not()) {
                                        launcher.launch(
                                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                                addCategory("android.intent.category.DEFAULT")
                                                data = (Uri.parse(
                                                    String.format(
                                                        "package:%s",
                                                        applicationContext.packageName
                                                    )
                                                ))
                                            }
                                        )
                                    }
                            }
                        }
                }
            }
        }
    }

    private fun download(fileUrl : String,file : String){
        val configuration = FetchConfiguration.Builder(this)
            .setDownloadConcurrentLimit(10)
            .setProgressReportingInterval(200)
            .setLogger(object : Logger {
                override var enabled: Boolean = true

                override fun d(message: String) {
                    Log.d("downloader",message)
                }

                override fun d(message: String, throwable: Throwable) {
                    Log.e("downloader",message, throwable)
                }

                override fun e(message: String) {
                    Log.e("downloader",message)
                }

                override fun e(message: String, throwable: Throwable) {
                    Log.e("downloader",message,throwable)
                }
            })
            .enableLogging(true)
            .enableAutoStart(false)
            .enableFileExistChecks(true)
            .build()
         fetch = Fetch.getInstance(configuration)
        fetch?.removeAll()

        val request = Request(fileUrl, file)
        request.priority = Priority.HIGH
        request.networkType = NetworkType.ALL
        fetch?.enqueue(request,
            { updatedRequest: Request? ->
            },
            {
                Log.e("error","error",it.throwable)
              hideProgressDialog()
            }
        )
        val listener = object : FetchListener {
            override fun onAdded(download: Download) {
                Log.e("onAdded()",download.toString())
            }

            override fun onCancelled(download: Download) = Unit

            override fun onCompleted(download: Download) {
                Log.e("onComplete()",download.toString())
                if (request.id == download.id) {
                    hideProgressDialog()
                    btnDownload?.visibility = if (File(file).exists()) View.GONE else View.VISIBLE
                    Toast.makeText(this@FullscreenActivity,"دانلود با موفقیت اانجام شد",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onDeleted(download: Download) = Unit

            override fun onDownloadBlockUpdated(
                download: Download,
                downloadBlock: DownloadBlock,
                totalBlocks: Int
            ) = Unit

            override fun onError(
                download: Download,
                error: Error,
                throwable: Throwable?
            ) {
                Log.e("onError()","error",throwable)
                hideProgressDialog()
            }

            override fun onPaused(download: Download) = Unit

            override fun onProgress(
                download: Download,
                etaInMilliSeconds: Long,
                downloadedBytesPerSecond: Long
            ) {
                Log.e("onProgress()",download.toString())
                mProgress?.setProgress(download.progress)
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                Log.e("onQueued()",download.toString())
                showProgressDownloadFile()
            }

            override fun onRemoved(download: Download) = Unit

            override fun onResumed(download: Download) = Unit

            override fun onStarted(
                download: Download,
                downloadBlocks: List<DownloadBlock>,
                totalBlocks: Int
            ) = Unit

            override fun onWaitingNetwork(download: Download) = Unit

        }
        fetch?.addListener(
            notify = true,
            autoStart = true,
            listener = listener
        )
    }

    private fun showProgressDownloadFile() {
       runOnUiThread {
           if (mProgress == null){
               mProgress = ProgressDialog{
                   fetch?.removeAll()
                   hideProgressDialog()
               }
               mProgress?.isCancelable = false
           }
           if (supportFragmentManager.isDestroyed.not())
                mProgress?.show(supportFragmentManager, "progress_dialog")
        }
    }

    private fun hideProgressDialog() {
        runOnUiThread {
            mProgress?.dismiss()
        }
    }
}