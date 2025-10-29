package com.mikufans.ui.nav

import androidx.navigation.NavController
import com.alibaba.fastjson.JSON
import com.mikufans.xmd.miku.entiry.Anime
import java.net.URLEncoder

object Navigation {
  const val ANIME_DETAIL = "animeDetail"
  const val ANIME_PLAYER = "animePlayer"
  const val HISTORY = "history"
  const val ABOUT = "about"
  const val FULL_SEARCH = "fullSearch"
  const val SETTING = "setting"
  const val LOGIN = "login"
  fun navigateToAnimeDetail(
    navController: NavController,
    animeId: String? = "",
    playSource: String = "",
    animeSubId: String,
    animeName: String
  ) {
    val enCodeAnimeId = URLEncoder.encode(animeId, "UTF-8")
    val enCodeAnimeSubId = URLEncoder.encode(animeSubId, "UTF-8")
    val enCodePlaySource = URLEncoder.encode(playSource, "UTF-8")
    val enCodeAnimeName = URLEncoder.encode(animeName, "UTF-8")
    navController.navigate("$ANIME_DETAIL/$enCodeAnimeId/$enCodeAnimeSubId/$enCodeAnimeName/$enCodePlaySource") {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToAnimePlayer(
    navController: NavController,
    animeId: String,
    animeSubId: String,
    episodeId: String,
    subject: Anime
  ) {
    val animeSubId = URLEncoder.encode(animeSubId, "UTF-8")
    val animeId = URLEncoder.encode(animeId, "UTF-8")
    val eps = URLEncoder.encode(episodeId, "UTF-8")
    val subject = URLEncoder.encode(JSON.toJSONString(subject), "UTF-8")
    navController.navigate("$ANIME_PLAYER/$animeId/$animeSubId/$subject/$eps") {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToHistory(navController: NavController) {
    navController.navigate(HISTORY) {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToAbout(navController: NavController) {
    navController.navigate(ABOUT) {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToFullSearch(navController: NavController, keyword: String) {
    navController.navigate("$FULL_SEARCH/$keyword") {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToSetting(navController: NavController) {
    navController.navigate(SETTING) {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToLogin(navController: NavController, email: String) {
    navController.navigate("$LOGIN/$email") {
      launchSingleTop = true
      restoreState = true
    }
  }
}