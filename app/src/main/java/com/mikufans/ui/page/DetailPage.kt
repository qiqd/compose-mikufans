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
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.teto.service.impl.RedDrillBit
import com.mikufans.xmd.util.SourceUtil
import com.mikufans.xmd.util.StringMatchUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPage(
  animeId: String = "",
  playSource: String = "",
  animeSubId: Int,
  animeName: String,
  navController: NavController,
  baseHorizontalPadding: Dp
) {
  val context = LocalContext.current
  var subject by rememberSaveable { mutableStateOf<Anime?>(null) }
  var animeDetail by rememberSaveable { mutableStateOf<AnimeDetail?>(null) }
  val coroutineScope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  var isLoadLine by remember { mutableStateOf(true) }
  val sources = SourceUtil.getSourceWithDelay()
  var id by rememberSaveable { mutableStateOf(animeId) }
  LaunchedEffect(Unit) {
    if (subject != null || animeDetail != null) {
      return@LaunchedEffect
    }
    isLoading = true
    coroutineScope.launch(Dispatchers.IO) {
      val service = sources[0].service
      try {
        val subjectSearch = RedDrillBit().fetchSubject(animeSubId)
        subject = subjectSearch
        isLoading = false
        // 没有id时，本地历史记录为空时，使用搜索结果
//        if (id.isEmpty()) {
//          val searchResult = service.getSearchResult(animeName, 1, 10)
//          val nameCnMap = searchResult.associateBy { it.nameCn }
//          val bestMatch =
//            StringMatchUtil.findBestMatchWithJaroWinkler(nameCnMap.keys.toList(), animeName)
//          id = nameCnMap[bestMatch]?.id!!
//        }
//        animeDetail = service.getAnimeDetail(id)
        val searchResult = service.getSearchResult(subjectSearch.nameCn, 1, 10)
        val nameCnMap = searchResult.associateBy { it.nameCn }
        val bestMatch = StringMatchUtil.findBestMatchWithJaroWinkler(
          nameCnMap.keys.toList(),
          subjectSearch.nameCn
        )
        val targetAnime = nameCnMap[bestMatch]
        animeDetail = service.getAnimeDetail(targetAnime?.id)
        id = targetAnime?.id!!
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

          subject != null -> AnimeDetailContent(
            coroutineScope,
            id,
            subject!!,
            animeDetail,
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

/* 3. 头部+简介改用 Subject */
@Composable
private fun AnimeDetailContent(
  coroutineScope: CoroutineScope,
  animeId: String,
  subject: Anime,
  animeDetail: AnimeDetail?,
  navController: NavController,
  isLoadLine: Boolean
) {

  val tabs = arrayOf("路线", "简介", "角色", "制作信息")
//  val coroutineScope = rememberCoroutineScope()
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(modifier = Modifier.fillMaxWidth()) { AnimeHeader(subject) }
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
            animeDetail = animeDetail,
            isLoadLine = isLoadLine,
            navController = navController,
            animeId = animeId,
            subject = subject
          )

          1 -> SimpleIntroduction(subject)
          2 -> ActorInformation(subject)
          3 -> InformationErstellen(subject)
        }
      }
    }
  }
}

/* 5. 头部信息全部来自 Subject */
@Composable
private fun AnimeHeader(subject: Anime) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    AsyncImage(
      model = subject.coverUrl,
      contentDescription = subject.name,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .width(150.dp)
        .aspectRatio(2 / 3f),
      placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current)
    )

    Column(Modifier.weight(1f)) {
      subject.nameCn?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
      subject.name?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Normal,
          color = Color.Gray
        )
      }
      subject.platform?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodyLarge,
          color = Color.Gray
        )
      }
      subject.status?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodyLarge,
          color = Color.Gray
        )
      }
      subject.ariDate?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.bodyLarge,
          color = Color.Gray
        )
      }
      subject.ariDate?.let {
        Text(
          text = subject.totalEpisodes.toString(),
          style = MaterialTheme.typography.bodyLarge,
          color = Color.Gray
        )
      }
      Spacer(Modifier.height(8.dp))

      subject.rating?.let {
        Text("评分: $it", style = MaterialTheme.typography.bodyMedium)
      }


      subject.ariDate?.let {
        Text("年份: $it", style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

@Composable
fun PlayLine(
  animeDetail: AnimeDetail?,
  isLoadLine: Boolean,
  navController: NavController,
  animeId: String,
  subject: Anime
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    animeDetail?.sources?.let { sourceList ->
      itemsIndexed(sourceList) { index, source ->
        source.episodes?.let { episodes ->
          Card(
            modifier = Modifier
              .clip(MaterialTheme.shapes.medium)
              .fillMaxWidth()
              .clickable {
                val json = URLEncoder.encode(JSON.toJSONString(episodes), "UTF-8")
                //跳转
                Navigation.navigateToAnimePlayer(
                  navController,
                  animeId,
                  subject.subId.toString(),
                  json,
                  subject
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
fun SimpleIntroduction(subject: Anime) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(Modifier.padding(16.dp)) {
      Text("简介", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Text(
        text = subject.description ?: "暂无简介",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

@Composable
fun ActorInformation(subject: Anime) {
  EmptyCompose("暂无演员信息")
  //todo 2025-10-22
}

@Composable
fun InformationErstellen(subject: Anime) {
  EmptyCompose("暂无信息")
  //todo 2025-10-22
}
