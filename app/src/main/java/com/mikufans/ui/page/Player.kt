package com.mikufans.ui.page

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mikufans.R
import com.mikufans.util.LocalStorage
import com.mikufans.xmd.access.AAFunAccessPoint
import com.mikufans.xmd.miku.entiry.Episode
import com.mikufans.xmd.miku.entiry.History
import com.mikufans.xmd.miku.entiry.PlayInfo
import com.mikufans.xmd.teto.entity.SubjectSearch
import com.mikufans.xmd.teto.service.impl.RedDrillBit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Player(animeId: Int, navController: NavController?, episodeList: List<Episode>) {
  val content = LocalContext.current
  val tabs = arrayOf("简介", "剧集")
  var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
  var currentPlayingEpisodeIndex by rememberSaveable { mutableIntStateOf(0) }
  var currentPlayingEpisodeId by remember { mutableStateOf(episodeList[0].id) }
  var isLoading by rememberSaveable { mutableStateOf(true) }
  var playInfo by rememberSaveable { mutableStateOf<PlayInfo?>(null) }
  var subject by rememberSaveable { mutableStateOf<SubjectSearch.Subject?>(null) }
  val episodes by rememberSaveable { mutableStateOf<List<Episode>?>(episodeList) }
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
  val coroutineScope = rememberCoroutineScope()
  LaunchedEffect(Unit) {
    isLoading = true;
    coroutineScope.launch(Dispatchers.IO) {
      Log.i("player.source", episodes.toString())
      try {
        subject = RedDrillBit().fetchSubject(animeId)
        playInfo = AAFunAccessPoint().getVideoUrl(currentPlayingEpisodeId)
      } catch (e: Exception) {
        Log.e("player.error", e.toString())
        launch(Dispatchers.Main) {
          Toast.makeText(content, "加载数据失败", Toast.LENGTH_SHORT).show()
        }
      } finally {
        isLoading = false;
      }
    }
  }
  DisposableEffect(Unit) {
    onDispose {
      val history = History(
        id = animeId,
        name = subject?.name,
        cover = subject?.images?.large,
        episodeId = currentPlayingEpisodeId,
        episodeIndex = currentPlayingEpisodeIndex,
        position = currentPosition,
        time = System.currentTimeMillis()
      )
      val oldList = LocalStorage.getList(content, "view:history", History::class.java)
        ?.toMutableList() ?: mutableListOf()
      val index = oldList.indexOfFirst { it.id == animeId }

      if (index >= 0) {
        oldList[index] = history
      } else {
        oldList.add(history)
      }
      LocalStorage.setList(content, "view:history", oldList)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Player", textAlign = TextAlign.Center) },
        navigationIcon = {
          Icon(
            Icons.Outlined.ArrowBackIosNew,
            contentDescription = null,
            modifier = Modifier.clickable { navController?.popBackStack() })
        })
    },
    content = { innerPadding ->
      Column(modifier = Modifier.padding(innerPadding)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
        ) {
          if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
              CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
          } else {
            ExoVideoPlayer(playInfo?.currentEpisodeUrl!!, onExit = { currentPosition = it })
          }
        }
        TabRow(selectedTabIndex = tabIndex.value, Modifier.padding(horizontal = 10.dp)) {
          tabs.forEachIndexed { index, string ->
            Tab(selected = tabIndex.value == index, onClick = {
              coroutineScope.launch { pagerState.animateScrollToPage(index) }
            }) {
              Text(text = string, modifier = Modifier.padding(10.dp))
            }
          }
        }
        HorizontalPager(
          state = pagerState,
          modifier = Modifier.fillMaxSize(),

          ) { page ->
          when (page) {
            0 -> AnimeInfoPage(subject)
            1 -> EpisodePage(episodes, currentPlayingEpisodeId!!) { newId, index ->
              isLoading = true
              currentPlayingEpisodeIndex = index
              currentPlayingEpisodeId = newId;
              coroutineScope.launch(Dispatchers.IO) {
                playInfo = AAFunAccessPoint().getVideoUrl(currentPlayingEpisodeId)
                isLoading = false
              }
            }
          }
        }
      }
    })
}

