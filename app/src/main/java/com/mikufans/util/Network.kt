package com.mikufans.util

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Network {
  fun isNetworkAvailable(context: Context): Boolean {
    // 获取ConnectivityManager实例
    val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
// 检查网络连接状态（API 29及以上推荐）
    val networkCapabilities =
      connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    val isConnected =
      networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    return isConnected
  }
}