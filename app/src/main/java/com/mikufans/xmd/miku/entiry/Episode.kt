package com.mikufans.xmd.miku.entiry

import kotlinx.serialization.Serializable

@Serializable
data class Episode(
  var id: String? = null,
  var videoId: String? = null,
  var title: String? = null,
  var episodeNumber: String? = null,
  var playUrl: String? = null,
  var releaseTime: String? = null,
  var duration: Long? = null
)