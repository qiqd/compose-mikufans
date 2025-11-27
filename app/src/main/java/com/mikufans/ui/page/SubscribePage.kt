package com.mikufans.ui.page

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikufans.entity.History
import com.mikufans.ui.component.EmptyCompose
import com.mikufans.ui.component.MediaCard
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.LocalStorage
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribePage(
  navController: NavController,
  activity: ComponentActivity,
  baseHorizontalPadding: Dp
) {
  val content = LocalContext.current
  var animationHistoryList by remember { mutableStateOf<List<History>>(emptyList()) }
  var comicHistoryList by remember { mutableStateOf<List<History>>(emptyList()) }
  LaunchedEffect(Unit) {
    LocalStorage.getList(content, "view:history", History::class.java)?.let { histories ->
      animationHistoryList =
        histories.filter { it.mediaType == Navigation.TYPE_ANIMATION && it.isLove }
      comicHistoryList = histories.filter { it.mediaType == Navigation.TYPE_COMIC && it.isLove }
    }
  }
  BackHandler { activity.moveTaskToBack(true) }
  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = { TopAppBar(title = { Text("追番") }) },
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    ) {
      val tabs = arrayOf("动画", "漫画")
      val pagerState = rememberPagerState(pageCount = { tabs.size })
      val coroutineScope = rememberCoroutineScope()
      PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
        tabs.forEachIndexed { index, title ->
          Tab(
            text = { Text(title) },
            selected = pagerState.currentPage == index,
            onClick = {
              coroutineScope.launch {
                pagerState.animateScrollToPage(index)
              }
            }
          )
        }
      }
      HorizontalPager(state = pagerState) {
        when (it) {
          0 -> SubscribeContent(animationHistoryList, navController)
          1 -> SubscribeContent(comicHistoryList, navController)
        }
      }
    }
  }
}

@Composable
fun SubscribeContent(loveList: List<History>, navController: NavController) {
  if (loveList.isEmpty()) {
    EmptyCompose()
  } else {
    LazyColumn(
      contentPadding = PaddingValues(vertical = 5.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(loveList.size) { index ->
        if (!loveList[index].nameCn.isNullOrBlank()) {
          MediaCard(
            id = loveList[index].id!!,
            title = loveList[index].name,
            titleCn = loveList[index].nameCn,
            coverUrl = loveList[index].cover,
            author = loveList[index].author,
            mediaType = when (loveList[index].mediaType) {
              Navigation.TYPE_COMIC -> "漫画"
              else -> "动画"
            },
            onTap = { _ ->
              Navigation.navigateToDetail(
                navController = navController,
                id = when (loveList[index].mediaType) {
                  Navigation.TYPE_COMIC -> loveList[index].id!!
                  else -> ""
                },
                type = loveList[index].mediaType,
                subId = loveList[index].subId!!,
                title = loveList[index].nameCn ?: ""
              )
            }
          )
        }
      }
    }
  }

}