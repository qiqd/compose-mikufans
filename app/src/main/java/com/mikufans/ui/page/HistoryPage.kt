import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun HistoryPage(navController: NavController, baseHorizontalPadding: Dp) {
  val context = LocalContext.current
  val tabs = arrayOf("动画", "漫画")
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val coroutineScope = rememberCoroutineScope()
  var animationHistoryList by remember { mutableStateOf<List<History>>(emptyList()) }
  var comicHistoryList by remember { mutableStateOf<List<History>>(emptyList()) }
  var showClearDialog by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    LocalStorage.getList(context, "view:history", History::class.java)?.let { histories ->
      histories.filter { it.mediaType == Navigation.TYPE_ANIMATION }
        .let { animationHistoryList = it }
      histories.filter { it.mediaType == Navigation.TYPE_COMIC }.let { comicHistoryList = it }
    }
  }
  val updateHistory: () -> Unit = {
    LocalStorage.setList(context, "view:history", animationHistoryList + comicHistoryList)
  }
  val doClearHistory: () -> Unit = {
    animationHistoryList = emptyList()
    comicHistoryList = emptyList()
    updateHistory()
    showClearDialog = false
  }
  // 显示清空确认弹窗
  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = { Text("提示") },
      text = { Text("确定要清空全部历史记录吗？") },
      confirmButton = {
        TextButton(onClick = doClearHistory) { Text("确定") }
      },
      dismissButton = {
        TextButton(onClick = { showClearDialog = false }) { Text("取消") }
      }
    )
  }

  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding), topBar = {
      TopAppBar(actions = {
        IconButton(onClick = { showClearDialog = true }) {
          Icon(Icons.Default.Delete, contentDescription = "清空全部历史记录")
        }
      }, navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
          Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = "返回")
        }
      }, title = { Text("历史记录") })
    }) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    ) {

      PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            text = { Text(title) },
            selected = pagerState.currentPage == index,
            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } }
          )
        }
      }
      HorizontalPager(
        state = pagerState, userScrollEnabled = false
      ) {
        when (pagerState.currentPage) {
          0 -> HistoryContent(animationHistoryList, navController) {
            animationHistoryList = it
          }

          1 -> HistoryContent(comicHistoryList, navController) {
            comicHistoryList = it
          }

          else -> {}
        }
      }
    }
  }
}

@Composable
fun HistoryContent(
  historyList: List<History>,
  navController: NavController,
  onHistoryUpdate: (List<History>) -> Unit
) {
  val lazyGridState = rememberLazyListState()
  if (historyList.isEmpty()) {
    EmptyCompose(text = "暂无历史记录")
  } else {
    LazyColumn(
      state = lazyGridState,
      contentPadding = PaddingValues(5.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(historyList.size, key = { index -> historyList[index].hashCode() }) { index ->
        val currentItem = historyList[index]
        val dismissState = rememberSwipeToDismissBoxState()
        SwipeToDismissBox(
          state = dismissState,
          enableDismissFromStartToEnd = false,
          onDismiss = {
            val updatedList = historyList.toMutableList()
            if (index < updatedList.size) {
              updatedList.removeAt(index)
              onHistoryUpdate(updatedList)
              Log.e("HistoryPage", "onDismiss: item at index $index removed")
            }
          },
          backgroundContent = {
            val color by animateColorAsState(
              when (dismissState.targetValue) {
                SwipeToDismissBoxValue.Settled -> Color.Transparent
                SwipeToDismissBoxValue.EndToStart -> Color.Red
                else -> Color.Transparent
              }
            )
            Box(
              Modifier
                .fillMaxSize()
                .background(color, MaterialTheme.shapes.medium)
                .clip(MaterialTheme.shapes.medium)
            ) {
              Icon(
                Icons.Default.Delete,
                modifier = Modifier
                  .align(Alignment.CenterEnd)
                  .padding(end = 16.dp),
                contentDescription = "删除",
                tint = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Color.Black else Color.Transparent
              )
            }
          },
        ) {
          MediaCard(
            id = currentItem.id!!,
            title = currentItem.name,
            titleCn = currentItem.nameCn,
            coverUrl = currentItem.cover,
            lastViewAt = currentItem.time,
            chapterName = currentItem.chapterName,
            episodeIndex = if (currentItem.mediaType == Navigation.TYPE_ANIMATION) currentItem.episodeIndex else null,
            author = when (currentItem.mediaType) {
              Navigation.TYPE_COMIC -> currentItem.author
              else -> null
            },
            mediaType = when (currentItem.mediaType) {
              Navigation.TYPE_ANIMATION -> "动画"
              else -> "漫画"
            },
            onTap = { _ ->
              Navigation.navigateToDetail(
                navController = navController,
                id = when (currentItem.mediaType) {
                  Navigation.TYPE_COMIC -> currentItem.id!!
                  else -> ""
                },
                type = currentItem.mediaType,
                subId = currentItem.subId ?: "",
                title = currentItem.nameCn ?: currentItem.name ?: ""
              )
            })
        }
      }
    }
  }
}
