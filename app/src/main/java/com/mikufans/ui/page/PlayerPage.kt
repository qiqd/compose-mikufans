package com.mikufans.ui.page

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.api.AnimationService
import com.mikufans.entity.History
import com.mikufans.ui.component.CapVideoPlayer
import com.mikufans.ui.component.EmptyCompose
import com.mikufans.util.GifLoader
import com.mikufans.util.GlobalSharedValue
import com.mikufans.util.LocalStorage
import com.mikufans.view.CapPlayerViewModel
import kotlinx.coroutines.launch
import org.anime.entity.animation.Animation
import org.anime.entity.base.Source
import org.anime.entity.base.ViewInfo
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackPage(
  id: String,
  subId: String,
  title: String,
  navController: NavController,
  baseHorizontalPadding: Dp,
) {
  val content = LocalContext.current
  val view = LocalView.current
  val tabs = arrayOf("简介", "剧集")
  var isLove by rememberSaveable { mutableStateOf(false) }
  var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
  var historyPosition by rememberSaveable { mutableLongStateOf(0L) }
  var playerKey by rememberSaveable { mutableIntStateOf(-1) }
  var isLoading by rememberSaveable { mutableStateOf(false) }
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
  val coroutineScope = rememberCoroutineScope()
  var isFullscreen by rememberSaveable { mutableStateOf(false) }
  val capPlayer: CapPlayerViewModel = viewModel()
  var localHistory by rememberSaveable { mutableStateOf<List<History>>(emptyList()) }
  var viewInfo by rememberSaveable { mutableStateOf<ViewInfo?>(null) }
  var episodeIndex by rememberSaveable { mutableIntStateOf(0) }
  var historyIndex by rememberSaveable { mutableIntStateOf(-1) }
  var sourceIndex by rememberSaveable { mutableIntStateOf(0) }
  var errorMsg by rememberSaveable { mutableStateOf("") }
  val detail = GlobalSharedValue.animationDetail
  val loadLocalHistory: () -> Unit = {
    LocalStorage.getList(content, "view:history", History::class.java)?.toMutableList()
      ?.let { localHistory = it }
    historyIndex = localHistory.indexOfFirst { it.subId == subId }
    if (historyIndex >= 0) {
      episodeIndex = localHistory[historyIndex].episodeIndex ?: 0
      historyPosition = localHistory[historyIndex].position ?: 0L
      isLove = localHistory[historyIndex].isLove
      sourceIndex = localHistory[historyIndex].sourceIndex ?: 0

      // 先限制 sourceIndex 范围
      sourceIndex = sourceIndex.coerceAtMost((detail?.sources?.size ?: 1) - 1)
      sourceIndex = sourceIndex.coerceAtLeast(0)

      // 再限制 episodeIndex 范围
      val maxEpisodeIndex = (detail?.sources?.getOrNull(sourceIndex)?.episodes?.size ?: 1) - 1
      episodeIndex = episodeIndex.coerceAtMost(maxEpisodeIndex)
      episodeIndex = episodeIndex.coerceAtLeast(0)
    }


  }
  val updateLocalHistory: () -> Unit = {
    val animation = detail?.media
    val history = History(
      id = id,
      subId = subId,
      name = animation?.title,
      nameCn = animation?.titleCn,
      cover = animation?.coverUrls[0],
      episodeIndex = episodeIndex,
      videoUrl = viewInfo?.urls[0],
      position = currentPosition - 5000,
      isLove = isLove,
      sourceIndex = sourceIndex,
      time = System.currentTimeMillis(),
    )
    val list = localHistory.toMutableList()
    if (historyIndex >= 0) list[historyIndex] = history else list.add(history)
    LocalStorage.setList(content, "view:history", list)
  }
  val fetchPlayerInfo: () -> Unit = {
    isLoading = true
    if (detail?.sources.isNullOrEmpty()) {
      errorMsg = "剧集为空"
    }
    detail?.sources.takeIf { it.isNullOrEmpty().not() }?.let { sources ->
      sources.takeIf { it[sourceIndex].episodes.isNullOrEmpty().not() }?.let {
        coroutineScope.launch {
          AnimationService.fetchViewSync(sources[sourceIndex].episodes[episodeIndex].id) { msg ->
            errorMsg = msg.message.toString()
            coroutineScope.launch {
              Toast.makeText(content, msg.message, Toast.LENGTH_SHORT).show()
            }
          }?.let { info ->
            viewInfo = info; playerKey = episodeIndex; isLoading = false
          }
        }
      }
    }
  }
  val episodeChange: () -> Unit = {
    historyPosition = 0L
    fetchPlayerInfo()
  }
  DisposableEffect(Unit) { onDispose { updateLocalHistory() } }

  /* 初始数据加载 */
  LaunchedEffect(Unit) {
    loadLocalHistory()
    fetchPlayerInfo()
  }
  /**
   * Scaffold 布局
   */
  Scaffold(
    modifier = Modifier.also {
      if (isFullscreen) {
        Modifier.background(Color.Black)
      }
    }) { innerPadding ->
    Column(
      modifier = Modifier.padding(innerPadding).also {
        if (isFullscreen) {
          Modifier.background(Color.Black)
        }
      }) {
      Row(
        modifier = Modifier.fillMaxWidth()
      ) {
        val animation = detail?.media
        CapVideoPlayer(
          key = playerKey.toString(),
          isLoading = isLoading,
          videoUrl = viewInfo?.urls?.getOrNull(0),
          title = animation?.titleCn ?: animation?.title ?: "暂无标题",
          episodeIndex = episodeIndex,
          showNextButton = false,
          showPreviousButton = false,
          navController = navController,
          initPosition = historyPosition,
          onPositionChange = {
            currentPosition = it
          },
          onLeadingBackButtonTab = {
            if (!isFullscreen) {
              navController.popBackStack()
              capPlayer.releasePlayer()
            }
          },
          onLandscapeChange = {
            isFullscreen = it
          },
          onPlayerError = {
            Toast.makeText(
              content, "播放出错,试着换一条线路: ${it.message}", Toast.LENGTH_SHORT
            ).show()
          })
      }

      PrimaryTabRow(selectedTabIndex = tabIndex.value, Modifier.padding(horizontal = 10.dp)) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selected = tabIndex.value == index,
            onClick = {
              coroutineScope.launch { pagerState.animateScrollToPage(index) }
            }) {
            Text(text = title, modifier = Modifier.padding(10.dp))
          }
        }
      }
      HorizontalPager(
        state = pagerState, modifier = Modifier.fillMaxSize()
      ) { page ->
        when (page) {
          0 -> AnimeInfoPage(detail?.media, errorMsg, isLove, baseHorizontalPadding) { isLove = it }
          1 -> SourcePage(
            errorMsg = errorMsg,
            activeIndex = episodeIndex,
            sources = detail?.sources,
            baseHorizontalPadding = baseHorizontalPadding,
            onSourceChange = { s: Int ->
              sourceIndex = s
            },
            onEpisodeChange = { _, index ->
              if (index == episodeIndex || isLoading) return@SourcePage
              episodeIndex = index
              episodeChange()
            })
        }
      }
    }
  }
}


