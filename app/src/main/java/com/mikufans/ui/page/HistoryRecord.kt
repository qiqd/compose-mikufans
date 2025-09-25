package com.mikufans.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikufans.ui.component.AnimeCard
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.LocalStorage
import com.mikufans.xmd.miku.entiry.History
import com.mikufans.xmd.teto.entity.SubjectSearch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryRecord(navController: NavController) {
  val context = LocalContext.current
  val lazyGridState = rememberLazyGridState()
  var historyList by rememberSaveable { mutableStateOf<List<History>>(emptyList()) }

  LaunchedEffect(Unit) {
    historyList =
      LocalStorage.getList(context, "view:history", History::class.java)?.toMutableList()
        ?: mutableListOf()
  }
  Scaffold(
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
        Text(text = "暂无历史记录")
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(3), state = lazyGridState,
          modifier = Modifier.padding(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(historyList.size, key = { it }) { index ->
            val subject = SubjectSearch.Subject(
              id = historyList[index].id,
              images = SubjectSearch.Subject.Images(
                medium = historyList[index].cover, large = historyList[index].cover
              ),
              nameCn = historyList[index].nameCn,
              name = historyList[index].name,
            )
            AnimeCard(
              subject,
              isSimple = false,
              episodeIndex = historyList[index].episodeIndex ?: 0,
              dateTime = historyList[index].time ?: System.currentTimeMillis()
            ) { animeId, animeName ->
              Navigation.navigateToAnimeDetail(navController, animeId, animeName)
            }
          }
        }
      }
    }
  }
}
