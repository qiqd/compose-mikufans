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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
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
import coil.Coil
import coil.ImageLoader
import com.mikufans.entity.History
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
import okhttp3.OkHttpClient
import java.net.URLDecoder


class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val imageLoader = ImageLoader.Builder(this@MainActivity).okHttpClient {
      OkHttpClient.Builder().addInterceptor { chain ->
        val newRequest = chain.request().newBuilder().addHeader(
          "User-Agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"
        ).build()
        chain.proceed(newRequest)
      }.build()
    }.build()

    Coil.setImageLoader(imageLoader)
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
      popEnterTransition = {
        if (showNavigationBar) {
          fadeIn()
        } else {
          slideInHorizontally(initialOffsetX = { it }) + fadeIn()
        }
      },
      popExitTransition = {
        if (showNavigationBar) {
          fadeOut()
        } else {
          slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        }
      },
      enterTransition = {
        if (showNavigationBar) {
          fadeIn()
        } else {
          slideInHorizontally(initialOffsetX = { it }) + fadeIn()
        }
      },
      exitTransition = {
        if (showNavigationBar) {
          fadeOut()
        } else {
          slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        }
      },
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
      composable(Navigation.DETAIL + "/{type}/{id}/{title}") { backStackEntry ->
        var id = backStackEntry.arguments?.getString("id") ?: "0"
        val type = backStackEntry.arguments?.getString("type") ?: Navigation.TYPE_ANIMATION
        id = URLDecoder.decode(id, "UTF-8")
        var title = backStackEntry.arguments?.getString("title") ?: ""
        title = URLDecoder.decode(title, "UTF-8")
        DetailPage(
          id = id,
          type = type,
          title = title,
          navController = navController,
          baseHorizontalPadding = baseHorizontalPadding
        )
      }
      composable(Navigation.PLAYER + "/{id}/{subId}/{title}") { backStackEntry ->
        var id = backStackEntry.arguments?.getString("id") ?: "0"
        var title = backStackEntry.arguments?.getString("title") ?: ""
        id = URLDecoder.decode(id, "UTF-8")
        var subId = backStackEntry.arguments?.getString("subId") ?: ""
        subId = URLDecoder.decode(subId, "UTF-8")
        title = URLDecoder.decode(title, "UTF-8")
        PlaybackPage(id, subId, title, navController, baseHorizontalPadding)
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

