package com.mikufans.ui.page

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.ui.component.CapVideoPlayer
import com.mikufans.util.GifLoader
import com.mikufans.util.LocalStorage
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.Episode
import com.mikufans.xmd.miku.entiry.History
import com.mikufans.xmd.miku.entiry.PlayInfo
import com.mikufans.xmd.teto.service.impl.RedDrillBit
import com.mikufans.xmd.util.SourceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackPage(
  animeId: String,
  animeSubId: String,
  navController: NavController?,
  episodeList: List<Episode>,
  activity: ComponentActivity
) {
  val content = LocalContext.current
  val tabs = arrayOf("简介", "剧集")
  var isLove by rememberSaveable { mutableStateOf(false) }
  var historyList by rememberSaveable { mutableStateOf<List<History>>(emptyList()) }/* 播放状态持久化 */
  var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
  var historyPosition by rememberSaveable { mutableLongStateOf(0L) }

  var wasPlaying by rememberSaveable { mutableStateOf(true) }

  var currentPlayingEpisodeIndex by rememberSaveable { mutableIntStateOf(0) }
  var currentPlayingEpisodeId by rememberSaveable { mutableStateOf(episodeList[0].id) }
  var isLoading by rememberSaveable { mutableStateOf(false) }
  var playInfo by rememberSaveable { mutableStateOf(PlayInfo()) }
  var subject by rememberSaveable { mutableStateOf<Anime?>(null) }
  val episodes by rememberSaveable { mutableStateOf<List<Episode>?>(episodeList) }
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
  val coroutineScope = rememberCoroutineScope()
  var isFullscreen by rememberSaveable { mutableStateOf(false) }
  val sources = rememberSaveable { SourceUtil.getSourceWithDelay() }
  /* 历史记录保存 */
  DisposableEffect(Unit) {
    onDispose {
      val history = History(
        id = animeId,
        subId = animeSubId,
        name = subject?.name,
        nameCn = subject?.nameCn,
        cover = subject?.coverUrl,
        episodeId = currentPlayingEpisodeId,
        episodeIndex = currentPlayingEpisodeIndex,
        position = currentPosition - 5000,
        isLove = isLove,
        videoUrl = playInfo.currentEpisodeUrl,
        time = System.currentTimeMillis()
      )
      val list = historyList.toMutableList()
      val idx = list.indexOfFirst { it.subId == animeSubId }
      if (idx >= 0) list[idx] = history else list.add(history)
      LocalStorage.setList(content, "view:history", list)
//      CapPlayerViewModel
    }
  }

  /* 初始数据加载 */
  LaunchedEffect(Unit) {
    if (subject != null) {
      return@LaunchedEffect
    }
    isLoading = true
    historyList =
      LocalStorage.getList(content, "view:history", History::class.java)?.toMutableList()
        ?: mutableListOf()
    val idx = historyList.indexOfFirst { it.id == animeId }
    try {
      coroutineScope.launch(Dispatchers.IO) {
        subject = RedDrillBit().fetchSubject(animeSubId.toInt())
      }
      if (idx >= 0) {
        val tempPlayInfo = PlayInfo()
        currentPlayingEpisodeId = historyList[idx].episodeId
        currentPlayingEpisodeIndex = historyList[idx].episodeIndex ?: 0
        tempPlayInfo.currentEpisodeUrl = historyList[idx].videoUrl
        currentPosition = historyList[idx].position ?: 0L
        playInfo = tempPlayInfo
        isLove = historyList[idx].isLove
      } else {
        coroutineScope.launch(Dispatchers.IO) {
          playInfo.currentEpisodeUrl ?: let {
//            playInfo = GiligiliAccessPoint().getVideoUrl(currentPlayingEpisodeId)
            playInfo = sources[0].service.getPlayInfo(currentPlayingEpisodeId)
          }
        }
      }
    } catch (e: Exception) {
      Log.e("player.error", e.toString())
      launch(Dispatchers.Main) {
        Toast.makeText(content, "加载数据失败", Toast.LENGTH_SHORT).show()
      }
    } finally {
      isLoading = false
    }
  }
  /* UI 开始 */
  Scaffold(topBar = {
    if (!isLandscape()) {
      TopAppBar(
        title = {
          Text(
            subject?.nameCn ?: subject?.name ?: "",
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        navigationIcon = {
          IconButton(onClick = { navController?.popBackStack() }) {
            Icon(
              Icons.Outlined.ArrowBackIosNew,
              contentDescription = "back",
            )
          }
        })
    }
  }, content = { innerPadding ->
    Column(modifier = Modifier.padding(if (isLandscape()) PaddingValues(0.dp) else innerPadding)) {
      /* 播放器区域 */
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 9f)
      ) {
        if (isLoading || playInfo.currentEpisodeUrl == null) {
          Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
          }
        } else {
          CapVideoPlayer(
            videoUrl = playInfo.currentEpisodeUrl!!,
            position = currentPosition,
            activity = activity,
            modifier = if (isLandscape()) Modifier.padding(horizontal = 50.dp) else Modifier.fillMaxSize(),
            onProcessChange = {
              currentPosition = it
            },
          )
        }
      }

      TabRow(selectedTabIndex = tabIndex.value, Modifier.padding(horizontal = 10.dp)) {
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
          0 -> AnimeInfoPage(subject, isLove) { isLove = it }
          1 -> EpisodePage(
            episodes = episodes, activeIndex = currentPlayingEpisodeIndex
          ) { newId, index ->
            if (index == currentPlayingEpisodeIndex || isLoading) return@EpisodePage
            isLoading = true
            currentPlayingEpisodeIndex = index
            currentPlayingEpisodeId = newId
            try {
              coroutineScope.launch(Dispatchers.IO) {
                playInfo = sources[0].service.getPlayInfo(currentPlayingEpisodeId)
              }
            } catch (e: Exception) {
              isLoading = false
              Toast.makeText(
                navController?.context,
                "错误:${e.message}",
                Toast.LENGTH_SHORT
              ).show()
            } finally {
            }
            currentPosition = 0L
          }
        }
      }
    }
  })
}

@Composable
fun isLandscape(): Boolean =
  LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE


/* ====================== 简介页 ====================== */
@Composable
fun AnimeInfoPage(
  subject: Anime?, isLove: Boolean = false, loveHandle: (Boolean) -> Unit
) {
  subject?.let { anime ->
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
            model = anime.coverUrl,
            contentDescription = anime.nameCn ?: anime.name,
            placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current),
            contentScale = ContentScale.Crop
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = anime.nameCn ?: anime.name ?: "未知标题",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            anime.date?.let {
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
            anime.totalEpisodes?.let {
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
  episodes: List<Episode>?, activeIndex: Int = 0, onEpisodeChange: (String, Int) -> Unit
) {
  episodes?.let { episodeList ->
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
              episode.id?.let { id ->
                onEpisodeChange(id, index)
              }
            }, modifier = Modifier.fillMaxWidth()
          ) { Text(text = "${index + 1}", maxLines = 1) }
        }
      }
    }
  } ?: run {
    Box(
      modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) { Text("暂无剧集信息") }
  }
}

fun formatTime(ms: Long): String {
  if (ms <= 0) return "00:00"
  val s = (ms / 1000).toInt()
  val m = s / 60
  val r = s % 60
  return String.format(Locale.getDefault(), "%02d:%02d", m, r)
}
