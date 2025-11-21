package com.mikufans.api

import org.anime.api.ComicApi
import org.anime.entity.meta.SourceWithDelay
import org.anime.parser.HtmlParser

object ComicService {
 
  val comicApi = ComicApi.SOURCES_WITH_DELAY

  var targetApi: List<SourceWithDelay<out HtmlParser>>? = null
}