package com.mikufans.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikufans.entity.History
import com.mikufans.ui.component.AnimeCard
import com.mikufans.ui.component.EmptyCompose
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.LocalStorage
import org.anime.entity.Animation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(navController: NavController, baseHorizontalPadding: Dp) {
  val context = LocalContext.current
  val lazyGridState = rememberLazyListState()
  var historyList by remember { mutableStateOf<List<History>>(emptyList()) }
  LaunchedEffect(Unit) {
    historyList =
      LocalStorage.getList(context, "view:history", History::class.java)?.toMutableList()
        ?: mutableListOf()
    historyList = historyList.sortedByDescending { it.time }
  }
  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = "返回")
          }
        },
        title = { Text("历史记录") }
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),


      ) {
      if (historyList.isEmpty()) {
        EmptyCompose(text = "暂无历史记录")
      } else {
        LazyColumn(
          state = lazyGridState,
          contentPadding = PaddingValues(5.dp),
          verticalArrangement = Arrangement.spacedBy(5.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(historyList.size, key = { it }) { index ->
            val saveItem = Animation()
            saveItem.id = historyList[index].id
            saveItem.subId = historyList[index].subId?.toInt()
            saveItem.title = historyList[index].name
            saveItem.titleCn = historyList[index].nameCn
            saveItem.coverUrls = listOf(historyList[index].cover)
            AnimeCard(
              saveItem,
              isSimple = false,
              episodeIndex = historyList[index].episodeIndex ?: 0,
              dateTime = historyList[index].time ?: System.currentTimeMillis()
            ) { animeSubId, animeName ->
              Navigation.navigateToAnimeDetail(
                navController = navController,
                playSource = historyList[index].source!!,
                animeSubId = animeSubId.toString(),
                animeId = historyList[index].id,
                animeName = animeName,
              )
            }
          }
        }
      }
    }
  }
}
