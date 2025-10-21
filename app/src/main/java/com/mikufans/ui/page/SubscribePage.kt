package com.mikufans.ui.page

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikufans.ui.component.AnimeCard
import com.mikufans.ui.component.EmptyCompose
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.LocalStorage
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.History

@Composable
fun SubscribePage(navController: NavController, activity: ComponentActivity) {
  val anime = Anime()
  anime.coverUrl = "https://img.pan.kg/images/363957_pgptl.webp"
  anime.name = "夏日口袋"
  val content = LocalContext.current
  val lazyGridState = rememberLazyListState()
  var historyList by remember { mutableStateOf<List<History>>(emptyList()) }
  val loveList = remember(historyList) { historyList.filter { it.isLove } }
  LaunchedEffect(Unit) {
    historyList =
      LocalStorage.getList(content, "view:history", History::class.java)?.toList() ?: emptyList()
    historyList = historyList.sortedByDescending { it.time }
  }
  BackHandler { activity.moveTaskToBack(true) }
  Column(
    modifier = Modifier.fillMaxSize(),
//    verticalArrangement = Arrangement.Center,
//    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (!historyList.any { it.isLove }) {
      EmptyCompose()
    } else {
      LazyColumn(
        state = lazyGridState,
        contentPadding = PaddingValues(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(loveList.size) { index ->
          val isLove = loveList[index].isLove
          val anime1 = Anime(
            id = historyList[index].id.toString(),
            subId = historyList[index].subId?.toInt(),
            name = historyList[index].nameCn,
          )
          anime1.coverUrl = historyList[index].cover

          AnimeCard(anime1) { animeSubId, animeName ->
            Navigation.navigateToAnimeDetail(
              navController = navController,
              animeId = historyList[index].id.toString(),
              animeSubId = animeSubId.toString(),
              animeName = animeName
            )
          }
        }
      }
    }
  }


}
