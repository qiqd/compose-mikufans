package com.mikufans.ui.page

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.api.AnimationService
import com.mikufans.api.ComicService
import com.mikufans.api.NovelService
import com.mikufans.ui.component.LoadingCompose
import com.mikufans.ui.component.MediaCard
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.GifLoader
import com.mikufans.util.GlobalSharedValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import org.anime.api.AnimationApi
import org.anime.api.ComicApi
import org.anime.api.NovelApi
import org.anime.entity.animation.Animation
import org.anime.entity.base.Media
import org.anime.entity.comic.Comic
import org.anime.entity.meta.SourceWithDelay
import org.anime.entity.novel.Novel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexPage(
  navController: NavController,
  activity: ComponentActivity,
  baseHorizontalPadding: Dp,
) {
  var keyword by rememberSaveable { mutableStateOf("") }
  var animationResult by rememberSaveable { mutableStateOf<List<Animation>>(emptyList()) }
  var comicResult by rememberSaveable { mutableStateOf<List<Comic>>(emptyList()) }
  var novelResult by rememberSaveable { mutableStateOf<List<Novel>>(emptyList()) }
  var loadingAnimation by rememberSaveable { mutableStateOf(false) }
  var loadingComic by rememberSaveable { mutableStateOf(false) }
  var loadingNovel by rememberSaveable { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val coroutineScope = rememberCoroutineScope()
  var isRefreshing by rememberSaveable { mutableStateOf(false) }
  val pullToRefreshState = rememberPullToRefreshState()
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val tabs = arrayOf("番剧", "漫画", "轻小说")
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
  var lastRefreshTime by rememberSaveable { mutableLongStateOf(0L) }
  val refreshCooldown = 5000L
  val animationApi = AnimationApi.SOURCES_WITH_DELAY
  var isInit by rememberSaveable { mutableStateOf(false) }
  var msg by remember { mutableStateOf("") }
  var showRefreshIndicator by rememberSaveable { mutableStateOf(false) }
  val fetchAnimation: suspend (k: String) -> Unit = { k ->
    loadingAnimation = true
    AnimationService.fetchSearchSync(k) {
      msg = it.message.toString()
      Log.e("IndexPage-Search", "动画搜索失败", it)
    }.takeIf { it.isNotEmpty() }?.let { animationResult = it.filterIsInstance<Animation>() }
    loadingAnimation = false
  }
  val fetchComic: suspend (k: String) -> Unit = { k ->
    loadingComic = true
    ComicService.fetchSearchSync(k) {
      msg = it.message.toString()
      Log.e("IndexPage-Search", "漫画搜索失败", it)
    }.takeIf {
      it.isNotEmpty()
    }?.let {
      comicResult = it.filterIsInstance<Comic>()
    }
    loadingComic = false
  }
  val fetchNovel: suspend (k: String) -> Unit = { k ->
    loadingNovel = true
    NovelService.fetchSearchSync(k) {
      msg = "轻小说搜索失败"
      Log.e("IndexPage-Search", "轻小说搜索失败", it)
    }.takeIf { it.isNotEmpty() }?.let { novelResult = it.filterIsInstance<Novel>() }
    loadingNovel = false
  }
  val fetchSearch: (keyword: String) -> Unit = { k ->
    if (!isInit) {
      coroutineScope.launch { fetchAnimation(k) }
      coroutineScope.launch { fetchComic(k) }
      coroutineScope.launch { fetchNovel(k) }
    } else {
      msg = "初始化资源中..."
    }

  }
  val refreshSource: () -> Unit = {
    coroutineScope.launch(Dispatchers.IO) {
      val currentTime = System.currentTimeMillis()
      if (currentTime - lastRefreshTime < refreshCooldown) {
        withContext(Dispatchers.Main) {
          Toast.makeText(
            navController.context, "操作太频繁，请稍后再试", Toast.LENGTH_SHORT
          ).show()
        }
        isRefreshing = false
        return@launch
      }
      try {
        msg = "刷新资源中"
        isRefreshing = true
        AnimationApi.initialization()
        ComicApi.initialization()
        NovelApi.initialization()
        lastRefreshTime = currentTime
        delay(2000L)
      } catch (e: Exception) {
        Log.e("IndexPage-Refresh", "刷新失败", e)
      } finally {
        isRefreshing = false
      }
    }
  }
  LaunchedEffect(msg) {
    if (msg.isNotEmpty()) {
      Toast.makeText(navController.context, msg, Toast.LENGTH_SHORT).show()
    }
  }
  BackHandler { activity.moveTaskToBack(true) }
  LaunchedEffect(Unit) {
    if (AnimationApi.SOURCES_WITH_DELAY.isEmpty()) {
      coroutineScope.launch(Dispatchers.IO) {
        try {
          isInit = true
          GlobalSharedValue.isInit = true
          msg = "初始化资源中"
          AnimationApi.initialization()
          ComicApi.initialization()
          NovelApi.initialization()
          msg = "初始化资源完成"
          Log.w("IndexPage-Init", AnimationApi.SOURCES_WITH_DELAY.size.toString())
        } catch (e: TimeoutCancellationException) {
          Log.e("IndexPage-Init", "初始化资源超时", e)
          msg = "初始化资源失败"
        } catch (e: IOException) {
          e.printStackTrace()
          msg = "网络无法使用"
        } catch (e: Exception) {
          msg = e.message ?: "未知错误"
        } finally {
          isInit = false
          GlobalSharedValue.isInit = false
        }
      }
    }
  }
  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = {
      TopAppBar(title = { Text("首页") }, actions = {
        IconButton(
          enabled = isInit.not() && isRefreshing.not(), onClick = {
            Log.i("IndexPage-TopBar", "点击切换资源")
            coroutineScope.launch { sheetState.show() }
          }) {
          Icon(
            imageVector = Icons.Default.Source, contentDescription = "切换资源"
          )
        }
      })
    },
  ) { innerPadding ->
    Column(
      modifier = Modifier.padding(innerPadding)
    ) {
      // 搜索框
      TextField(
        modifier = Modifier
          .fillMaxWidth()
          .clip(shape = ShapeDefaults.Small),
        value = keyword,
        onValueChange = { keyword = it },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        placeholder = { Text("请输入关键字") },
        maxLines = 1,
        leadingIcon = {
          Icon(imageVector = Icons.Default.Search, contentDescription = "搜索")
        },
        keyboardActions = KeyboardActions(
          onSearch = {
            if (animationApi.isEmpty()) {
              Toast.makeText(navController.context, "初始化资源中，请稍后...", Toast.LENGTH_SHORT)
                .show()
              return@KeyboardActions
            }
            // 执行搜索
            fetchSearch(keyword)
            Log.i("IndexPage-Search", keyword)
            focusManager.clearFocus()
          }),
      )
      //下拉刷新
      PullToRefreshBox(
        state = pullToRefreshState,
        isRefreshing = showRefreshIndicator && !isInit,
        indicator = {
          Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = showRefreshIndicator && !isInit,
            color = MaterialTheme.colorScheme.primary,
            state = pullToRefreshState
          )
        },
        onRefresh = {
          if (isInit) {
            return@PullToRefreshBox
          }
          showRefreshIndicator = true
          coroutineScope.launch {
            fetchAnimation(keyword)
            fetchComic(keyword)
            fetchNovel(keyword)
            showRefreshIndicator = false
          }
        }) {
        Column(Modifier.fillMaxSize()) {
          PrimaryTabRow(selectedTabIndex = tabIndex.value) {
            tabs.forEachIndexed { index, title ->
              Tab(
                text = { Text(title) },
                selected = tabIndex.value == index,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                  Log.i("IndexPage-Tab", "点击切换到$index")
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                  }
                })
            }
          }
          HorizontalPager(
            state = pagerState, pageSpacing = 10.dp, modifier = Modifier.fillMaxSize()
          ) {
            when (it) {
              0 -> {
                if (loadingAnimation) {
                  LoadingCompose()
                } else {
                  AnimationPage(animationResult, navController, clickable = !isInit)
                }
              }

              1 -> {
                if (loadingComic) {
                  LoadingCompose()
                } else {
                  ComicPage(comicResult, navController, clickable = !isInit)
                }
              }

              2 -> {
                if (loadingNovel) {
                  LoadingCompose()
                } else {
                  NovelPage(novelResult, navController, clickable = !isInit)
                }
              }
            }
          }
        }
      }
    }

    // 底部弹窗
    if (sheetState.isVisible) {
      ModalBottomSheet(
        onDismissRequest = {
          coroutineScope.launch { sheetState.hide() }
        }, sheetState = sheetState
      ) {
        val tabs = arrayOf("动画", "漫画", "小说")
        val pagerState = rememberPagerState(pageCount = { tabs.size })
        Row(
          Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("资源列表")
          IconButton(onClick = {
            refreshSource()
            coroutineScope.launch { sheetState.hide() }
          }) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "刷新资源")
          }
        }
        Column(Modifier.fillMaxWidth()) {
          PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
              Tab(
                text = { Text(title) },
                selected = pagerState.currentPage == index,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                  Log.i("IndexPage-Tab", "点击切换到$index")
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                  }
                })
            }
          }
          HorizontalPager(state = pagerState, userScrollEnabled = false) {
            when (it) {
              0 -> SourcePage(
                source = AnimationApi.SOURCES_WITH_DELAY.toList(),
                sheetState = sheetState
              )

              1 -> SourcePage(
                source = ComicApi.SOURCES_WITH_DELAY.toList(),
                sheetState = sheetState
              )

              2 -> SourcePage(
                source = NovelApi.SOURCES_WITH_DELAY.toList(),
                sheetState = sheetState
              )
            }
          }
        }

      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcePage(source: List<SourceWithDelay>, sheetState: SheetState) {
  val coroutineScope = rememberCoroutineScope()
  LazyColumn {
    itemsIndexed(
      items = source,
    ) { index, item ->
      ListItem(modifier = Modifier.clickable {
        coroutineScope.launch {
          AnimationApi.moveToTop(index)
          sheetState.hide()
        }
      }, tonalElevation = 2.dp, leadingContent = {
        AsyncImage(
          modifier = Modifier
            .height(150.dp)
            .aspectRatio(2.5f / 3f)
            .clip(MaterialTheme.shapes.medium),
          contentScale = ContentScale.Fit,
          model = item.htmlParser.logoUrl,
          contentDescription = item.htmlParser.name,
          placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current),
        )
      }, headlineContent = {
        Text(item.htmlParser.name)
      }, supportingContent = {
        Text("延迟: ${item.delay}ms")
      })
      HorizontalDivider()
    }
  }
}

