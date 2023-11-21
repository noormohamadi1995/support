package com.example.support.update

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Data
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.support.databinding.UpdatelayoutBinding
import com.example.support.getFileSizeFromIntegerValue
import com.example.support.new_api.ApiController
import com.example.support.new_api.Routes
import com.example.support.new_model.DataModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.permissionx.guolindev.PermissionX
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Logger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class UpdateApplication : BottomSheetDialogFragment() {
    private var mBinding : UpdatelayoutBinding? = null
    private var linkdownload: String? = null
    private var Join: String? = null
    private var web_download: String? = null
    private var fristrun: SharedPreferences? = null
    private var fetch : Fetch? = null
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private var currentVersionData : DataModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = UpdatelayoutBinding.inflate(inflater,container,false).apply {
        mBinding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cast()
    }

    private fun cast() = mBinding?.apply {
        (arguments?.get("update") as? DataModel)?.let {data ->
            titleUpdate.text =data.title
            Glide.with(requireActivity())
                .load(data.imageurl)
                .centerCrop()
                .transform(RoundedCorners(20))
                .into(imageviewupdate)
            titlewebview.text = data.content
            btnDownload.text = data.desk
            linkdownload = data.linkdownload
            Join = data.Join
            btnWeb.text =data.text_web_download
            web_download = data.web_download
        }

        btnClose.setOnClickListener {
            fetch?.removeAll()
            dismiss()
        }

        btnDownload.setOnClickListener {
            val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path +
                    File.separator + "support_".plus(System.currentTimeMillis()).plus(".apk")

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
                .init(requireActivity())
                .permissions(list)
                .request { allGranted, _, _ ->
                    if (allGranted){
                        btnDownload.visibility = View.GONE
                        btnWeb.visibility = View.GONE
                        progressLayout.visibility = View.VISIBLE
                        fristrun = requireActivity().getSharedPreferences("updateapplication3", 0)
                        val ed = fristrun?.edit()
                        ed?.putString("check", "yes")
                        ed?.apply()
                        download(fileUrl =linkdownload ?: "", file = file )
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                            if (Environment.isExternalStorageManager().not()) {
                                launcher.launch(
                                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                        addCategory("android.intent.category.DEFAULT")
                                        data = (Uri.parse(
                                            String.format(
                                                "package:%s",
                                                requireActivity().applicationContext.packageName
                                            )
                                        ))
                                    }
                                )
                            }
                    }
                }
        }

        btnWeb.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("$web_download"))
            requireActivity().startActivity(i)
        }

        btnCancel.setOnClickListener {
            showDownloadView()
        }
    }

    private fun download(fileUrl : String,file : String) = mBinding?.apply{
        val configuration = FetchConfiguration.Builder(requireActivity())
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
                showDownloadView()
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
                    requireActivity().supportFragmentManager.setFragmentResult(
                        "update_application",
                        bundleOf(
                            "file_path" to download.file
                        )
                    )
                    dismiss()
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
                showDownloadView()
            }

            override fun onPaused(download: Download) = Unit

            override fun onProgress(
                download: Download,
                etaInMilliSeconds: Long,
                downloadedBytesPerSecond: Long
            ) {
                Log.e("onProgress()",download.toString())
                mProgressBar.progress = download.progress
                txtSize.text = download.downloaded.getFileSizeFromIntegerValue()
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                Log.e("onQueued()",download.toString())
                txtSize.text = "در حال دریافت اطلاعات..."
                titlewebview.text = "در حال دانلود نسخه جدید"
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

    private fun showDownloadView() = mBinding?.apply {
        fetch?.removeAll()
        btnDownload.visibility = View.VISIBLE
        btnWeb.visibility = View.VISIBLE
        progressLayout.visibility = View.GONE
        mProgressBar.progress = 0
        titlewebview.text = currentVersionData?.content
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}
