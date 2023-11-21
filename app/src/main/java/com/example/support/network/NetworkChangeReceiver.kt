package com.example.support.network

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import com.example.support.R

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            val alertDialog = AlertDialog.Builder(context)
                .setTitle("اینترنت برقرار نیست...")
                .setMessage("لطفا اتصال شبکه خود را بررسی کنید")
                .setIcon(R.drawable.problem)
                .setPositiveButton("تلاش دوباره", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    val newIntent = Intent(context, context::class.java)
                    newIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(newIntent)
                })
                .setCancelable(false)
                .create()
            alertDialog.show()
        }
    }
}
