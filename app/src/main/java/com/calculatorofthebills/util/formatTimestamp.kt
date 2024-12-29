package com.calculatorofthebills.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: String): String {
    return try {
        val milliseconds = timestamp.toLong()
        val date = Date(milliseconds)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        "Invalid timestamp"
    }
}