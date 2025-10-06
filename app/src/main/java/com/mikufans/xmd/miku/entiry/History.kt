package com.mikufans.xmd.miku.entiry

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
  var time: Long? = null
) : Serializable
