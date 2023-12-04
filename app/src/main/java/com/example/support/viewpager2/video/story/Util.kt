package com.example.support.viewpager2.video.story

import android.util.Log
import org.apache.commons.io.FilenameUtils
import java.net.URL


object Util {
    fun checkMimeType(url :String) : String {
        val imageFormat = ArrayList(
            listOf(
                "tif",
                "tiff",
                "bmp",
                "jpg",
                "jpeg",
                "gif",
                "png",
                "eps",
                "raw",
                "cr2",
                "nef",
                "orf",
                "sr2"
            )
        )

        val fileIneed = URL(url)

        val mimeType = FilenameUtils.getExtension(fileIneed.path).ifEmpty {
             if (url.contains("."))
                url.substring(url.lastIndexOf("."))
            else
                url.substring(url.lastIndexOf(".") + 1)
        }
        return if (imageFormat.contains(mimeType))
            "image"
        else
            "video"
    }

    fun formatNumber(number: Int): String {
        val suffix = charArrayOf(' ', 'K', 'M', 'B', 'T', 'P', 'E')
        var value = number.toDouble()
        var index = 0
        while (value >= 1000 && index < suffix.size - 1) {
            value /= 1000
            index++
        }
        return persianToEnglish(String.format("%.1f%s", value, suffix[index]))
            .replace("٫0", "")
            .replace("٫", ",")
    }

    private fun persianToEnglish(persianStr: String): String {
        var result = ""
        var en = '0'
        for (ch in persianStr) {
            en = ch
            when (ch) {
                '۰' -> en = '0'
                '۱' -> en = '1'
                '۲' -> en = '2'
                '۳' -> en = '3'
                '۴' -> en = '4'
                '۵' -> en = '5'
                '۶' -> en = '6'
                '۷' -> en = '7'
                '۸' -> en = '8'
                '۹' -> en = '9'
            }
            result = "${result}$en"
        }
        return result
    }
}