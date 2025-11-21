package com.mikufans.ui.page

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.api.MetaService
import com.mikufans.util.GifLoader
import kotlinx.coroutines.launch
import org.anime.entity.animation.Animation
import org.anime.entity.animation.Staff
import org.anime.entity.base.Detail
import org.anime.entity.base.Media
import org.anime.entity.meta.SourceWithDelay
import org.anime.parser.HtmlParser

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
  var detail by rememberSaveable { mutableStateOf<Detail<out Media>?>(null) }
  var animationDetail by rememberSaveable { mutableStateOf<Detail<Animation>?>(null) }
  var staffInfo by rememberSaveable { mutableStateOf<Staff?>(null) }
  val coroutineScope = rememberCoroutineScope()
  var isLoading by remember { mutableStateOf(false) }
  var staffLoading by remember { mutableStateOf(false) }
  var targetApi by rememberSaveable { mutableStateOf<List<SourceWithDelay<out HtmlParser>>?>(null) }
  var id by rememberSaveable { mutableStateOf(id) }
  val apiSelect: () -> Unit = {
    //todo 选择api
  }
  val fetchDetail: () -> Unit = {
    isLoading = true
    coroutineScope.launch {
      if (id.isBlank()) {
        MetaService.fetchSearchSync(title) {
          Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }.takeIf { it.isNotEmpty() }.let { id = it?.get(0)?.id!! }
      }
      MetaService.fetchDetailSync(id) {
        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
      }?.let { animationDetail = it }
      MetaService.fetchStaffSync(id) {
        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
      }.let { staffInfo = it; staffLoading = false }
    }
    isLoading = false
  }
  LaunchedEffect(Unit) {
    if (detail != null || animationDetail != null) return@LaunchedEffect
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

          (detail != null || animationDetail != null) -> {
            Column(
              Modifier
                .fillMaxSize()
                .padding(horizontal = baseHorizontalPadding),
              verticalArrangement = Arrangement.spacedBy(baseHorizontalPadding)
            ) {

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(baseHorizontalPadding)
              ) {
                Column(Modifier.height(200.dp)) {
                  AsyncImage(
                    modifier = Modifier
                      .fillMaxHeight()
                      .aspectRatio(2.5f / 3f)
                      .clip(MaterialTheme.shapes.medium),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    model = animationDetail?.media?.coverUrls[0],
                    contentDescription = animationDetail?.media?.title ?: "暂无动画标题",
                    placeholder = GifLoader.gifPlaceholder(R.drawable.loading, context),
                  )
                }
                Column(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                ) {
                  val media = animationDetail?.media
                  Text(
                    media?.title ?: "暂无动画标题",
                    fontStyle = MaterialTheme.typography.titleMedium.fontStyle
                  )
                  media?.genre?.let {
                    Text(
                      text = media.genre,
                      color = Color.Gray,
                      fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    )
                  }
                  media?.totalEpisode?.let {
                    Text(
                      text = media.totalEpisode.toString(),
                      color = Color.Gray,
                      fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    )
                  }
                  media?.rating?.takeIf { it.isNotBlank() }?.let {
                    Text(
                      it + "/" + media.ratingCount + "人评分",
                      color = MaterialTheme.colorScheme.primary,
                      fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                    )
                  }
                }
              }
              animationDetail?.media?.description?.let {
                Card(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                ) {
                  Text("剧集简介", fontStyle = MaterialTheme.typography.titleMedium.fontStyle)
                  Text(
                    it,
                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle
                  )
                }
              }
              staffInfo?.let {
                staffInfo?.directors?.let { directors ->
                  LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(baseHorizontalPadding),
                  ) {
                    itemsIndexed(items = directors) { index, director ->
                      Card(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(MaterialTheme.shapes.medium)
                      ) {
                        Text("导演简介", fontStyle = MaterialTheme.typography.titleMedium.fontStyle)
                        Column() { }
                        AsyncImage(
                          modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(2.5f / 3f)
                            .clip(MaterialTheme.shapes.medium),
                          contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                          model = director.imageUrl[0],
                          contentDescription = director.nameCn ?: "暂无导演姓名",
                          placeholder = GifLoader.gifPlaceholder(R.drawable.loading, context),
                        )
                        Text(
                          director.nameCn ?: "暂无导演姓名",
                          fontStyle = MaterialTheme.typography.titleSmall.fontStyle
                        )
                        Text(
                          text = director.role ?: "暂无配音信息",
                          color = Color.Gray,
                          fontStyle = MaterialTheme.typography.titleSmall.fontStyle
                        )
                      }
                    }
                  }

                }
                staffInfo?.actors?.let { directors ->
                  LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(baseHorizontalPadding),
                  ) {
                    itemsIndexed(items = directors) { index, director ->
                      Card(
                        modifier = Modifier
                          .fillMaxWidth()
                          .clip(MaterialTheme.shapes.medium)
                      ) {
                        Text("导演简介", fontStyle = MaterialTheme.typography.titleMedium.fontStyle)
                        Column() { }
                        AsyncImage(
                          modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(2.5f / 3f)
                            .clip(MaterialTheme.shapes.medium),
                          contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                          model = director.imageUrl[0],
                          contentDescription = director.nameCn ?: "暂无演员姓名",
                          placeholder = GifLoader.gifPlaceholder(R.drawable.loading, context),
                        )
                        Text(
                          director.nameCn ?: "暂无演员姓名",
                          fontStyle = MaterialTheme.typography.titleSmall.fontStyle
                        )
                        Text(
                          text = director.role ?: "暂无配音信息",
                          color = Color.Gray,
                          fontStyle = MaterialTheme.typography.titleSmall.fontStyle
                        )
                      }
                    }
                  }

                }
              } ?: run {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                  CircularProgressIndicator()
                }
              }
            }
          }

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
