package com.mikufans.ui.page

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.alibaba.fastjson.JSON
import com.mikufans.R
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.GifLoader
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.teto.service.impl.RedDrillBit
import com.mikufans.xmd.util.SourceUtil
import com.mikufans.xmd.util.StringMatchUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetail(
  animeId: String = "",
  animeSubId: Int,
  animeName: String,
  navController: NavController
) {
  val context = LocalContext.current
  var subject by rememberSaveable { mutableStateOf<Anime?>(null) }
  var animeDetail by rememberSaveable { mutableStateOf<AnimeDetail?>(null) }
  val coroutineScope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  var isLoadLine by remember { mutableStateOf(true) }
  val sources = rememberSaveable { SourceUtil.getSourceWithDelay() }
  var id by rememberSaveable { mutableStateOf(animeId) }
  LaunchedEffect(Unit) {
    if (subject != null || animeDetail != null) {
      return@LaunchedEffect
    }
    isLoading = true
    coroutineScope.launch(Dispatchers.IO) {
      try {
        val subjectSearch = RedDrillBit().fetchSubject(animeSubId)
        subject = subjectSearch
        isLoading = false
        if (id.isEmpty()) {
          val searchResult = sources[0].service.getSearchResult(animeName, 1, 10)
          val nameCnMap = searchResult.associateBy { it.nameCn }
          val bestMatch =
            StringMatchUtil.findBestMatchWithJaroWinkler(nameCnMap.keys.toList(), animeName)
          id = nameCnMap[bestMatch]?.id!!
        }
        animeDetail = sources[0].service.getAnimeDetail(id)
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
    topBar = {
      TopAppBar(
        title = { Text("动漫详情") },
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = "返回")
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
  animeId: String,
  subject: Anime,
  animeDetail: AnimeDetail?,
  navController: NavController,
  isLoadLine: Boolean
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    /* 封面与基本信息 */
    item { AnimeHeader(subject) }

    /* 4. 播放路线保持原逻辑不动 */
    animeDetail?.sources?.let { sourceList ->
      itemsIndexed(sourceList) { index, source ->
        source.episodes?.let { episodes ->
          Card(
            modifier = Modifier
              .clip(MaterialTheme.shapes.medium)
              .fillMaxWidth()
              .clickable {
                val json = URLEncoder.encode(JSON.toJSONString(episodes), "UTF-8")
                Navigation.navigateToAnimePlayer(
                  navController,
                  animeId,
                  subject.subId.toString(),
                  json
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
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

    /* 简介 */
    item {
      Card(Modifier.fillMaxWidth()) {
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
      Text(
        text = subject.name ?: "未知标题",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )

      Spacer(Modifier.height(8.dp))

      subject.rating?.let {
        Text("评分: $it", style = MaterialTheme.typography.bodyMedium)
      }


      subject.date?.let {
        Text("年份: $it", style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}
