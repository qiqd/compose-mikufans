package com.mikufans.util

import org.anime.entity.animation.Animation
import org.anime.entity.base.Detail
import org.anime.entity.base.Source
import org.anime.entity.comic.Comic
import org.anime.entity.novel.Novel

object GlobalSharedValue {
  var episodes = mutableListOf<String>()
  var sources = mutableListOf<Source>()
  var currentEpisodeId = ""
  var animationDetail: Detail<Animation>? = null
  var comicDetail: Detail<Comic>? = null
  var NovelDetail: Detail<Novel>? = null

}