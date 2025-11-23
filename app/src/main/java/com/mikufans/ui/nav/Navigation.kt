package com.mikufans.ui.nav

import androidx.navigation.NavController
import com.mikufans.util.GlobalSharedValue
import java.net.URLEncoder

object Navigation {
  const val DETAIL = "Detail"
  const val PLAYER = "Player"
  const val HISTORY = "history"
  const val ABOUT = "about"
  const val FULL_SEARCH = "fullSearch"
  const val SETTING = "setting"
  const val LOGIN = "login"
  const val TYPE_ANIMATION = "animation"
  const val TYPE_COMIC = "comic"
  const val TYPE_NOVEL = "novel"
  fun navigateToIndex(navController: NavController) {
    navController.navigate(BottomNavigationItem.Index.route)
  }

  fun navigateToDetail(
    id: String,
    title: String,
    type: String = TYPE_ANIMATION,
    navController: NavController,
  ) {
    val enCodeId = URLEncoder.encode(id, "UTF-8")
    val enCodeTitle = URLEncoder.encode(title, "UTF-8")
    navController.navigate("$DETAIL/$type/$enCodeId/$enCodeTitle") {
      launchSingleTop = true
      restoreState = true
    }
  }

  fun navigateToPlayer(
    id: String,
    subId: String,
    title: String,
    navController: NavController,
    episodes: List<String> = emptyList()
  ) {
    val enCodeId = URLEncoder.encode(id, "UTF-8")
    val enCodeSubId = URLEncoder.encode(subId, "UTF-8")
    val enCodeTitle = URLEncoder.encode(title, "UTF-8")
    GlobalSharedValue.episodes = episodes.toMutableList()
    navController.navigate("$PLAYER/$enCodeId/$enCodeSubId/$enCodeTitle") {
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