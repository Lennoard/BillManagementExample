package com.lennoardsilva.androidmobillschallenge.utils

import java.text.DateFormat
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

    @JvmStatic
    fun dateMillisToString(millis: Long, pattern: String = "dd/MM/yyyy HH:mm"): String? {
        return runCatching {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
        }.getOrDefault("Data desconhecida")
    }
}