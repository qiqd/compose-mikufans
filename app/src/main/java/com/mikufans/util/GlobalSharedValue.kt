package com.mikufans.util

import org.anime.entity.animation.Animation
import org.anime.entity.base.Detail
import org.anime.entity.base.Source

object GlobalSharedValue {
  var episodes = mutableListOf<String>()
  var sources = mutableListOf<Source>()
  var currentEpisodeId = ""
  var animationDetail: Detail<Animation>? = null
}