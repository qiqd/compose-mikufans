package com.mikufans.ui.page

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.mikufans.ui.component.AnimeCard
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.GifLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okio.IOException
import org.anime.api.AnimeApi
import org.anime.entity.Animation
import org.anime.entity.bangmi.SourceWithDelay

import org.anime.meta.impl.Bangumi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexPage(
  navController: NavController,
  activity: ComponentActivity,
  baseHorizontalPadding: Dp,
) {
  var keyword by rememberSaveable { mutableStateOf("") }
  var searchResult by rememberSaveable { mutableStateOf<List<Animation>>(emptyList()) }
  var isLoading by rememberSaveable { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val coroutineScope = rememberCoroutineScope()
  val lazyGridState = rememberLazyListState()
  var sources by rememberSaveable { mutableStateOf(emptyList<SourceWithDelay>()) }
  var isRefreshing by rememberSaveable { mutableStateOf(false) }
  val pullToRefreshState = rememberPullToRefreshState()
  var showBottomSheet by rememberSaveable { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState()
  val scope = rememberCoroutineScope()
  val meteService = Bangumi()
  var lastRefreshTime by rememberSaveable { mutableLongStateOf(0L) }
  val refreshCooldown = 5000L
  BackHandler { activity.moveTaskToBack(true) }
  LaunchedEffect(Unit) {
    if (sources.isNotEmpty()) return@LaunchedEffect
    Toast.makeText(navController.context, "初始化资源中", Toast.LENGTH_SHORT).show()
    coroutineScope.launch(Dispatchers.IO) {
      try {
        withTimeout(30000) {
          while (sources.isEmpty()) {
            sources = AnimeApi.SOURCES_WITH_DELAY.toList()
            delay(100) // 避免过于频繁的检查
          }
        }
        withContext(Dispatchers.Main) {
          Toast.makeText(
            navController.context, "初始化资源完成", Toast.LENGTH_SHORT
          ).show()
        }
      } catch (e: TimeoutCancellationException) {
        Log.e("IndexPage-Init", "初始化资源超时", e)
        withContext(Dispatchers.Main) {
          Toast.makeText(
            navController.context, "初始化资源失败", Toast.LENGTH_SHORT
          ).show()
        }
      } catch (e: IOException) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
          Toast.makeText(
            navController.context, "网络无法使用", Toast.LENGTH_SHORT
          ).show()
        }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          Toast.makeText(
            navController.context, e.message, Toast.LENGTH_SHORT
          ).show()
        }
      }
    }
  }
  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = {
      TopAppBar(title = { Text("首页") }, actions = {
        IconButton(onClick = {
          Log.i("IndexPage-TopBar", "点击切换资源")
          scope.launch { sheetState.show() }
          showBottomSheet = true
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
            if (sources.isEmpty()) {
              Toast.makeText(navController.context, "初始化资源中，请稍后...", Toast.LENGTH_SHORT)
                .show()
              return@KeyboardActions
            }
            Log.i("IndexPage-Search", keyword)
            coroutineScope.launch(Dispatchers.IO) {
              try {
                isLoading = true
//              val search = sources[0].service.getSearchResult(keyword, 1, 20)
                val anime = meteService.fetchSearchResultSync(keyword, 1, 10)
                val result = anime ?: emptyList()
                withContext(Dispatchers.Main) {
                  searchResult = result
                }
              } catch (e: Exception) {
                // 处理错误
                withContext(Dispatchers.Main) {
                  searchResult = emptyList()
                }
                Log.e("IndexPage-Search", "搜索失败", e)
                withContext(Dispatchers.Main) {
                  Toast.makeText(
                    navController.context, "搜索失败:${e.message}", Toast.LENGTH_SHORT
                  ).show()
                }
              } finally {
                withContext(Dispatchers.Main) {
                  isLoading = false
                }
              }
            }
            focusManager.clearFocus()
          }),
      )
      // 添加加载指示器
      if (isLoading) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }

      PullToRefreshBox(state = pullToRefreshState, isRefreshing = isRefreshing, indicator = {
        Indicator(
          modifier = Modifier.align(Alignment.TopCenter),
          isRefreshing = isRefreshing,
          color = MaterialTheme.colorScheme.primary,
          state = pullToRefreshState
        )
      }, onRefresh = {
        coroutineScope.launch(Dispatchers.IO) {
          val currentTime = System.currentTimeMillis()
          // 检查是否在冷却时间内
          if (currentTime - lastRefreshTime < refreshCooldown) {
            withContext(Dispatchers.Main) {
              Toast.makeText(
                navController.context,
                "操作太频繁，请稍后再试",
                Toast.LENGTH_SHORT
              ).show()
            }
            isRefreshing = false
            return@launch
          }
          try {
            isRefreshing = true
            lastRefreshTime = currentTime
            AnimeApi.initialization()
            delay(2000L)
          } catch (e: Exception) {
            Log.e("IndexPage-Refresh", "刷新失败", e)
          } finally {
            isRefreshing = false
          }
        }
      }) {
        LazyColumn(
          state = lazyGridState,
          contentPadding = PaddingValues(vertical = 5.dp),
          verticalArrangement = Arrangement.spacedBy(5.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(searchResult.size) { index ->
            AnimeCard(anime = searchResult[index]) { animeId, animeName ->
              Navigation.navigateToAnimeDetail(
                navController = navController,
                animeSubId = animeId.toString(),
                animeName = animeName,
              )
            }
          }
        }
      }
    }
    if (showBottomSheet) {
      ModalBottomSheet(
        onDismissRequest = {
          showBottomSheet = false
        },
        sheetState = sheetState
      ) {
        LazyColumn {
          itemsIndexed(
            items = AnimeApi.SOURCES_WITH_DELAY.toList(),
          ) { index, item ->
            ListItem(
              modifier = Modifier.clickable {
                showBottomSheet = false
                scope.launch {
                  AnimeApi.moveToTop(index)
                  sheetState.hide()
                }
              },
              tonalElevation = 2.dp,
              leadingContent = {
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
              },
              headlineContent = {
                Text(item.htmlParser.name)
              },
              supportingContent = {
                Text("延迟: ${item.delay}ms")
              }
            )
            HorizontalDivider()
          }
        }
      }
    }
  }
}
