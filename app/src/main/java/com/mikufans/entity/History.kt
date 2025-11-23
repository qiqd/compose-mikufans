package com.mikufans.entity

import java.io.Serializable

data class History(
  var id: String? = null,
  var subId: String? = null,
  var isLove: Boolean = false,
  var videoUrl: String? = null,
  var name: String? = null,
  var nameCn: String? = null,
  var cover: String? = null,
  var episodeId: String? = null,
  var episodeIndex: Int? = null,
  var position: Long? = null,
  /**
   * 播放时间，单位毫秒
   */
  var time: Long? = null,
  var sourceIndex: Int? = null,
  var serviceName: String? = null,
) : Serializable