package com.mikufans.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mikufans.xmd.access.AAFunAccessPoint
import com.mikufans.xmd.access.GiligiliAccessPoint
import com.mikufans.xmd.miku.entiry.WebsiteDelay
import com.mikufans.xmd.util.HttpUtil

class NetworkViewModel : ViewModel() {
  private val _websiteDelays = MutableLiveData<List<WebsiteDelay>>()
  val websiteDelays: LiveData<List<WebsiteDelay>> = _websiteDelays
  private val sources = mapOf(
    "www.aafun.cc" to AAFunAccessPoint(),
    "bgm.girigirilove.com" to GiligiliAccessPoint()
  )

  fun loadWebsiteDelays() {
    Thread {
      val delays = HttpUtil.getDomainDelaysConcurrent(sources.keys.toList())
      delays.sortedBy { it.delay }
      _websiteDelays.postValue(delays)
    }.start()
  }
}
