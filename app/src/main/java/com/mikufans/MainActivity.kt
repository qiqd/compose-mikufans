package com.mikufans

import Weekly
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.mikufans.ui.page.About
import com.mikufans.ui.page.AnimeDetail
import com.mikufans.ui.page.FullSearch
import com.mikufans.ui.page.HistoryRecord
import com.mikufans.ui.page.Index
import com.mikufans.ui.page.Me
import com.mikufans.ui.page.Player
import com.mikufans.ui.page.Subscribe
import com.mikufans.ui.theme.MikufansTheme
import com.mikufans.xmd.miku.entiry.Episode
import com.mikufans.xmd.util.SourceUtil
import java.net.URLDecoder


class MainActivity : ComponentActivity() {
  init {
    if (SourceUtil.getSourceWithDelay().isEmpty()) {
      Thread {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(activity: ComponentActivity) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination?.route
  val screens = listOf(
    BottomNavigationItem.Index,
    BottomNavigationItem.Weekly,
    BottomNavigationItem.Subscribe,
    BottomNavigationItem.Me
  )
  val title = when (currentDestination) {
    BottomNavigationItem.Index.route -> "首页"
    BottomNavigationItem.Weekly.route -> "周更表"
    BottomNavigationItem.Subscribe.route -> "追番"
    BottomNavigationItem.Me.route -> "我的"
    else -> ""
  }
  val showNavigationBar = screens.any { it.route == currentDestination }
  val minePageItem = arrayOf(
    Navigation.HISTORY, Navigation.ABOUT
  )
  Scaffold(
    topBar = {
      AnimatedVisibility(
        visible = showNavigationBar,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
      ) {
        TopAppBar(
          title = { Text(title) },
        )
      }
    },
    bottomBar = {
      AnimatedVisibility(
        visible = showNavigationBar,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
      ) {
        NavigationBar {
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
    },
  ) { innerPadding ->
    Box(modifier = Modifier.padding(if (showNavigationBar) innerPadding else PaddingValues(0.dp))) {
      NavHost(
        navController = navController,
        startDestination = BottomNavigationItem.Index.route,
      ) {
        composable(BottomNavigationItem.Index.route) { Index(navController, activity) }
        composable(BottomNavigationItem.Weekly.route) { Weekly(navController, activity) }
        composable(BottomNavigationItem.Subscribe.route) { Subscribe(navController, activity) }
        composable(
          route = BottomNavigationItem.Me.route,
//        enterTransition = { if (showNavigationBar) fadeIn() else EnterTransition.None },
//        exitTransition = { if (showNavigationBar) fadeOut() else ExitTransition.None }
        ) { Me(navController, activity) }
        composable(Navigation.ANIME_DETAIL + "/{animeId}/{animeSubId}/{animeName}") { backStackEntry ->
          var animeId = backStackEntry.arguments?.getString("animeId") ?: "0"
          animeId = URLDecoder.decode(animeId, "UTF-8")
          val animeName = backStackEntry.arguments?.getString("animeName") ?: ""
          val animeSubId = backStackEntry.arguments?.getString("animeSubId") ?: ""
          AnimeDetail(animeId, animeSubId.toInt(), animeName, navController)
        }
        composable(Navigation.ANIME_PLAYER + "/{animeId}/{animeSubId}/{episodes}") { backStackEntry ->
          var animeId = backStackEntry.arguments?.getString("animeId") ?: "0"
          animeId = URLDecoder.decode(animeId, "UTF-8")
          var episodes = backStackEntry.arguments?.getString("episodes") ?: ""
          val animeSubId = backStackEntry.arguments?.getString("animeSubId") ?: ""
          episodes = URLDecoder.decode(episodes, "UTF-8")

          val source = JSON.parseArray(episodes, Episode::class.java)
          Player(animeId, animeSubId, navController, source, activity)
        }
        composable(route = Navigation.HISTORY) { HistoryRecord(navController) }
        composable(route = Navigation.ABOUT) { About(navController) }
        composable(route = Navigation.FULL_SEARCH + "/{keyword}") { backStackEntry ->
          val keyword = backStackEntry.arguments?.getString("keyword") ?: ""
          FullSearch(keyword, navController)
        }
      }
    }

  }
}

