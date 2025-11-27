package com.mikufans.ui.page

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.api.ComicService
import com.mikufans.entity.History
import com.mikufans.ui.component.LoadingOrShowMsg
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.GifLoader
import com.mikufans.util.GlobalSharedValue
import com.mikufans.util.LocalStorage
import com.mikufans.util.WindowUtil
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import org.anime.entity.base.ViewInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicPage(id: String, navController: NavController, baseHorizontalPadding: Dp) {
  val context = LocalContext.current
  val readMode = listOf("垂直", "反向", "默认")
  var msg by rememberSaveable { mutableStateOf("") }
  var showController by rememberSaveable { mutableStateOf(true) }
  var showChapter by rememberSaveable { mutableStateOf(false) }
  var showSetting by rememberSaveable { mutableStateOf(false) }
  var readModeIndex by rememberSaveable { mutableIntStateOf(0) }
  var episodeIndex by rememberSaveable { mutableIntStateOf(id.toIntOrNull() ?: 0) }
  var initPageIndex by rememberSaveable { mutableIntStateOf(0) }
  val coroutineScope = rememberCoroutineScope()
  var viewInfo by rememberSaveable { mutableStateOf<ViewInfo?>(null) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var localHistory by rememberSaveable { mutableStateOf<List<History>>(mutableListOf()) }
  val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = initPageIndex)
  val detail = GlobalSharedValue.mediaDetail
  var love by rememberSaveable { mutableStateOf(false) }
  var historyIndex by rememberSaveable { mutableIntStateOf(-1) }
  val loadLocalHistory: () -> Unit = {
    LocalStorage.getList(context, "view:history", History::class.java)?.toMutableList()
      ?.let { histories ->
        localHistory = histories;
        histories.indexOfFirst { it.id == detail?.media?.id }.let {
          historyIndex = it
          if (historyIndex >= 0) {
            initPageIndex = histories[historyIndex].pageIndex
            love = histories[historyIndex].isLove
          }
        }
      }

  }
  val updateHistory: () -> Unit = {
    val history = History(
      id = detail?.media?.id,
      subId = detail?.media?.id,
      name = detail?.media?.title,
      nameCn = detail?.media?.titleCn,
      mediaType = Navigation.TYPE_COMIC,
      isLove = love,
      cover = detail?.media?.coverUrls[0],
      episodeIndex = episodeIndex,
      pageIndex = initPageIndex,
      time = System.currentTimeMillis()
    )
    localHistory = localHistory.toMutableList()
      .apply { add(history) }
      .groupBy { it.id }
      .map { (_, value) -> value.maxBy { it.time } }
      .sortedByDescending { it.time }
    LocalStorage.setList(context, "view:history", localHistory)
  }
  val initComic: suspend () -> Unit = {
    ComicService.fetchViewSync(detail?.sources[0]?.episodes[id.toInt()]?.id) {
      msg = "加载章节出错"
      Log.e("加载章节出错", it.toString())
    }.let { viewInfo = it }
  }
  //初次加载漫画详情
  LaunchedEffect(Unit) {
    readModeIndex = LocalStorage.getInt(context, "comic:readMode")
    loadLocalHistory()
    coroutineScope.launch {
      initComic()
    }
  }
  DisposableEffect(Unit) {
    onDispose {
      updateHistory()
    }
  }
  LaunchedEffect(initPageIndex) {
    lazyListState.animateScrollToItem(initPageIndex)
  }
  LaunchedEffect(readModeIndex) {
    LocalStorage.set(context, "comic:readMode", readMode[readModeIndex])
  }
  LaunchedEffect(showController) {
    WindowUtil.hideSystemBar((context as Activity).window, showController.not())
  }

  //章节切换时加载章节
  LaunchedEffect(episodeIndex) {
    viewInfo = null
    coroutineScope.launch {
      detail?.let {
        it.sources
          .takeIf { sources -> sources.isNullOrEmpty().not() }
          ?.let { s ->
            msg = ""
            ComicService.fetchViewSync(s[0].episodes[episodeIndex].id) { error ->
              msg = "加载章节出错"
              Log.e("加载章节出错", error.toString())
            }?.let { info ->
              viewInfo = info
            }
          }
      }
    }
  }
  Scaffold(
    topBar = {
      AnimatedVisibility(
        visible = showController,
        enter = slideInVertically(),
        exit = slideOutVertically()
      ) {
        TopAppBar(
          navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
          },
          title = { Text(viewInfo?.episodeName ?: "暂无章节标题") },
          actions = {
            IconButton(onClick = { showChapter = !showChapter }) {
              Icon(Icons.AutoMirrored.Rounded.FormatListBulleted, contentDescription = "章节列表")
            }
            IconButton(onClick = { showSetting = !showSetting }) {
              Icon(Icons.Rounded.Settings, contentDescription = "设置")
            }
          }
        )
      }
    },
    bottomBar = {
      if (showController) {
        BottomAppBar {
          Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            OutlinedButton(
              enabled = episodeIndex > 0,
              onClick = { episodeIndex-- }) { Text("上一章") }
            OutlinedButton(
              enabled = episodeIndex < (detail?.sources[0]?.episodes?.size ?: 0),
              onClick = { episodeIndex++ }) { Text("下一章") }
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      Modifier
        .fillMaxSize()
        .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
      Box(
        Modifier
          .fillMaxSize()
          .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
          ) { showController = !showController; }) {
        viewInfo?.let {
          when (readModeIndex) {
            0 -> viewInfo?.let {
              VerticalReader(it, initIndex = initPageIndex)
            } ?: run {
              LoadingOrShowMsg(msg)
            }

            1 -> viewInfo?.let {
              DefaultReader(it, initIndex = initPageIndex)

            } ?: run {
              LoadingOrShowMsg(msg)
            }

            2 -> viewInfo?.let {
              DefaultReader(it, initIndex = initPageIndex, reverse = true)
            } ?: run {
              LoadingOrShowMsg(msg)
            }
          }
        } ?: run {
          LoadingOrShowMsg(msg)
        }
      }
    }
    // 阅读模式选择弹窗
    AnimatedVisibility(visible = showSetting, enter = fadeIn(), exit = fadeOut()) {
      AlertDialog(
        onDismissRequest = { showSetting = false },
        title = { Text("选择阅读模式") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            readMode.forEachIndexed { index, s ->
              Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                  selected = index == readModeIndex,
                  onClick = { readModeIndex = index }
                )
                Text(text = s)
              }
            }
          }
        },
        confirmButton = {
          TextButton(
            onClick = { showSetting = false }
          ) {
            Text("确定")
          }
        }
      )
    }
    // 章节列表弹窗
    AnimatedVisibility(visible = showChapter) {
      ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { coroutineScope.launch { sheetState.hide(); showChapter = false } }
      ) {

        detail?.sources[0]?.episodes.takeIf { it.isNullOrEmpty().not() }?.let { episodes ->

          LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = baseHorizontalPadding)
          ) {
            items(count = episodes.size) {
              if (it == episodeIndex) {
                Button(
                  modifier = Modifier.fillMaxWidth(),
                  onClick = {
                    episodeIndex = it;
                    showChapter =
                      false; coroutineScope.launch { sheetState.hide() }
                  }) {
                  Text(text = episodes[it].title, textAlign = TextAlign.Start)
                }
              } else {
                OutlinedButton(
                  modifier = Modifier.fillMaxWidth(),
                  onClick = {
                    episodeIndex = it
                    showChapter =
                      false; coroutineScope.launch { sheetState.hide() }
                  }) {
                  Text(text = episodes[it].title, textAlign = TextAlign.Start)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DefaultReader(
  viewInfo: ViewInfo,
  initIndex: Int = 0,
  reverse: Boolean = false,
  onIndexChange: (Int) -> Unit = {}
) {
  val content = LocalContext.current
  val pagerState = rememberPagerState(initialPage = initIndex, pageCount = { viewInfo.urls.size })
  LaunchedEffect(pagerState.currentPage) {
    onIndexChange(pagerState.currentPage)
  }
  HorizontalPager(state = pagerState, reverseLayout = reverse) {
    AsyncImage(
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Fit,
      model = viewInfo.urls[it],
      contentDescription = viewInfo.episodeName,
      placeholder = GifLoader.gifPlaceholder(R.drawable.loading, content),
    )
  }
}

@Composable
private fun VerticalReader(
  viewInfo: ViewInfo,
  initIndex: Int = 0,
  onIndexChange: (Int) -> Unit = {}
) {
  val content = LocalContext.current
  val initIndex = initIndex
  initIndex.coerceIn(0, viewInfo.urls.size - 1)
  val state = rememberLazyListState(initIndex)
  LaunchedEffect(state) {
    snapshotFlow { state.firstVisibleItemIndex }
      .collect { index ->
        awaitFrame()
        onIndexChange(index)
      }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
  ) {
    items(viewInfo.urls.size) {
      AsyncImage(
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        model = viewInfo.urls[it],
        contentDescription = viewInfo.episodeName,
        placeholder = GifLoader.gifPlaceholder(R.drawable.loading, content),
      )
    }
  }
}