/* ====================== 简介页 ====================== */
@Composable
fun AnimeInfoPage(
  animation: Animation?,
  errorMsg: String,
  isLove: Boolean = false,
  baseHorizontalPadding: Dp,
  loveHandle: (Boolean) -> Unit
) {
  animation?.let { anime ->
    LazyColumn(
      modifier = Modifier
        .padding(baseHorizontalPadding)
        .padding(bottom = 0.dp)
        .fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          AsyncImage(
            modifier = Modifier
              .width(120.dp)
              .aspectRatio(2 / 3f)
              .clip(MaterialTheme.shapes.medium),
            model = anime.coverUrls[0],
            contentDescription = anime.titleCn ?: anime.title,
            placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current),
            contentScale = ContentScale.Crop
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = anime.titleCn ?: anime.title ?: "未知标题",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            anime.ariDate?.let {
              Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
              )
            }
            anime.rating?.let {
              Text(
                text = "评分: $it",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
            anime.totalEpisode?.let {
              Text(
                text = "集数: $it",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
            Spacer(Modifier.weight(1f))
            AnimatedContent(targetState = isLove, label = "loveToggle") { love ->
              if (love) {
                Button(
                  modifier = Modifier
                    .width(150.dp)
                    .padding(0.dp),
                  onClick = { loveHandle(false) }) { Text("追番中") }
              } else {
                OutlinedButton(
                  modifier = Modifier
                    .width(150.dp)
                    .padding(0.dp),
                  onClick = { loveHandle(true) }) { Text("追番") }
              }
            }
          }
        }
      }

      anime.description?.let { summary ->
        item {
          Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "简介",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
              )
            }
          }
        }
      }

      item {
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "详细信息",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            anime.platform?.let {
              Text(
                text = "平台: $it",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
              )
            }
          }
        }
      }
    }
  } ?: run {
    Box(
      modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) { Text(errorMsg.ifEmpty { "加载中..." }) }
  }
}

