package com.mikufans.ui.page

import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.api.AnimationService
import com.mikufans.api.ComicService
import com.mikufans.api.MetaService
import com.mikufans.entity.History
import com.mikufans.ui.component.EmptyCompose
import com.mikufans.ui.component.LoadingOrShowMsg
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.GifLoader
import com.mikufans.util.GlobalSharedValue
import com.mikufans.util.LocalStorage
import com.mikufans.util.StringMatchUtil
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import org.anime.entity.animation.Animation
import org.anime.entity.animation.Staff
import org.anime.entity.base.Detail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPage(
  id: String,
  subId: String,
  title: String,
  type: String,
  navController: NavController,
  baseHorizontalPadding: Dp
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var staffInfo by rememberSaveable { mutableStateOf<Staff?>(null) }
  var subId by rememberSaveable { mutableStateOf(subId) }
  var id by rememberSaveable { mutableStateOf(id) }
  var love by rememberSaveable { mutableStateOf(false) }
  var loadingMetaData by rememberSaveable { mutableStateOf(false) }
  var localHistory by rememberSaveable { mutableStateOf<List<History>>(mutableListOf()) }
  var errMsg by rememberSaveable { mutableStateOf<String?>(null) }
  var mediaDetail by rememberSaveable { mutableStateOf<Detail?>(null) }
  var chapterIndex by rememberSaveable { mutableIntStateOf(0) }
  var historyIndex by rememberSaveable { mutableIntStateOf(-1) }
  val loadLocalHistory: () -> Unit = {
    LocalStorage.getList(context, "view:history", History::class.java)?.let { histories ->
      localHistory = histories
      historyIndex = if (id.isNotBlank()) {
        histories.indexOfFirst { it.nameCn == title }
      } else {
        histories.indexOfFirst { it.subId == subId }
      }
      if (historyIndex >= 0) {
        chapterIndex = histories[historyIndex].episodeIndex ?: 0
        love = histories[historyIndex].isLove
      }
    }
  }
  val updateHistory: () -> Unit = {
    val history = History(
      id = mediaDetail?.media?.id,
      subId = subId,
      name = mediaDetail?.media?.title,
      nameCn = mediaDetail?.media?.titleCn,
      time = System.currentTimeMillis(),
      mediaType = type,
      isLove = love,
      chapterName = mediaDetail?.sources
        ?.takeIf { it.isNotEmpty() }
        ?.getOrNull(0)
        ?.episodes
        ?.takeIf { it.isNotEmpty() }
        ?.getOrNull(chapterIndex)
        ?.title,
      cover = mediaDetail?.media?.coverUrls[0]
    ).apply {
      if (type == Navigation.TYPE_COMIC) {
        episodeIndex = chapterIndex
      }
    }
    localHistory = localHistory.toMutableList()
      .apply { add(history) }
      .filter { it.nameCn.isNullOrBlank().not() }
      .groupBy { it.nameCn }
      .map { (_, value) -> value.maxBy { it.time } }
      .sortedByDescending { it.time }
    LocalStorage.setList(context, "view:history", localHistory)
  }
  val loadMetadata: suspend () -> Unit = {
    loadingMetaData = true
    if (subId.isBlank()) {
      MetaService.fetchSearchSync(title) {
        errMsg = it.message
      }.takeIf { it.isNotEmpty() }?.let {
        val media = it[0]
        if (media is Animation) {
          subId = media.subId
        }
      }
    }
    subId.takeIf { it.isNotBlank() }?.let {
      MetaService.fetchDetailSync(subId) {
        errMsg = it.message
      }?.let {
        mediaDetail?.media = it.media
        GlobalSharedValue.mediaDetail?.media = it.media
      }
      MetaService.fetchStaffSync(subId) {
        errMsg = it.message
      }?.let { staffInfo = it }
      localHistory.indexOfFirst { it.subId == subId }.takeIf { it >= 0 }?.let {
        love = true
      }
    }
    loadingMetaData = false

  }
  val fetchDetail: suspend () -> Unit = {
    when (type) {
      Navigation.TYPE_ANIMATION -> {
//        loadMetadata()
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
        }?.let { item ->
          mediaDetail = item
          GlobalSharedValue.mediaDetail = item
        }
      }

      Navigation.TYPE_COMIC -> {
        ComicService.fetchDetailSync(id) {
          errMsg = it.message.toString()
        }?.let { mediaDetail = it; GlobalSharedValue.mediaDetail = it }
      }

      else -> {
        //todo novel case
      }
    }
  }

  LaunchedEffect(Unit) {
    if (mediaDetail != null) return@LaunchedEffect
    scope.launch {
      fetchDetail()
//      if (type == Navigation.TYPE_ANIMATION) loadMetadata()
      loadLocalHistory()
    }

  }
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    loadLocalHistory()
  }
  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = {
      TopAppBar(title = { Text("详情") }, navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
          Icon(
            imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = "back"
          )
        }
      })
    },
  ) { innerPadding ->
    Column(
      verticalArrangement = Arrangement.spacedBy(baseHorizontalPadding),
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
    ) {
      if (mediaDetail != null) {
        HeaderRow(
          detail = mediaDetail!!,
          mediaType = type,
          baseHorizontalPadding = baseHorizontalPadding,
          love = love,
          onPlayTab = {
            Navigation.navigateToPlayer(
              id = mediaDetail!!.media.id ?: "",
              subId = mediaDetail!!.media.id ?: "",
              title = mediaDetail!!.media.titleCn ?: mediaDetail!!.media.title ?: "暂无标题",
              navController = navController
            )
          },
          onLoveTab = {
            love = !love
            updateHistory()
          }
        )
      }
      when (type) {
        Navigation.TYPE_ANIMATION -> {
          AnimationPart(mediaDetail, staffInfo, baseHorizontalPadding, errMsg)
        }

        Navigation.TYPE_COMIC -> {
          ChapterList(mediaDetail, chapterIndex, errMsg, baseHorizontalPadding) {
            Navigation.navigateToComic(it.toString(), navController)
          }
        }

        else -> {
          LoadingOrShowMsg(errMsg)
        }
      }
    }
  }
}

