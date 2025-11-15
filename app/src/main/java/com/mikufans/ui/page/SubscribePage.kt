package com.mikufans.ui.page

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.mikufans.ui.component.EmptyCompose
import com.mikufans.ui.component.MediaCard
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.LocalStorage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribePage(
  navController: NavController,
  activity: ComponentActivity,
  baseHorizontalPadding: Dp
) {
  val content = LocalContext.current
  val lazyGridState = rememberLazyListState()
  var loveList by remember { mutableStateOf<List<History>>(emptyList()) }

  LaunchedEffect(Unit) {
    val all =
      LocalStorage.getList(content, "view:history", History::class.java)?.toList() ?: emptyList()
    loveList = all.filter { it.isLove }.sortedByDescending { it.time }
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
      if (loveList.isEmpty()) {
        EmptyCompose()
      } else {
        LazyColumn(
          state = lazyGridState,
          contentPadding = PaddingValues(vertical = 5.dp),
          verticalArrangement = Arrangement.spacedBy(5.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(loveList.size) { index ->
            MediaCard(
              id = loveList[index].id!!,
              title = loveList[index].name,
              titleCn = loveList[index].nameCn,
              coverUrl = loveList[index].cover,
              onTap = { id ->
                Navigation.navigateToPlayer(
                  id = id,
                  navController = navController,
                  title = loveList[index].name ?: loveList[index].nameCn ?: "",
                )
              }
            )
          }
        }
      }
    }
  }
}
