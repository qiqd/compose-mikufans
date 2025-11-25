package com.mikufans.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.anime.api.NovelApi
import org.anime.entity.base.Detail
import org.anime.entity.base.ViewInfo
import org.anime.entity.novel.Novel
import org.anime.parser.AbstractNovelParser

object NovelService {
  var novelApi: AbstractNovelParser = NovelApi.SOURCES_WITH_DELAY[0].htmlParser

  suspend fun fetchSearchSync(
    keyword: String, exceptionHandler: (Exception) -> Unit
  ): List<Novel> {
    novelApi = NovelApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      novelApi.fetchSearchSync(keyword, 1, 10) {
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchDetailSync(
    mediaId: String, exceptionHandler: (Exception) -> Unit
  ): Detail<Novel>? {
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