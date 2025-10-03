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
import com.mikufans.xmd.access.GiligiliAccessPoint
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.teto.entity.SubjectSearch
import com.mikufans.xmd.teto.service.impl.RedDrillBit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetail(animeId: Int, animeName: String, navController: NavController) {
  val context = LocalContext.current
  var subject by rememberSaveable { mutableStateOf<SubjectSearch.Subject?>(null) }
  var animeDetail by rememberSaveable { mutableStateOf<AnimeDetail?>(null) }
  val coroutineScope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    if (subject != null || animeDetail != null) {
      return@LaunchedEffect
    }
    isLoading = true
    coroutineScope.launch(Dispatchers.IO) {
      try {
        subject = RedDrillBit().fetchSubject(animeId)
        isLoading = false
        animeDetail = GiligiliAccessPoint().getAnimeInfo(animeName, animeId)
        launch(Dispatchers.Main) { isLoading = false }
      } catch (e: Exception) {
        e.printStackTrace()
        launch(Dispatchers.Main) {
          isLoading = false
          Toast.makeText(context, "获取数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

          subject != null -> AnimeDetailContent(subject!!, animeDetail, navController, animeId)
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
  subject: SubjectSearch.Subject,
  animeDetail: AnimeDetail?,
  navController: NavController,
  animeId: Int
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
                Navigation.navigateToAnimePlayer(navController, animeId.toString(), json)
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
    }

    /* 简介 */
    item {
      Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
          Text("简介", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(
            text = subject.summary ?: "暂无简介",
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
private fun AnimeHeader(subject: SubjectSearch.Subject) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    AsyncImage(
      model = subject.images?.large ?: subject.images?.medium,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .width(150.dp)
        .aspectRatio(2 / 3f),
      placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current)
    )

    Column(Modifier.weight(1f)) {
      Text(
        text = subject.nameCn ?: subject.name ?: "未知标题",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )

      Spacer(Modifier.height(8.dp))

      subject.rating?.score?.let {
        Text("评分: $it", style = MaterialTheme.typography.bodyMedium)
      }


      subject.date?.let {
        Text("年份: ${it.take(4)}", style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}
