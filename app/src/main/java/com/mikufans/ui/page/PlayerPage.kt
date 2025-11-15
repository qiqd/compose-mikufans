package com.mikufans.ui.page

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.entity.History
import com.mikufans.ui.component.CapVideoPlayer
import com.mikufans.util.GifLoader
import com.mikufans.util.LocalStorage
import com.mikufans.view.CapPlayerViewModel
import com.mikufans.xmd.util.MetaService
import com.mikufans.xmd.util.StringMatchUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.anime.api.AnimationApi
import org.anime.entity.animation.Animation
import org.anime.entity.base.ViewInfo
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackPage(
  id: String,
  title: String,
  episodes: List<String> = emptyList(),
  navController: NavController,
) {
  val content = LocalContext.current
  val tabs = arrayOf("简介", "剧集")
  val animationApi = AnimationApi.SOURCES_WITH_DELAY
  var isLove by rememberSaveable { mutableStateOf(false) }
  var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
  var historyPosition by rememberSaveable { mutableLongStateOf(0L) }
  var currentPlayingEpisodeIndex by rememberSaveable { mutableIntStateOf(0) }
  var currentPlayingEpisodeId by rememberSaveable { mutableStateOf("") }
  var isLoading by rememberSaveable { mutableStateOf(false) }
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
  val coroutineScope = rememberCoroutineScope()
  var isFullscreen by rememberSaveable { mutableStateOf(false) }
  val capPlayerViewModel: CapPlayerViewModel = viewModel()
  val bangumi by rememberSaveable { mutableStateOf(MetaService.bangumi) }
  var animation by rememberSaveable { mutableStateOf<Animation?>(null) }
  var localHistory by rememberSaveable { mutableStateOf<List<History>>(emptyList()) }
  var viewInfo by rememberSaveable { mutableStateOf<ViewInfo?>(null) }
  var episodes by rememberSaveable { mutableStateOf(episodes) }
  val fetchMetaInfo: () -> Unit = {
    coroutineScope.launch(Dispatchers.IO) {
      val animations = bangumi.fetchSearchResultSync(title, 1, 1)
      val titleToAnimationMap = animations.associateBy { it.titleCn }
      val bestMatch =
        StringMatchUtil.findBestMatchWithJaroWinkler(animations.map { it.titleCn }, title)
      animation = titleToAnimationMap[bestMatch]
    }
  }
  val loadLocalHistory: () -> Unit = {
    LocalStorage.getList(content, "view:history", History::class.java)?.toMutableList()
      ?.let { localHistory = it }
    val idx = localHistory.indexOfFirst { it.subId == animation?.id }
    if (idx >= 0) {
      currentPlayingEpisodeId = localHistory[idx].episodeId!!
      currentPlayingEpisodeIndex = localHistory[idx].episodeIndex ?: 0
      currentPosition = localHistory[idx].position ?: 0L
      isLove = localHistory[idx].isLove
    } else {
      currentPlayingEpisodeId = episodes[0]
      currentPlayingEpisodeIndex = 0
      currentPosition = 0L
    }
  }
  val updateLocalHistory: () -> Unit = {
    val history = History(
      id = id,
      subId = animation?.id,
      name = animation?.title,
      nameCn = animation?.titleCn,
      cover = animation?.coverUrls[0],
      episodeId = currentPlayingEpisodeId,
      episodeIndex = currentPlayingEpisodeIndex,
      videoUrl = viewInfo?.urls[0],
      position = currentPosition - 5000,
      isLove = isLove,
      time = System.currentTimeMillis(),
    )
    val list = localHistory.toMutableList()
    val idx = list.indexOfFirst { it.subId == animation?.id }
    if (idx >= 0) list[idx] = history else list.add(history)
    LocalStorage.setList(content, "view:history", list)
  }
  val onEpisodeChange: (episodeId: String, index: Int) -> Unit = { episodeId, index ->
    isLoading = true
    currentPlayingEpisodeIndex = index
    currentPlayingEpisodeId = episodeId
    coroutineScope.launch(Dispatchers.IO) {
      try {
        Log.w("PlayerPage", "onEpisodeChange: $episodeId, $index")
        animationApi[0].htmlParser.fetchViewSync(episodeId)?.let { viewInfo = it }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          Toast.makeText(
            navController.context, "错误:${e.message}", Toast.LENGTH_SHORT
          ).show()
        }
      } finally {
        currentPosition = 0L
        isLoading = false
      }
    }
  }
  val playInfoInit: () -> Unit = {
    if (episodes.isEmpty()) {
      coroutineScope.launch(Dispatchers.IO) {
        try {
          animationApi[0].htmlParser.fetchDetailSync(id)?.let {
            episodes =
              it.episodes.map { source ->
                source.episodes.map { episode -> episode.id }.toString()
              }
          }
        } catch (e: Exception) {
          Toast.makeText(
            navController.context, "错误:${e.message}", Toast.LENGTH_SHORT
          ).show()
        }
      }
    }

    onEpisodeChange(currentPlayingEpisodeId, currentPlayingEpisodeIndex)
  }


  DisposableEffect(Unit) { onDispose { updateLocalHistory() } }

  /* 初始数据加载 */
  LaunchedEffect(Unit) {
    fetchMetaInfo()
    loadLocalHistory()
    playInfoInit()
  }

  Scaffold { innerPadding ->
    /* 播放器区域 */
    Column(
      modifier = Modifier.padding(innerPadding)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth()
      ) {
        viewInfo?.urls?.let {
          CapVideoPlayer(
            videoUrl = viewInfo?.urls?.getOrNull(0),
            title = animation?.titleCn ?: animation?.title ?: "暂无标题",
            episodeIndex = currentPlayingEpisodeIndex,
            showNextButton = false,
            showPreviousButton = false,
            navController = navController,
            initPosition = currentPosition,
            onPositionChange = {
              historyPosition = it
            },
            onLeadingBackButtonTab = {
              if (!isFullscreen) {
                navController.popBackStack()
                capPlayerViewModel.releasePlayer()
              }
            },
            onLandscapeChange = {
              isFullscreen = it
            },
            onPlayerError = {
              Toast.makeText(content, "播放出错,试着换一条线路: ${it.message}", Toast.LENGTH_SHORT)
                .show()
            }
          )
        } ?: run {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .aspectRatio(16f / 9),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator()
          }
        }

      }

      PrimaryTabRow(selectedTabIndex = tabIndex.value, Modifier.padding(horizontal = 10.dp)) {
        tabs.forEachIndexed { index, title ->
          Tab(selected = tabIndex.value == index, onClick = {
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
          0 -> AnimeInfoPage(animation, isLove) { isLove = it }
          1 -> EpisodePage(
            episodes = episodes, activeIndex = currentPlayingEpisodeIndex
          ) { newId, index ->
            if (index == currentPlayingEpisodeIndex || isLoading) return@EpisodePage
            onEpisodeChange(newId, index)
          }
        }
      }
    }
  }
}


