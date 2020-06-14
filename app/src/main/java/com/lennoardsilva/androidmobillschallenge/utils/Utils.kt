package com.lennoardsilva.androidmobillschallenge.utils

import android.net.Uri
import androidx.annotation.DrawableRes
import com.lennoardsilva.androidmobillschallenge.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


object Utils {

    fun randomId(): String {
        val sb = StringBuilder()
        repeat(28) {
            // A = 65; z = 122. The result of the random generation
            // is converted to a char and then appended into the string builder
            sb.append(Random.nextInt(65, 123).toChar())
        }

        var s = sb.toString()

        /*
            Bad chars within 65..122
            Also removing "-" and "_" even though Firebase doesn't
            complain about them
        */
        val badChars = arrayOf("[", "\\", "]", "^", "`", "-", "_")
        badChars.forEach {
            if (s.contains(it)) s = s.replace(it, "A")
        }

        // Replace 1st char if it's a number
        return runCatching {
            // Force exception by first converting to string
            s[0].toString().toInt()
            s.replaceFirst(s[0], '-', true)
        }.getOrDefault(s)
    }

    fun randomUid() : String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    fun parseDate(dateString: String, pattern: String = "dd/MM/yyyy HH:mm"): Date? {
        return runCatching {
            SimpleDateFormat(pattern, Locale.ENGLISH).parse(dateString)
        }.getOrNull()
    }

    fun getFilenameFromUri(uri: Uri): String {
        val file = File(uri.path!!)
        val split = file.path.split(":")

        return split[1]
    }

    @DrawableRes
    fun getIconFromContentType(contentType: String): Int {
        return when (contentType) {
            "application/pdf" -> R.drawable.ic_file_pdf
            "application/xml" -> R.drawable.ic_file_xml

            "application/x-rar-compressed", "application/zip",
            "application/x-7z-compressed", "application/x-tar" -> {
                R.drawable.ic_file_compressed
            }

            "image/png", "image/jpeg", "image/bmp",
            "image/gif", "image/webp" -> {
               R.drawable.ic_file_image
            }

            "application/vnd.oasis.opendocument.text",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"-> {
               R.drawable.ic_file_word
            }

            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"-> {
                R.drawable.ic_file_sheet
            }

            else -> R.drawable.ic_file
        }
    }

    @JvmStatic
    fun dateMillisToString(millis: Long, pattern: String = "dd/MM/yyyy HH:mm"): String? {
        return runCatching {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
        }.getOrDefault("Data desconhecida")
    }
}