package com.mikufans.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.Window
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object WindowUtil {
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

  fun hideSystemBar(window: Window, hide: Boolean) {
    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
    if (!hide) {
      // 退出沉浸：显示状态栏+导航栏
      insetsController.show(WindowInsetsCompat.Type.systemBars())
      insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    } else {
      // 沉浸：隐藏状态栏+导航栏
      insetsController.hide(WindowInsetsCompat.Type.systemBars())
      insetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
  }
}