package com.mikufans.ui.nav

import androidx.navigation.NavController
import java.net.URLEncoder

object Navigation {
  const val ANIME_DETAIL = "animeDetail"
  const val ANIME_PLAYER = "animePlayer"
  const val HISTORY = "history"
  const val ABOUT = "about"
  const val FULL_SEARCH = "fullSearch"
  fun navigateToAnimeDetail(navController: NavController, animeId: String, animeName: String) {
    val encode = URLEncoder.encode(animeId)
    navController.navigate("$ANIME_DETAIL/$encode/$animeName") {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToAnimePlayer(navController: NavController, animeId: String, episodeId: String) {
    val encode = URLEncoder.encode(animeId, "UTF-8")
    val eps = URLEncoder.encode(episodeId)
    navController.navigate("$ANIME_PLAYER/$encode/$eps") {
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