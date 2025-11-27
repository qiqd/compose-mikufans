package com.mikufans.entity

import com.mikufans.ui.nav.Navigation
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
   * 最后一次播放时间，单位毫秒
   */
  var time: Long = 0,
  var sourceIndex: Int? = null,
  var serviceName: String? = null,
  var mediaType: String = Navigation.TYPE_ANIMATION,
  var author: String? = null,
  var pageIndex: Int = 0,
  var chapterName: String? = null,
) : Serializable