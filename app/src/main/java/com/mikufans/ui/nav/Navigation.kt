package com.mikufans.ui.nav

import androidx.navigation.NavController

object Navigation {
  const val ANIME_DETAIL = "animeDetail"
  const val ANIME_PLAYER = "animePlayer"
  const val HISTORY = "history"
  const val ABOUT = "about"

  fun navigateToAnimeDetail(navController: NavController, animeId: Int, animeName: String) {
    navController.navigate("$ANIME_DETAIL/$animeId/$animeName") {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToAnimePlayer(navController: NavController, animeId: String, episodeId: String) {
    navController.navigate("$ANIME_PLAYER/$animeId/$episodeId") {
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

}