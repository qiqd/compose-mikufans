package com.mikufans.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo

class Orientation {
  /**
   * 强制设置屏幕方向
   * @param context 任意 Context
   * @param landscape true=横屏 false=竖屏
   */
  fun forceOrientation(context: Context, landscape: Boolean) {
    val activity = context.findActivity() ?: return
    activity.requestedOrientation = if (landscape) {
      ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    } else {
      ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
  }

  /** 从 Context 链里取出 Activity */
  private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }
}