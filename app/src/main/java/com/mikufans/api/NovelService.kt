package com.mikufans.api

import org.anime.api.NovelApi
import org.anime.entity.meta.SourceWithDelay
import org.anime.parser.HtmlParser

object NovelService {

  val novelApi = NovelApi.SOURCES_WITH_DELAY
  var targetApi: List<SourceWithDelay<out HtmlParser>>? = null
}