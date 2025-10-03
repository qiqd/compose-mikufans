package com.mikufans

import Weekly
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.mikufans.ui.page.HistoryRecord
import com.mikufans.ui.page.Index
import com.mikufans.ui.page.Me
import com.mikufans.ui.page.Player
import com.mikufans.ui.page.Subscribe
import com.mikufans.ui.theme.MikufansTheme
import com.mikufans.xmd.miku.entiry.Episode
import java.net.URLDecoder


class MainActivity : ComponentActivity() {
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
  Scaffold(
    topBar = {
      AnimatedVisibility(visible = showNavigationBar, enter = fadeIn(), exit = fadeOut()) {
        TopAppBar(
          title = { Text(title) },
        )
      }
    },
    bottomBar = {
      AnimatedVisibility(visible = showNavigationBar, enter = fadeIn(), exit = fadeOut()) {
        NavigationBar(
          modifier = Modifier.height(100.dp)
        ) {
          screens.forEach { screen ->
            NavigationBarItem(
              icon = { Icon(screen.icon, contentDescription = screen.title) },
              selected = currentDestination == screen.route,
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
    NavHost(
      enterTransition = { if (showNavigationBar) fadeIn() else EnterTransition.None },
      exitTransition = { if (showNavigationBar) fadeOut() else ExitTransition.None },
      navController = navController,
      startDestination = BottomNavigationItem.Index.route,
      modifier = Modifier.padding(if (showNavigationBar) innerPadding else PaddingValues(0.dp))

    ) {
      composable(BottomNavigationItem.Index.route) { Index(navController) }
      composable(BottomNavigationItem.Weekly.route) { Weekly(navController) }
      composable(BottomNavigationItem.Subscribe.route) { Subscribe(navController) }
      composable(
        route = BottomNavigationItem.Me.route,
        enterTransition = { if (showNavigationBar) fadeIn() else EnterTransition.None },
        exitTransition = { if (showNavigationBar) fadeOut() else ExitTransition.None }
      ) { Me(navController) }
      composable(Navigation.ANIME_DETAIL + "/{animeId}/{animeName}") { backStackEntry ->
        val animeId = backStackEntry.arguments?.getString("animeId") ?: "0"
        val animeName = backStackEntry.arguments?.getString("animeName") ?: ""
        AnimeDetail(animeId.toInt(), animeName, navController)
      }
      composable(Navigation.ANIME_PLAYER + "/{animeId}/{episodes}") { backStackEntry ->
        val animeId = backStackEntry.arguments?.getString("animeId") ?: "0"
        var episodes = backStackEntry.arguments?.getString("episodes") ?: ""
        episodes = URLDecoder.decode(episodes, "UTF-8")
        val source = JSON.parseArray(episodes, Episode::class.java)
        Player(animeId.toInt(), navController, source, activity)
      }
      composable(
        route = Navigation.HISTORY,
      ) {
        HistoryRecord(navController)
      }
      composable(
        route = Navigation.ABOUT,
      ) {
        About(navController)
      }
    }
  }
}
