package com.mikufans.ui.nav

import androidx.navigation.NavController
import java.net.URLEncoder

object Navigation {
  const val ANIME_DETAIL = "animeDetail"
  const val ANIME_PLAYER = "animePlayer"
  const val HISTORY = "history"
  const val ABOUT = "about"
  const val FULL_SEARCH = "fullSearch"
  fun navigateToAnimeDetail(
    navController: NavController,
    animeId: String? = null,
    animeSubId: String,
    animeName: String
  ) {
    val animeId = URLEncoder.encode(animeId, "UTF-8")
    val animeSubId = URLEncoder.encode(animeSubId, "UTF-8")
    navController.navigate("$ANIME_DETAIL/$animeId/$animeSubId/$animeName") {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToAnimePlayer(
    navController: NavController,
    animeId: String,
    animeSubId: String,
    episodeId: String
  ) {
    val animeSubId = URLEncoder.encode(animeSubId, "UTF-8")
    val animeId = URLEncoder.encode(animeSubId, "UTF-8")
    val eps = URLEncoder.encode(episodeId, "UTF-8")
    navController.navigate("$ANIME_PLAYER/$animeId/$animeSubId/$eps") {
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
}