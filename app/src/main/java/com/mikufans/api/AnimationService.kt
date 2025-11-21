package com.mikufans.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.anime.api.AnimationApi
import org.anime.entity.animation.Animation
import org.anime.entity.base.Detail
import org.anime.entity.base.ViewInfo
import org.anime.parser.AbstractAnimationParser

object AnimationService {
  val animationApi: AbstractAnimationParser = AnimationApi.SOURCES_WITH_DELAY[0].htmlParser
  suspend fun fetchSearchSync(
    keyword: String,
    exceptionHandler: (Exception) -> Unit
  ): List<Animation> {
    try {
      return withContext(Dispatchers.IO) {
        animationApi.fetchSearchSync(keyword, 1, 10)
      }
    } catch (e: Exception) {
      exceptionHandler(e)
    }
    return emptyList()
  }

  suspend fun fetchDetailSync(
    mediaId: String,
    exceptionHandler: (Exception) -> Unit
  ): Detail<Animation>? {
    try {
      return withContext(Dispatchers.IO) {
        animationApi.fetchDetailSync(mediaId)
      }
    } catch (e: Exception) {
      exceptionHandler(e)
    }
    return null
  }

  suspend fun fetchViewSync(
    episodeId: String,
    exceptionHandler: (Exception) -> Unit
  ): ViewInfo? {
    try {
      return withContext(Dispatchers.IO) {
        animationApi.fetchViewSync(episodeId)
      }
    } catch (e: Exception) {
      exceptionHandler(e)
    }
    return null
  }
}