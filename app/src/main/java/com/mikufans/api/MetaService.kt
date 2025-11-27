package com.mikufans.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.anime.entity.animation.Staff
import org.anime.entity.base.Detail
import org.anime.entity.base.Media
import org.anime.meta.impl.Bangumi
import org.anime.parser.impl.meta.Douban

object MetaService {
  val douban = Douban()
  val bangumi = Bangumi()

  suspend fun fetchSearchSync(
    keyword: String, exceptionHandler: (Exception) -> Unit
  ): List<Media> {
    return withContext(Dispatchers.IO) {
      douban.fetchSearchSync(keyword, 1, 10) {
        Log.e("MetaService", "fetchSearchSync: $it")
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchDetailSync(
    mediaId: String, exceptionHandler: (Exception) -> Unit
  ): Detail? {
    Thread.sleep(500L)
    return withContext(Dispatchers.IO) {
      douban.fetchDetailSync(mediaId) {
        Log.e("MetaService", "fetchDetailSync: $it")
        exceptionHandler(it)
      }
    }
  }

  suspend fun fetchStaffSync(
    mediaId: String, exceptionHandler: (Exception) -> Unit
  ): Staff? {
    Thread.sleep(500L)
    return withContext(Dispatchers.IO) {
      douban.fetchStaffSync(mediaId) {
        Log.e("MetaService", "fetchStaffSync: $it")
        exceptionHandler(it)
      }
    }
  }


}

