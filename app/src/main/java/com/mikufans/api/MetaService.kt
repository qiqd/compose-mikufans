package com.mikufans.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.anime.entity.animation.Animation
import org.anime.entity.animation.Staff
import org.anime.entity.base.Detail
import org.anime.meta.impl.Bangumi
import org.anime.parser.impl.meta.Douban

object MetaService {
  val douban = Douban()
  val bangumi = Bangumi()

  suspend fun fetchSearchSync(
    keyword: String,
    exceptionHandler: (Exception) -> Unit
  ): List<Animation> {
    try {
      return withContext(Dispatchers.IO) {
        douban.fetchSearchSync(keyword, 1, 10)
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
        douban.fetchDetailSync(mediaId)
      }
    } catch (e: Exception) {
      exceptionHandler(e)
    }
    return null
  }

  suspend fun fetchStaffSync(
    mediaId: String,
    exceptionHandler: (Exception) -> Unit
  ): Staff {
    try {
      return withContext(Dispatchers.IO) {
        douban.fetchStaffSync(mediaId)
      }
    } catch (e: Exception) {
      exceptionHandler(e)
    }
    return Staff()
  }
}