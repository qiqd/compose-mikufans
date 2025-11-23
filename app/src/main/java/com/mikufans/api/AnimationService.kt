package com.mikufans.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.anime.api.AnimationApi
import org.anime.entity.animation.Animation
import org.anime.entity.base.Detail
import org.anime.entity.base.ViewInfo
import org.anime.parser.AbstractAnimationParser

object AnimationService {
  var animationApi: AbstractAnimationParser = AnimationApi.SOURCES_WITH_DELAY[0].htmlParser
  suspend fun fetchSearchSync(
    keyword: String, exceptionHandler: (Exception) -> Unit
  ): List<Animation> {
    animationApi = AnimationApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      animationApi.fetchSearchSync(keyword, 1, 10) {
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchDetailSync(
    mediaId: String, exceptionHandler: (Exception) -> Unit
  ): Detail<Animation>? {
    animationApi = AnimationApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      animationApi.fetchDetailSync(mediaId) {
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchViewSync(
    episodeId: String, exceptionHandler: (Exception) -> Unit
  ): ViewInfo? {
    animationApi = AnimationApi.SOURCES_WITH_DELAY[0].htmlParser
    return withContext(Dispatchers.IO) {
      animationApi.fetchViewSync(episodeId) {
        exceptionHandler(it)
      }
    }
  }
}