@Composable
fun ExoVideoPlayer(videoUrl: String, onExit: (Long) -> Unit = {}) {
  val context = LocalContext.current
  val exoPlayer = remember(context) {
    ExoPlayer.Builder(context).build().apply {
      val mediaItem = MediaItem.fromUri(videoUrl.replaceFirst("http://", "https://"))
      setMediaItem(mediaItem)
      prepare()
      playWhenReady = true
    }
  }

  DisposableEffect(exoPlayer) {
    onDispose {
      exoPlayer.release()
    }
  }
  DisposableEffect(exoPlayer) {
    onDispose {
      // 组件销毁时读一次进度
      val position = exoPlayer.currentPosition
      onExit(position)          // 把最终进度抛出去
      exoPlayer.release()
    }
  }
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(16f / 9f)
  ) {
    AndroidView(
      factory = { PlayerView(context) },
      update = { playerView ->
        playerView.player = exoPlayer
      },
      modifier = Modifier.fillMaxSize()
    )
  }
}


@Composable
fun AnimeInfoPage(subject: SubjectSearch.Subject?) {
  subject?.let { anime ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 封面和基本信息
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          AsyncImage(
            modifier = Modifier
              .width(120.dp)
              .aspectRatio(2 / 3f)
              .clip(MaterialTheme.shapes.medium),
            model = anime.images?.large ?: anime.images?.medium,
            contentDescription = anime.nameCn ?: anime.name,
            placeholder = painterResource(R.drawable.ahhhh),
            contentScale = ContentScale.Crop
          )

          Column(
            modifier = Modifier.weight(1f)
          ) {
            Text(
              text = anime.nameCn ?: anime.name ?: "未知标题",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )

            anime.date?.let { date ->
              Text(
                text = "发行日期: $date",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
              )
            }

            anime.rating?.score?.let { score ->
              Text(
                text = "评分: $score",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
              )
            }

            anime.eps?.let { eps ->
              Text(
                text = "集数: $eps",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }
      }

      // 简介
      anime.summary?.let { summary ->
        item {
          Card(
            modifier = Modifier.fillMaxWidth()
          ) {
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

      // 其他信息
      item {
        Card(
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "详细信息",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )

            anime.platform?.let { platform ->
              Text(
                text = "平台: $platform",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
              )
            }

            anime.infobox?.let { infoList ->
              infoList.forEach { info ->
                info.key?.let { key ->
                  Text(
                    text = "$key: ${info.value ?: "无"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  } ?: run {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text("加载中...")
    }
  }
}

@Composable
fun EpisodePage(
  episodes: List<Episode>?,
  currentPlayingEpisodeId: String,
  onEpisodeChange: (String, Int) -> Unit
) {
  var activeIndex by rememberSaveable { mutableIntStateOf(0) };
  episodes?.let { episodeList ->
    LazyVerticalGrid(
      columns = GridCells.Fixed(4),
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      itemsIndexed(episodeList) { index, episode ->
        val isPlaying = activeIndex == index
        if (isPlaying) {
          Button(
            onClick = {},
            modifier = Modifier
              .fillMaxWidth()
          ) {
            Text(
              text = "${index + 1}",
              maxLines = 1
            )
          }
        } else {
          OutlinedButton(
            onClick = {
              episode.id?.let { id ->
                activeIndex = index
                onEpisodeChange(id, index)
                Log.i(" Player1", id)
              }
            },
            modifier = Modifier
              .fillMaxWidth()
          ) {
            Text(
              text = "${index + 1}",
              maxLines = 1
            )
          }
        }
      }
    }
  } ?: run {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text("暂无剧集信息")
    }
  }
}
