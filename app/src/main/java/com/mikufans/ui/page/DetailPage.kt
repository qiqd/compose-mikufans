package com.mikufans.ui.page

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.api.AnimationService
import com.mikufans.api.MetaService
import com.mikufans.ui.component.EmptyCompose
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.GifLoader
import com.mikufans.util.GlobalSharedValue
import com.mikufans.util.StringMatchUtil
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
  val scope = rememberCoroutineScope()
  val tabs = arrayOf("简介", "导演", "演员", "制片", "编剧", "动画")
  var detail by rememberSaveable { mutableStateOf<Detail<out Media>?>(null) }
  var animationDetail by rememberSaveable { mutableStateOf<Detail<Animation>?>(null) }
  var staffInfo by rememberSaveable { mutableStateOf<Staff?>(null) }
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  var targetApis by rememberSaveable { mutableStateOf<List<SourceWithDelay<out HtmlParser>>?>(null) }
  var subId by rememberSaveable { mutableStateOf(id) }
  var id by rememberSaveable { mutableStateOf(id) }
  var love by rememberSaveable() { mutableStateOf(false) }
  val fetchDetail: () -> Unit = {
    scope.launch {
      if (id.isBlank()) {
        AnimationService.fetchSearchSync(title) {
          scope.launch { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
        }.let { animations ->
          val associateBy = animations.associateBy { it.titleCn }
          val best =
            StringMatchUtil.findBestMatchWithJaroWinkler(animations.map { it.titleCn }, title)
          associateBy[best]?.let { id = it.id }
        }
      }
      AnimationService.fetchDetailSync(id) {
        scope.launch { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
      }?.let { detail ->
        GlobalSharedValue.animationDetail = detail
      }
      MetaService.fetchSearchSync(title) {
        scope.launch { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
      }.takeIf { it.isNotEmpty() }?.let { subId = it.first().id }
      MetaService.fetchDetailSync(subId) {
        scope.launch { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
      }?.let { animationDetail = it }
      MetaService.fetchStaffSync(subId) {
        scope.launch { Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show() }
      }?.let { staffInfo = it }
    }
  }

  LaunchedEffect(Unit) {
    if (detail != null || animationDetail != null) return@LaunchedEffect
    fetchDetail()
  }


  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding), topBar = {
      TopAppBar(title = { Text("详情") }, navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
          Icon(
            imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = "back"
          )
        }
      })
    },
    floatingActionButton = {
      Column {
        AnimatedVisibility(visible = animationDetail != null, enter = fadeIn(), exit = fadeOut()) {
          OutlinedButton(onClick = {
            Navigation.navigateToPlayer(id, subId, animationDetail!!.media.titleCn, navController)
          }) {
            Icon(Icons.Default.PlayArrow, contentDescription = "play")
          }
        }
        OutlinedButton(onClick = {
          love = !love; Toast.makeText(
          context,
          if (love) "收藏成功" else "取消收藏",
          Toast.LENGTH_SHORT
        ).show()
        }) {
          if (love) {
            Icon(
              imageVector = Icons.Default.Favorite,
              tint = MaterialTheme.colorScheme.primary,
              contentDescription = "love"
            )
          } else {
            Icon(
              imageVector = Icons.Default.FavoriteBorder,

              contentDescription = "unlove"
            )
          }
        }
      }
    },
    floatingActionButtonPosition = FabPosition.End
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    ) {
      if (animationDetail == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
          CircularProgressIndicator()
        }
      } else {
        HeaderRow(media = animationDetail!!, baseHorizontalPadding = baseHorizontalPadding)
        if (staffInfo == null) {
          Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
          }
        } else {
          Column(
            Modifier
              .weight(1f)
              .padding(top = baseHorizontalPadding)
          ) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
              tabs.forEachIndexed { index, title ->
                Tab(
                  selectedContentColor = MaterialTheme.colorScheme.primary,
                  unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  text = { Text(title) },
                  selected = pagerState.currentPage == index,
                  onClick = { scope.launch { pagerState.animateScrollToPage(index) } })
              }
            }
            HorizontalPager(
              state = pagerState,
              pageSpacing = baseHorizontalPadding,
              contentPadding = PaddingValues(vertical = baseHorizontalPadding),
              verticalAlignment = Alignment.Top
            ) { pageIndex ->
              when (pageIndex) {
                0 -> ExpandableDescription(
                  description = animationDetail?.media?.description ?: "暂无简介",
                  baseHorizontalPadding = baseHorizontalPadding
                )

                1 -> StaffCard(
                  title = tabs[pageIndex],
                  list = staffInfo?.directors?.toList(),
                  baseHorizontalPadding = baseHorizontalPadding
                )

                2 -> StaffCard(
                  title = tabs[pageIndex],
                  list = staffInfo?.actors?.toList(),
                  baseHorizontalPadding = baseHorizontalPadding
                )

                3 -> StaffCard(
                  title = tabs[pageIndex],
                  list = staffInfo?.producers?.toList(),
                  baseHorizontalPadding = baseHorizontalPadding
                )

                4 -> StaffCard(
                  title = tabs[pageIndex],
                  list = staffInfo?.writers?.toList(),
                  baseHorizontalPadding = baseHorizontalPadding
                )

                5 -> StaffCard(
                  title = tabs[pageIndex],
                  list = staffInfo?.animators?.toList(),
                  baseHorizontalPadding = baseHorizontalPadding
                )
              }
            }
          }
        }
      }
    }
  }
}


