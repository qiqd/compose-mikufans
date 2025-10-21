package com.mikufans.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object RelativeTime {
  fun relativeTime(millis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = millis }

    val yearNow = now[Calendar.YEAR]
    val yearTarget = target[Calendar.YEAR]
    val dayNow = now[Calendar.DAY_OF_YEAR]
    val dayTarget = target[Calendar.DAY_OF_YEAR]

    return when {
      yearTarget != yearNow -> "${yearTarget}年"
      dayNow == dayTarget -> "今天 ${
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(
          Date(
            millis
          )
        )
      }"

      dayNow - dayTarget == 1 -> "昨天 ${
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(
          Date(
            millis
          )
        )
      }"

      now.timeInMillis - millis < 30L * 24 * 60 * 60 * 1000 -> // 30 天内
        "${(now.timeInMillis - millis) / (24 * 60 * 60 * 1000)} 天前"

      else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(millis))
    }
  }

  fun formatTime(ms: Long): String {
    if (ms <= 0) return "00:00"
    val s = (ms / 1000).toInt()
    val m = s / 60
    val r = s % 60
    return String.format(Locale.getDefault(), "%02d:%02d", m, r)
  }
}