@Composable
fun AnimationPage(
  animations: List<Media>, navController: NavController, clickable: Boolean = false
) {
  LazyColumn(
    contentPadding = PaddingValues(vertical = 5.dp),
    verticalArrangement = Arrangement.spacedBy(5.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    animations.let {
      items(it.size) { index ->
        MediaCard(
          id = it[index].id,
          status = it[index].status,
          coverUrl = it[index].coverUrls[0],
          title = it[index].title,
          titleCn = it[index].titleCn,
          rating = it[index].rating,
          ratingCount = it[index].ratingCount,
          genre = it[index].genre,
          airDate = it[index].releaseDate,
          onTap = { id ->
            if (clickable) {
              Navigation.navigateToDetail(
                id = id, title = it[index].titleCn, subId = "", navController = navController
              )
            }
          })
      }
    }
  }
}

@Composable
fun ComicPage(comics: List<Comic>, navController: NavController, clickable: Boolean = false) {
  LazyColumn(
    contentPadding = PaddingValues(vertical = 5.dp),
    verticalArrangement = Arrangement.spacedBy(5.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    comics.let {
      items(it.size) { index ->
        MediaCard(
          id = it[index].id,
          status = it[index].status,
          coverUrl = it[index].coverUrls[0],
          title = it[index].title,
          titleCn = it[index].titleCn,
          rating = it[index].rating,
          ratingCount = it[index].ratingCount,
          genre = it[index].genre,
          airDate = it[index].releaseDate,
          author = it[index].author,
          mediaType = Navigation.TYPE_COMIC,
          onTap = { id ->
            if (clickable) {
              Navigation.navigateToDetail(
                type = Navigation.TYPE_COMIC,
                id = id, title = it[index].titleCn, subId = "", navController = navController
              )
            }
          })
      }
    }
  }
}

@Composable
fun NovelPage(novel: List<Any>, navController: NavController, clickable: Boolean = false) {

}