/* ====================== 简介页 ====================== */
@Composable
fun AnimeInfoPage(
  animation: Animation?, isLove: Boolean = false, loveHandle: (Boolean) -> Unit
) {
  animation?.let { anime ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
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
                text = "发行日期: $it",
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
//            anime.infobox?.let { infoList ->
//              infoList.forEach { info ->
//                info.key?.let { key ->
//                  Text(
//                    text = "$key: ${info.value ?: "无"}",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.padding(top = 4.dp)
//                  )
//                }
//              }
//            }
          }
        }
      }
    }
  } ?: run {
    Box(
      modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) { Text("加载中...") }
  }
}

/* ====================== 选集页 ====================== */
@Composable
fun EpisodePage(
  episodes: List<String>, activeIndex: Int = 0, onEpisodeChange: (String, Int) -> Unit
) {
  if (episodes.isNotEmpty()) {
    episodes.let { episodeList ->
      LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        itemsIndexed(episodeList) { index, episode ->
          val isPlaying = activeIndex == index
          if (isPlaying) {
            Button(
              onClick = {}, modifier = Modifier.fillMaxWidth()
            ) { Text(text = "${index + 1}", maxLines = 1) }
          } else {
            OutlinedButton(
              onClick = {
                onEpisodeChange(episode, index)
              }, modifier = Modifier.fillMaxWidth()
            ) { Text(text = "${index + 1}", maxLines = 1) }
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