/**
 * 详情页头部
 */
@Composable
private fun HeaderRow(
  detail: Detail,
  baseHorizontalPadding: Dp,
  love: Boolean = false,
  mediaType: String,
  onPlayTab: () -> Unit,
  onLoveTab: () -> Unit
) {
  Row(
    modifier = Modifier
      .height(200.dp)
      .fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(baseHorizontalPadding)
  ) {
    val media = detail.media
    Column(
      Modifier
        .fillMaxHeight()
    ) {
      AsyncImage(
        model = media.coverUrls.firstOrNull(),
        contentDescription = media.titleCn ?: media.title,
        placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current),
        modifier = Modifier
          .fillMaxHeight()
          .aspectRatio(2.5f / 3f)
          .clip(MaterialTheme.shapes.medium)
      )
    }
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      media.titleCn?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      media.title?.let {
        Text(
          text = it,
          style = MaterialTheme.typography.titleMedium,
          color = Color.Gray,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }
      media.genre?.let {
        Text(
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          text = it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray
        )
      }
      if (media is Animation) {
        media.totalEpisode.takeIf {
          !(it.isNullOrBlank())
        }?.let {
          Text(
            text = "共 $it 集", style = MaterialTheme.typography.bodyMedium, color = Color.Gray
          )
        }
      }
      if (!media.rating.isNullOrBlank()) {
        Text(
          text = "${media.rating} 分 / ${media.ratingCount} 人评分",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary
        )
      }
      media.status?.let {
        Text(
          text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray
        )
      }
      Spacer(
        Modifier
          .weight(1f)
          .fillMaxSize()
      )
      Row(
        Modifier
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        if (mediaType == Navigation.TYPE_ANIMATION) {
          OutlinedButton(onClick = { onPlayTab() }, Modifier.weight(1f)) {
            Icon(
              imageVector = Icons.Outlined.PlayArrow,
              contentDescription = "play"
            )
          }
        }
        if (love) {
          Button(onClick = { onLoveTab() }) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = "love"
            )
          }
        } else {
          OutlinedButton(onClick = { onLoveTab() }) {
            Icon(
              imageVector = Icons.Default.FavoriteBorder,
              contentDescription = "unlove"
            )
          }
        }
      }
    }
  }
}

/**
 * 详情页剧集简介
 */
