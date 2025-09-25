package com.mikufans.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikufans.ui.component.AnimeCard
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.LocalStorage
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.History
import com.mikufans.xmd.teto.entity.SubjectSearch

@Composable
fun Subscribe(navController: NavController) {
  val anime = Anime()
  anime.coverUrl = "https://img.pan.kg/images/363957_pgptl.webp"
  anime.title = "夏日口袋"
  val content = LocalContext.current
  val lazyGridState = rememberLazyGridState()
  var historyList by remember { mutableStateOf<List<History>>(emptyList()) }
  val loveList = remember(historyList) { historyList.filter { it.isLove } }
  LaunchedEffect(Unit) {
    historyList =
      LocalStorage.getList(content, "view:history", History::class.java)?.toList() ?: emptyList()
  }
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    if (!historyList.any { it.isLove }) {
      Text("暂无追番", textAlign = TextAlign.Center)
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = lazyGridState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
      ) {
        items(loveList.size) { index ->
          val isLove = loveList[index].isLove
          val subject = SubjectSearch.Subject(
            id = historyList[index].id,
            images = SubjectSearch.Subject.Images(
              medium = historyList[index].cover, large = historyList[index].cover
            ),
            nameCn = historyList[index].nameCn,
            name = historyList[index].name,
          )
          AnimeCard(subject) { animeId, animeName ->
            Navigation.navigateToAnimeDetail(navController, animeId, animeName)
          }
        }
      }
    }
  }


}