/* ====================== 选集页 ====================== */
@Composable
fun SourcePage(
  errorMsg: String,
  activeIndex: Int = 0,
  sources: List<Source>?,
  onSourceChange: (Int) -> Unit,
  baseHorizontalPadding: Dp,
  onEpisodeChange: (String, Int) -> Unit
) {
  val sourcePagerState = rememberPagerState(pageCount = { sources?.size ?: 0 })
  val coroutineScope = rememberCoroutineScope()
  sources?.let {
    Column(Modifier.fillMaxWidth()) {
      PrimaryTabRow(
        selectedTabIndex = sourcePagerState.currentPage,
        modifier = Modifier.padding(horizontal = baseHorizontalPadding),
        divider = {}) {
        sources.forEachIndexed { index, _ ->
          Tab(
            modifier = Modifier.height(30.dp),
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            text = {
              Text(
                text = "${index + 1}", fontSize = MaterialTheme.typography.bodySmall.fontSize
              )
            },
            selected = sourcePagerState.currentPage == index,
            onClick = {
              onSourceChange(index)
              coroutineScope.launch { sourcePagerState.animateScrollToPage(index) }
            })
        }
      }
      HorizontalPager(state = sourcePagerState, userScrollEnabled = false) { index ->
        EpisodePage(sources[index], activeIndex, onEpisodeChange)
      }
    }
  } ?: run {
    EmptyCompose(errorMsg.ifEmpty { "暂无线路" })
  }
}


/**
 * 剧集页
 */
@Composable
fun EpisodePage(source: Source, activeIndex: Int = 0, onEpisodeChange: (String, Int) -> Unit) {
  val episodes = source.episodes
  var index by rememberSaveable() { mutableStateOf(activeIndex) }
  if (episodes.isNotEmpty()) {
    episodes.let { episodeList ->
      LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
          .padding(horizontal = 18.dp)
          .padding(top = 10.dp)
          .fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        itemsIndexed(episodeList) { i, episode ->
          if (index == i) {
            Button(
              onClick = {}, modifier = Modifier.fillMaxWidth()
            ) { Text(text = "${i + 1}", maxLines = 1) }
          } else {
            OutlinedButton(
              onClick = {
                index = i
                onEpisodeChange(episode.id, index)
              }, modifier = Modifier.fillMaxWidth()
            ) { Text(text = "${i + 1}", maxLines = 1) }
          }
        }
      }
    }
  } else {
    Box(
      modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) { Text("暂无集数") }
  }
}

fun formatTime(ms: Long): String {
  if (ms <= 0) return "00:00"
  val s = (ms / 1000).toInt()
  val m = s / 60
  val r = s % 60
  return String.format(Locale.getDefault(), "%02d:%02d", m, r)
}