@Composable
private fun ExpandableDescription(
  description: String, baseHorizontalPadding: Dp
) {
  Column(
    Modifier.fillMaxSize()
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Column(Modifier.padding(baseHorizontalPadding)) {
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
}


@Composable
private fun StaffCard(
  title: String, list: List<Staff.Person>?, baseHorizontalPadding: Dp
) {
  if (list.isNullOrEmpty()) {
    EmptyCompose(text = "暂无$title" + "信息")
  } else {
    Column(Modifier.fillMaxSize()) {
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
}

@Composable
private fun AnimationPart(
  metadata: Detail?,
  staffInfo: Staff?,
  baseHorizontalPadding: Dp,
  errMsg: String?
) {
  val tabs = arrayOf("简介", "导演", "演员", "制片", "编剧", "动画")
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val scope = rememberCoroutineScope()
  metadata?.let {
    staffInfo?.let {
      Column(
        Modifier
          .fillMaxSize()
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
              description = metadata.media?.description ?: "暂无简介",
              baseHorizontalPadding = baseHorizontalPadding
            )

            1 -> StaffCard(
              title = tabs[pageIndex],
              list = staffInfo.directors?.toList(),
              baseHorizontalPadding = baseHorizontalPadding
            )

            2 -> StaffCard(
              title = tabs[pageIndex],
              list = staffInfo.actors?.toList(),
              baseHorizontalPadding = baseHorizontalPadding
            )

            3 -> StaffCard(
              title = tabs[pageIndex],
              list = staffInfo.producers?.toList(),
              baseHorizontalPadding = baseHorizontalPadding
            )

            4 -> StaffCard(
              title = tabs[pageIndex],
              list = staffInfo.writers?.toList(),
              baseHorizontalPadding = baseHorizontalPadding
            )

            5 -> StaffCard(
              title = tabs[pageIndex],
              list = staffInfo.animators?.toList(),
              baseHorizontalPadding = baseHorizontalPadding
            )
          }
        }
      }
    } ?: run {
      Box(
        Modifier
          .fillMaxSize()
          .padding(top = baseHorizontalPadding), Alignment.Center
      ) {
//        CircularProgressIndicator()

        ExpandableDescription(
          description = metadata.media?.description ?: "暂无简介",
          baseHorizontalPadding = baseHorizontalPadding
        )
      }
    }

  } ?: run {
    LoadingOrShowMsg(errMsg)
  }
}

@Composable
private fun ChapterList(
  mediaDetail: Detail?,
  chapterIndex: Int,
  errMsg: String?,
  baseHorizontalPadding: Dp,
  onEpisodeClick: (Int) -> Unit
) {
  var reverse by rememberSaveable { mutableStateOf(false) }
  var comicIndex by rememberSaveable { mutableIntStateOf(chapterIndex) }
  val listState = rememberLazyListState(initialFirstVisibleItemIndex = chapterIndex)
  Log.e("DetailPage", "ChapterList: $chapterIndex")
  val coroutineScope = rememberCoroutineScope()
  LaunchedEffect(mediaDetail, reverse) {
    mediaDetail ?: return@LaunchedEffect
    val total = mediaDetail.sources[0].episodes.size
    val target = comicIndex.coerceIn(0, total - 1)
    awaitFrame()
    listState.scrollToItem(if (reverse) total - 1 - target else target)
  }
  LaunchedEffect(chapterIndex) {
    if (chapterIndex == 0) {
      return@LaunchedEffect
    }
    awaitFrame()
    comicIndex = chapterIndex
    listState.animateScrollToItem(chapterIndex)
  }
  mediaDetail?.let {
    Row(
      Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = "章节列表", style = MaterialTheme.typography.titleMedium)
      Spacer(Modifier.weight(1f))
      TextButton(onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } }) {
        Text(text = "回到顶部")
      }
      TextButton(onClick = { reverse = !reverse }) {
        Text(text = if (reverse) "正序" else "倒序")
      }
    }
    LazyColumn(
      state = listState,
      reverseLayout = reverse,
      contentPadding = PaddingValues(vertical = baseHorizontalPadding),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      itemsIndexed(it.sources[0].episodes) { index, episode ->
        if (comicIndex == index) {
          Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onEpisodeClick(index) }) {
            Text(
              episode.title,
              textAlign = TextAlign.Start
            )
          }
        } else {
          OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
              comicIndex = index; onEpisodeClick(index)
            }) { Text(episode.title, textAlign = TextAlign.Start) }
        }
      }
    }
  } ?: run { LoadingOrShowMsg(errMsg) }
}

