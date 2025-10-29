package com.mikufans

import WeeklyPage
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alibaba.fastjson.JSON
import com.mikufans.ui.nav.BottomNavigationItem
import com.mikufans.ui.nav.Navigation
import com.mikufans.ui.page.AboutPage
import com.mikufans.ui.page.DetailPage
import com.mikufans.ui.page.FullSearch
import com.mikufans.ui.page.HistoryPage
import com.mikufans.ui.page.IndexPage
import com.mikufans.ui.page.LoginPage
import com.mikufans.ui.page.MinePage
import com.mikufans.ui.page.PlaybackPage
import com.mikufans.ui.page.SettingPage
import com.mikufans.ui.page.SubscribePage
import com.mikufans.ui.theme.MikufansTheme
import com.mikufans.util.LocalStorage
import com.mikufans.util.Network
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.Episode
import com.mikufans.xmd.miku.entiry.History
import com.mikufans.xmd.util.HttpUtil
import com.mikufans.xmd.util.SourceUtil
import java.net.URLDecoder


class MainActivity : ComponentActivity() {
  init {
    if (SourceUtil.getSourceWithDelay().isEmpty()) {
      Thread {
        HttpUtil.applicationContext = this
        SourceUtil.initSources()
      }.start()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MikufansTheme {
        MainScreen(activity = this)
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    if (!(Network.isNetworkAvailable(this))) {
      return
    }
    try {
      Thread {
        LocalStorage.getList(this, "view:history", History::class.java)?.let {
//          UserApi.updateHistory(it)
        }
      }.start()
    } catch (e: Exception) {
      Log.e("保存历史记录失败", e.toString())
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(activity: ComponentActivity) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination?.route
  val baseHorizontalPadding = 8.dp
  val screens = listOf(
    BottomNavigationItem.Index,
    BottomNavigationItem.Weekly,
    BottomNavigationItem.Subscribe,
    BottomNavigationItem.Me
  )
  val showNavigationBar = screens.any { it.route == currentDestination }
  Scaffold(bottomBar = {

    AnimatedVisibility(
      visible = showNavigationBar,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
      NavigationBar(
        modifier = Modifier.height(100.dp)
      ) {
        screens.forEach { screen ->
          NavigationBarItem(
            icon = { Icon(screen.icon, contentDescription = screen.title) },
            selected = currentDestination == screen.route,
            colors = NavigationBarItemDefaults.colors(
            ),
            onClick = {
              navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
                }
                launchSingleTop = true
                restoreState = true
              }
            })
        }

      }
    }

  }) { innerPadding ->
    NavHost(
      modifier = Modifier.padding(
        if (showNavigationBar) PaddingValues(bottom = innerPadding.calculateBottomPadding() / 2) else PaddingValues(
          0.dp
        )
      ),
      navController = navController,
      startDestination = BottomNavigationItem.Index.route,
    ) {
      composable(BottomNavigationItem.Index.route) {
        IndexPage(
          navController,
          activity,
          baseHorizontalPadding,
        )
      }
      composable(BottomNavigationItem.Weekly.route) {
        WeeklyPage(
          navController,
          activity,
          baseHorizontalPadding,
        )
      }
      composable(BottomNavigationItem.Subscribe.route) {
        SubscribePage(
          navController,
          activity,
          baseHorizontalPadding,
        )
      }
      composable(route = BottomNavigationItem.Me.route) {
        MinePage(
          navController,
          activity,
          baseHorizontalPadding,
        )
      }
      composable(Navigation.ANIME_DETAIL + "/{animeId}/{animeSubId}/{animeName}/{source}") { backStackEntry ->
        var animeId = backStackEntry.arguments?.getString("animeId") ?: "0"
        animeId = URLDecoder.decode(animeId, "UTF-8")
        var animeName = backStackEntry.arguments?.getString("animeName") ?: ""
        animeName = URLDecoder.decode(animeName, "UTF-8")
        var animeSubId = backStackEntry.arguments?.getString("animeSubId") ?: ""
        animeSubId = URLDecoder.decode(animeSubId, "UTF-8")
        var source = backStackEntry.arguments?.getString("source") ?: ""
        source = URLDecoder.decode(source, "UTF-8")
        DetailPage(
          animeId,
          source,
          animeSubId.toInt(),
          animeName,
          navController,
          baseHorizontalPadding
        )
      }
      composable(Navigation.ANIME_PLAYER + "/{animeId}/{animeSubId}/{subject}/{episodes}") { backStackEntry ->
        var animeId = backStackEntry.arguments?.getString("animeId") ?: "0"
        animeId = URLDecoder.decode(animeId, "UTF-8")
        var episodes = backStackEntry.arguments?.getString("episodes") ?: ""
        val animeSubId = backStackEntry.arguments?.getString("animeSubId") ?: ""
        episodes = URLDecoder.decode(episodes, "UTF-8")
        val subject =
          URLDecoder.decode(backStackEntry.arguments?.getString("subject") ?: "", "UTF-8")
        val source = JSON.parseArray(episodes, Episode::class.java)
        val parseObject = JSON.parseObject(subject, Anime::class.java)
        PlaybackPage(animeId, animeSubId, navController, source, activity, parseObject)
      }
      composable(route = Navigation.HISTORY) { HistoryPage(navController, baseHorizontalPadding) }
      composable(route = Navigation.ABOUT) { AboutPage(navController, baseHorizontalPadding) }
      composable(route = Navigation.FULL_SEARCH + "/{keyword}") { backStackEntry ->
        val keyword = backStackEntry.arguments?.getString("keyword") ?: ""
        FullSearch(keyword, navController)
      }
      composable(route = Navigation.SETTING) { SettingPage(navController, baseHorizontalPadding) }
      composable(route = Navigation.LOGIN + "/{email}") { navBackStackEntry ->
        val email = navBackStackEntry.arguments?.getString("email") ?: ""
        LoginPage(navController, baseHorizontalPadding, email)
      }


    }
  }
}

