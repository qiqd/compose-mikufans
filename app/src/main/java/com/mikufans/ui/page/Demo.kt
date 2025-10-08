package com.mikufans.ui.page

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBasicSample(
  items: List<String>,
  onRefresh: @Composable () -> Unit,
  modifier: Modifier = Modifier
) {
  val pullToRefreshState = rememberPullToRefreshState()
  val coroutineScope = rememberCoroutineScope()
  var isRefreshing by rememberSaveable { mutableStateOf(false) }
  PullToRefreshBox(
    state = pullToRefreshState, // 使用状态中的 isRefreshing
    onRefresh = {
      isRefreshing = true
      coroutineScope.launch(Dispatchers.IO) {
        delay(5000)
      }
      isRefreshing = false
    },
    isRefreshing = isRefreshing
  ) {
    LazyColumn(Modifier.fillMaxSize()) {
      items(items) {
        ListItem({ Text(text = it) })
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun PullToRefreshSample() {
  val items = (1..100).map { "Item $it" }
  var isRefreshing by remember { mutableStateOf(false) }

  PullToRefreshBasicSample(
    items = items,
    onRefresh = {
      // 模拟网络请求
      LaunchedEffect(Unit) {
        delay(2000) // 模拟加载时间
      }
    }
  )
}
