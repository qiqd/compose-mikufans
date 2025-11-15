package com.mikufans.ui.page

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.alibaba.fastjson.JSON
import com.mikufans.R
import com.mikufans.ui.component.EmptyCompose
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.GifLoader
import com.mikufans.xmd.util.StringMatchUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.anime.api.AnimationApi
import org.anime.api.ComicApi
import org.anime.api.NovelApi
import org.anime.entity.bangmi.SourceWithDelay
import org.anime.entity.base.Detail
import org.anime.entity.base.Media
import org.anime.parser.HtmlParser
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPage(
  id: String,
  title: String,
  type: String,
  navController: NavController,
  baseHorizontalPadding: Dp
) {
  val context = LocalContext.current
//  var subject by rememberSaveable { mutableStateOf<Animation?>(null) }
  var detail by rememberSaveable { mutableStateOf<Detail<out Media>?>(null) }
  val coroutineScope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  var isLoadLine by remember { mutableStateOf(true) }
  val animationApi = AnimationApi.SOURCES_WITH_DELAY
  val comicApi = ComicApi.SOURCES_WITH_DELAY
  val novelApi = NovelApi.SOURCES_WITH_DELAY
  var targetApi by rememberSaveable { mutableStateOf<List<SourceWithDelay<out HtmlParser>>?>(null) }
  var id by rememberSaveable { mutableStateOf(id) }
  val apiSelect: () -> Unit = {
    when (type) {
      Navigation.TYPE_ANIMATION -> {
        targetApi = animationApi
      }

      Navigation.TYPE_COMIC -> targetApi = comicApi
      Navigation.TYPE_NOVEL -> targetApi = novelApi
    }
  }
  val fetchDetail: () -> Unit = {
    isLoading = true
    val htmlParser = targetApi!![0].htmlParser
    coroutineScope.launch(Dispatchers.IO) {
      try {
        if (id.isBlank()) {
          val media = htmlParser.fetchSearchSync(title, 1, 10)
          val mapToMedia = media.associateBy { it.titleCn }
          val bestMatch =
            StringMatchUtil.findBestMatchWithJaroWinkler(media.map { it.titleCn }, title)
          mapToMedia[bestMatch].let { id = it?.id ?: "" }
        }
        detail = htmlParser.fetchDetailSync(id)
        launch(Dispatchers.Main) { isLoading = false }
      } catch (e: Exception) {
        e.printStackTrace()
        launch(Dispatchers.Main) {
          isLoading = false
          Toast.makeText(context, "获取数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
      } finally {
        isLoading = false
        isLoadLine = false
      }
    }
  }
  LaunchedEffect(Unit) {
    if (detail != null) return@LaunchedEffect
    apiSelect()
    fetchDetail()
  }

  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = {
      TopAppBar(
        title = { Text("详情") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = "back")
          }
        }
      )
    },
    content = { innerPadding ->
      Box(modifier = Modifier.padding(innerPadding)) {
        when {
          isLoading -> Box(
            modifier = Modifier
              .fillMaxSize()
              .wrapContentSize(Alignment.Center)
          ) { CircularProgressIndicator() }

          detail != null -> AnimeDetailContent(
            coroutineScope,
            id,
            detail!!,
            navController,
            isLoadLine
          )

          else -> Box(
            modifier = Modifier
              .fillMaxSize()
              .wrapContentSize(Alignment.Center)
          ) { Text("暂无数据") }
        }
      }
    }
  )
}

@Composable
private fun AnimeDetailContent(
  coroutineScope: CoroutineScope,
  animeId: String,
  detail: Detail<out Media>,
  navController: NavController,
  isLoadLine: Boolean
) {

  val tabs = arrayOf("路线", "简介", "角色", "制作信息")
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(modifier = Modifier.fillMaxWidth()) { AnimeHeader(detail.media) }
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxSize()
    ) {
      PrimaryTabRow(selectedTabIndex = tabIndex.value) {
        tabs.forEachIndexed { index, title ->
          Tab(
            text = { Text(title) },
            selected = tabIndex.value == index,
            onClick = {
              coroutineScope.launch {
                pagerState.animateScrollToPage(index)
              }
            }
          )
        }
      }
      HorizontalPager(
        state = pagerState,
        verticalAlignment = Alignment.Top,
        contentPadding = PaddingValues(top = 8.dp),
        modifier = Modifier.fillMaxSize()
      ) { index ->
        when (index) {
          0 -> PlayLine(
            detail = detail,
            isLoadLine = isLoadLine,
            navController = navController,
            animeId = animeId,
          )

          1 -> SimpleIntroduction(detail.media)
          2 -> ActorInformation(detail.media)
          3 -> InformationSteeler(detail.media)
        }
      }
    }
  }
}

/* 5. 头部信息全部来自 Subject */
@Composable
private fun AnimeHeader(media: Media) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    AsyncImage(
      model = media.coverUrls[0],
      contentDescription = media.titleCn,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .width(150.dp)
        .aspectRatio(2 / 3f),
      placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current)
    )

    Column(Modifier.weight(1f)) {
      media.titleCn?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
      media.title?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Normal,
          color = Color.Gray
        )
      }
//      media.platform?.let {
//        Text(
//          text = it,
//          style = MaterialTheme.typography.bodyLarge,
//          color = Color.Gray
//        )
//      }
      media.status?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodyLarge,
          color = Color.Gray
        )
      }
//      media.totalEpisode?.let {
//        Text(
//          text = "总集数: $it",
//          style = MaterialTheme.typography.bodyLarge,
//          color = Color.Gray
//        )
//      }
      media.genre?.let {
        Text(
          text = media.genre ?: "",
          style = MaterialTheme.typography.bodyLarge,
          color = Color.Gray
        )
      }
      Spacer(Modifier.height(8.dp))

      media.rating?.let {
        Text("评分: $it", style = MaterialTheme.typography.bodyMedium)
      }


//      media.airDate?.let {
//        Text("年份: $it", style = MaterialTheme.typography.bodyMedium)
//      }
    }
  }
}

@Composable
fun PlayLine(
  detail: Detail<out Media>,
  isLoadLine: Boolean,
  navController: NavController,
  animeId: String,
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    detail.episodes?.let { sourceList ->
      itemsIndexed(sourceList) { index, source ->
        source.episodes?.let { episodes ->
          Card(
            modifier = Modifier
              .clip(MaterialTheme.shapes.medium)
              .fillMaxWidth()
              .clickable {
                val json = URLEncoder.encode(JSON.toJSONString(episodes), "UTF-8")
                //跳转,todo
                Navigation.navigateToPlayer(
                  id = animeId,
                  title = detail.media.titleCn ?: "",
                  episodes = episodes.map { it.id },
                  navController = navController,
                )
              }
          ) {
            Column(Modifier.padding(16.dp)) {
              Text(
                text = "${source.name ?: "播放路线"} ${index + 1}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    } ?: run {
      item {
        if (isLoadLine) {
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) { CircularProgressIndicator() }
        } else {
          Card {
            Column(Modifier.padding(16.dp)) {
              Text(
                text = "暂无播放路线",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun SimpleIntroduction(media: Media) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
      Text("简介", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Text(
        text = media.description ?: "暂无简介",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

@Composable
fun ActorInformation(media: Media) {
  EmptyCompose("暂无演员信息")
  //todo 2025-10-22
}

@Composable
fun InformationSteeler(media: Media) {
  EmptyCompose("暂无信息")
  //todo 2025-10-22
}
