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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPage(navController: NavController, baseHorizontalPadding: Dp) {
  val context = LocalContext.current
  val lazyGridState = rememberLazyListState()
  var historyList by remember { mutableStateOf<List<History>>(emptyList()) }
  // 控制是否显示“清空确认”弹窗
  var showClearDialog by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    historyList =
      LocalStorage.getList(context, "view:history", History::class.java)?.toMutableList()
        ?: mutableListOf()
    historyList = historyList.sortedByDescending { it.time }
  }
  val updateHistory: () -> Unit = {
    LocalStorage.setList(context, "view:history", historyList)
  }
  val doClearHistory: () -> Unit = {
    historyList = mutableListOf()
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
                  historyList = updatedList
                  updateHistory()
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
                    .background(color)
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
                episodeIndex = currentItem.episodeIndex,
                lastViewAt = currentItem.time,
                onTap = {
                  Navigation.navigateToDetail(
                    navController = navController,
                    id = "",
                    title = currentItem.nameCn ?: currentItem.name ?: ""
                  )
                })
            }
          }
        }
      }
    }
  }
}