@Composable
private fun HeaderRow(media: Detail<Animation>, baseHorizontalPadding: Dp, love: Boolean = false) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(200.dp),
    horizontalArrangement = Arrangement.spacedBy(baseHorizontalPadding)
  ) {
    val animation = media.media
    AsyncImage(
      model = animation.coverUrls.firstOrNull(),
      contentDescription = animation.titleCn ?: animation.title,
      placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current),
      modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(2.5f / 3f)
        .clip(MaterialTheme.shapes.medium)
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .fillMaxHeight(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      animation.titleCn?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      animation.title?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.titleMedium,
          color = Color.Gray,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      animation.genre?.let {
        Text(
          text = it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray
        )
      }
      animation.totalEpisode?.let {
        Text(
          text = "共 $it 集", style = MaterialTheme.typography.bodyMedium, color = Color.Gray
        )
      }
      if (!animation.rating.isNullOrBlank()) {
        Text(
          text = "${animation.rating} 分 / ${animation.ratingCount} 人评分",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary
        )
      }
      Spacer(Modifier.weight(1f))
    }
  }
}


@Composable
private fun ExpandableDescription(
  description: String, baseHorizontalPadding: Dp
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(baseHorizontalPadding)
    ) {
      Text(
        text = "剧集简介",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}


@Composable
private fun StaffCard(
  title: String, list: List<Staff.Person>?, baseHorizontalPadding: Dp
) {
  if (list.isNullOrEmpty()) {
    EmptyCompose(text = "暂无$title" + "信息")
  } else {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(baseHorizontalPadding)
      ) {
        Text(
          text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
        )
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          itemsIndexed(list) { _, item ->
            Row(
              Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(baseHorizontalPadding)
            ) {
              Column(Modifier.width(80.dp)) {
                AsyncImage(
                  model = item.imageUrl[0],
                  contentScale = ContentScale.Crop,
                  contentDescription = item.nameCn,
                  placeholder = GifLoader.gifPlaceholder(
                    R.drawable.loading, LocalContext.current
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2.5f / 3f)
                    .clip(MaterialTheme.shapes.medium)
                )
              }
              Column(Modifier.weight(1f)) {
                Text(
                  text = item.nameCn,
                  style = MaterialTheme.typography.labelMedium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = item.role,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.Gray,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }
      }
    }
  }
}
