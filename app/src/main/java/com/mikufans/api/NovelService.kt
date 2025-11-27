package com.mikufans.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.anime.api.NovelApi
import org.anime.entity.base.Detail
import org.anime.entity.base.Media
import org.anime.entity.base.ViewInfo
import org.anime.parser.HtmlParser

object NovelService {
  var novelApi: HtmlParser = NovelApi.SOURCES_WITH_DELAY[0].htmlParser

  suspend fun fetchSearchSync(
    keyword: String, exceptionHandler: (Exception) -> Unit
  ): List<Media> {
    novelApi = NovelApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      novelApi.fetchSearchSync(keyword, 1, 10) {
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchDetailSync(
    mediaId: String, exceptionHandler: (Exception) -> Unit
  ): Detail? {
    novelApi = NovelApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      novelApi.fetchDetailSync(mediaId) {
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchViewSync(
    episodeId: String, exceptionHandler: (Exception) -> Unit
  ): ViewInfo? {
    novelApi = NovelApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      novelApi.fetchViewSync(episodeId) {
        exceptionHandler(it)
      }
    }
  }
}