package com.mikufans.ui.page

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.api.ComicService
import com.mikufans.ui.component.LoadingOrMsg
import com.mikufans.util.GlobalSharedValue
import kotlinx.coroutines.launch
import org.anime.entity.base.ViewInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicPage(id: String, navController: NavController, baseHorizontalPadding: Dp) {
  var msg by rememberSaveable { mutableStateOf("") }
  val readMode = listOf("default", "reverse", "vertical")
  var showController by rememberSaveable { mutableStateOf(true) }
  var showChapter by rememberSaveable { mutableStateOf(false) }
  var showSetting by rememberSaveable { mutableStateOf(false) }
  var readModeIndex by rememberSaveable { mutableStateOf(0) }
  var episodeIndex by rememberSaveable { mutableStateOf(0) }
  var initIndex by rememberSaveable { mutableStateOf(0) }
  val coroutineScope = rememberCoroutineScope()
  var viewInfo by rememberSaveable { mutableStateOf<ViewInfo?>(null) }
  val sheetState = rememberModalBottomSheetState()
  val detail = GlobalSharedValue.comicDetail
  val initComic: suspend () -> Unit = {
    ComicService.fetchViewSync(id) {
      msg = "加载章节出错"
      Log.e("加载章节出错", it.toString())
    }.let { viewInfo = it }
  }
  //初次加载漫画详情
  LaunchedEffect(Unit) {
    coroutineScope.launch {
      initComic()
    }
  }
  //章节切换时加载章节
  LaunchedEffect(episodeIndex) {
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
              Icon(Icons.Rounded.FormatListNumbered, contentDescription = "章节列表")
            }
            IconButton(onClick = { showSetting = !showSetting }) {
              Icon(Icons.Rounded.Settings, contentDescription = "设置")
            }
          }
        )
      }
    }
  ) { _ ->
    Box(Modifier.fillMaxSize()) {
      Box(
        Modifier
          .fillMaxSize()
          .clickable { showController = !showController }) {
        viewInfo?.let {
          when (readModeIndex) {
            0 -> viewInfo?.let {
              DefaultReader(it, initIndex = initIndex)
            } ?: run {
              LoadingOrMsg(msg)
            }

            1 -> viewInfo?.let {
              DefaultReader(it, initIndex = initIndex, reverse = true)
            } ?: run {
              LoadingOrMsg(msg)
            }

            2 -> viewInfo?.let {
              VerticalReader(it, initIndex = initIndex)
            } ?: run {
              LoadingOrMsg(msg)
            }
          }
        } ?: run {
          LoadingOrMsg(msg)
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
              Row() {
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
        onDismissRequest = { coroutineScope.launch { sheetState.hide() } }
      ) {
        detail?.sources[0]?.episodes.takeIf { it.isNullOrEmpty().not() }?.let { episodes ->
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = baseHorizontalPadding)
          ) {
            items(count = episodes.size) {
              Text(
                text = episodes[it].title,
                modifier = Modifier
                  .padding(5.dp)
                  .clickable {
                    episodeIndex = it
                    coroutineScope.launch { sheetState.hide() }
                  })
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
  val pagerState = rememberPagerState(initialPage = initIndex, pageCount = { viewInfo.urls.size })
  LaunchedEffect(pagerState.currentPage) {
    onIndexChange(pagerState.currentPage)
  }
  HorizontalPager(state = pagerState, reverseLayout = reverse) {
    AsyncImage(
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Fit,
      model = viewInfo.urls[it],
      contentDescription = viewInfo.episodeName
    )
  }
}

@Composable
private fun VerticalReader(
  viewInfo: ViewInfo,
  initIndex: Int = 0,
  onIndexChange: (Int) -> Unit = {}
) {
  val initIndex = initIndex
  initIndex.coerceIn(0, viewInfo.urls.size - 1)
  val state = rememberLazyListState(initIndex)
  LaunchedEffect(state.firstVisibleItemIndex) {
    onIndexChange(state.firstVisibleItemIndex)
  }
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
  ) {
    items(viewInfo.urls.size) {
      AsyncImage(
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,
        model = viewInfo.urls[it],
        contentDescription = viewInfo.episodeName
      )
    }
  }
}
