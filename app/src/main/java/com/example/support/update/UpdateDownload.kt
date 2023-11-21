package com.example.support.update

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.database.Cursor
import android.media.RingtoneManager
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.support.R
import com.v2ray.rocket.update.NotificationReceiver
import java.io.File
import java.lang.IllegalStateException

class UpdateDownload {
    @SuppressLint("UnspecifiedImmutableFlag", "SuspiciousIndentation")
    fun downloadFile(activity: Activity, url: String?, fileName: String?) {
        try {
            if (url != null && !url.isEmpty()) {
                val uri = Uri.parse(url)
                activity.registerReceiver(
                    attachmentDownloadCompleteReceive, IntentFilter(
                        DownloadManager.ACTION_DOWNLOAD_COMPLETE
                    )
                )
                val request: DownloadManager.Request = DownloadManager.Request(uri)
                request.setMimeType(getMimeType(uri.toString()))
                request.setTitle(fileName)
                request.setDescription("شروع دانلود...")
                request.setDescription("نسخه جدید رسید")
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                val dm: DownloadManager =
                    activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                val alarmSound: Uri =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val intent = Intent(activity, NotificationReceiver::class.java)
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    activity,
                    System.currentTimeMillis().toInt(),
                    intent,
                    0)


                val n = Notification.Builder(activity)
                    .setContentTitle("شروع بارگیری " + "لطفا منتظر بمانید در حال دانلود ورژن جدید")
                  .setSmallIcon(R.drawable.ic_support)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSound(alarmSound)
                    .build()
                val notificationManager: NotificationManager =
                    activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
              n.flags or Notification.FLAG_AUTO_CANCEL
                notificationManager.notify(0, n)
            }
        } catch (e: IllegalStateException) {
            Toast.makeText(
                activity,
                "لطفا sdk card را چهت ذخیره سازی وارد کنید.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getMimeType(url: String): String? {
        var type: String? = null
        val extension: String = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            val mime: MimeTypeMap = MimeTypeMap.getSingleton()
            type = mime.getMimeTypeFromExtension(extension)
        }
        return type
    }

    var attachmentDownloadCompleteReceive: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.getAction()!!
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                val downloadId: Long = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, 0
                )
//                openDownloadedAttachment(context, downloadId)
            }
        }
    }

    @SuppressLint("Range")
    private fun openDownloadedAttachment(context: Context, downloadId: Long) {
        val downloadManager: DownloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query: DownloadManager.Query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val downloadLocalUri =
                cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
            val downloadMimeType =
                cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE))
            if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL && downloadLocalUri != null) {
                openDownloadedAttachment(context, Uri.parse(downloadLocalUri), downloadMimeType)
            }
        }
        cursor.close()
    }

    private fun openDownloadedAttachment(
        context: Context,
        attachmentUri: Uri,
        attachmentMimeType: String
    ) {
        var attachmentUri: Uri? = attachmentUri
        if (attachmentUri != null) {
            if (ContentResolver.SCHEME_FILE == attachmentUri.scheme) {
                val file = File(attachmentUri.path)
                attachmentUri =
                    FileProvider.getUriForFile(context, "com.freshdesk.helpdesk.provider", file)
            }
            val openAttachmentIntent = Intent(Intent.ACTION_VIEW)
            openAttachmentIntent.setDataAndType(attachmentUri, attachmentMimeType)
            openAttachmentIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(openAttachmentIntent)
            } catch (e: ActivityNotFoundException) {

            }
        }
    }
}