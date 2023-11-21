package com.example.support

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


fun Long.getFileSizeFromIntegerValue(): String {
    if (this == 0L) {
        return "0"
    } else {
        val limitSize: Long
        try {
            limitSize = this
            return if (limitSize <= 0) {
                "0"
            } else {
                val units = arrayOf(
                    "بایت",
                    "کیلوبایت",
                    "مگابایت",
                    "گیگابایت",
                    "ترابایت"
                )
                val digitGroups = (log10(limitSize.toDouble()) / log10(1024.0)).toInt()
                DecimalFormat("#,##0.#").format(
                    limitSize / 1024.0.pow(digitGroups.toDouble())
                ) + " " + units[digitGroups]
            }
        } catch (e: Exception) {
            return "0"
        }
    }
}

fun String.getUriFromFile(context: Context): Uri {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        Uri.fromFile(File(this))
    } else {
        FileProvider.getUriForFile(
            context,
            "${context.applicationContext.packageName}.provider",
            File(this)
        )
    }
}