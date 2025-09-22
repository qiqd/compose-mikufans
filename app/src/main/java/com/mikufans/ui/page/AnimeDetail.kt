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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.alibaba.fastjson.JSON
import com.example.mikufans.R
import com.mikufans.xmd.access.AAFunAccessPoint
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.AnimeDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetail(animeId: Int, animeName: String, navController: NavController) {
  var animeDetail by rememberSaveable { mutableStateOf<AnimeDetail?>(null) }
  val coroutineScope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(animeDetail == null) }
  var error by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    if (animeDetail != null) {
      return@LaunchedEffect
    }
    isLoading = true
    error = null
    coroutineScope.launch(Dispatchers.IO) {
      try {
        val result = AAFunAccessPoint().getAnimeInfo(animeName, animeId)
        launch(Dispatchers.Main) {
          animeDetail = result
//          Log.e("result", result.toString())
          isLoading = false
        }
      } catch (e: Exception) {
        e.printStackTrace()
        launch(Dispatchers.Main) {
          error = e.message ?: "获取数据失败"
          isLoading = false
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
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "返回"
            )
          }
        }
      )
    },
    content = { innerPadding ->
      Box(modifier = Modifier.padding(innerPadding)) {
        when {
          isLoading -> {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
            ) {
              CircularProgressIndicator()
            }
          }

          error != null -> {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
            ) {
              Text("加载失败: $error")
            }
          }

          animeDetail != null -> {
            AnimeDetailContent(animeDetail!!, navController, animeId)
          }

          else -> {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
            ) {
              Text("暂无数据")
            }
          }
        }
      }
    }
  )
}

@Composable
fun AnimeDetailContent(animeDetail: AnimeDetail, navController: NavController, animeId: Int) {
  val anime = animeDetail.anime
  val sources = animeDetail.sources

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 封面和基本信息
    item {
      anime?.let {
        AnimeHeader(anime)
      }
    }

    // 简介
    item {
      anime?.description?.let { description ->
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
              text = description,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(top = 8.dp)
            )
          }
        }
      }
    }

    // 播放路线和剧集列表
    sources?.let { sourceList ->
      itemsIndexed(sourceList) { index, source ->
        source.episodes?.let { episodes ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
//                Log.i("card click:", source.toString())
                var stringEpisode = JSON.toJSONString(episodes)
                stringEpisode = URLEncoder.encode(stringEpisode, "UTF-8")
                navController.navigate("animePlayer/$animeId/$stringEpisode") {
                  launchSingleTop = true
                  restoreState = true
                }
              }
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
  }
}

@Composable
fun AnimeHeader(anime: Anime) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    AsyncImage(
      modifier = Modifier
        .width(150.dp)
        .aspectRatio(2 / 3f),
      model = anime.coverUrl,
      contentDescription = null,
      placeholder = painterResource(R.drawable.ahhhh),
    )
    Column(
      modifier = Modifier.weight(1f)
    ) {
      Text(
        text = anime.title ?: "未知标题",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      anime.rating?.let { rating ->
        Text(
          text = "评分: $rating",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      anime.type?.let { type ->
        Text(
          text = "类型: $type",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      anime.year?.let { year ->
        Text(
          text = "年份: $year",
          style = MaterialTheme.typography.bodyMedium
        )
      }

      anime.status?.let { status ->
        Text(
          text = "状态: $status",
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
}
