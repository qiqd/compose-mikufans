package com.mikufans.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.anime.api.ComicApi
import org.anime.entity.base.Detail
import org.anime.entity.base.Media
import org.anime.entity.base.ViewInfo
import org.anime.parser.HtmlParser

object ComicService {

  var comicApi: HtmlParser = ComicApi.SOURCES_WITH_DELAY[0].htmlParser

  suspend fun fetchSearchSync(
    keyword: String, exceptionHandler: (Exception) -> Unit
  ): List<Media> {
    comicApi = ComicApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      comicApi.fetchSearchSync(keyword, 1, 10) {
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchDetailSync(
    mediaId: String, exceptionHandler: (Exception) -> Unit
  ): Detail? {
    comicApi = ComicApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      comicApi.fetchDetailSync(mediaId) {
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchViewSync(
    episodeId: String?, exceptionHandler: (Exception) -> Unit
  ): ViewInfo? {
    comicApi = ComicApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      comicApi.fetchViewSync(episodeId) {
        exceptionHandler(it)
      }
    }
  }
}