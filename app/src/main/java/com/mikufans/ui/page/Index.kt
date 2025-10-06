package com.mikufans.ui.page

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikufans.ui.component.AnimeCard
import com.mikufans.ui.nav.Navigation
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.teto.service.impl.RedDrillBit
import com.mikufans.xmd.util.SourceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Index(navController: NavController) {
  var keyword by rememberSaveable { mutableStateOf("") }
  var searchResult by rememberSaveable {
    mutableStateOf<List<Anime>>(emptyList())
  }
  var isLoading by rememberSaveable { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val coroutineScope = rememberCoroutineScope()
  val lazyGridState = rememberLazyStaggeredGridState()
  val sources = rememberSaveable { SourceUtil.getSourceWithDelay() }
  Column(modifier = Modifier.padding(horizontal = 8.dp)) {
    TextField(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape = ShapeDefaults.Small),
      value = keyword,
      onValueChange = { keyword = it },
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      placeholder = { Text("请输入关键字") },
      maxLines = 1,
      keyboardActions = KeyboardActions(
        onSearch = {
          Log.i("Index-Search", keyword)
          coroutineScope.launch(Dispatchers.IO) {
            try {
              isLoading = true
//              val search = sources[0].service.getSearchResult(keyword, 1, 20)
              val anime = RedDrillBit().fetchSearchResult(keyword, 1, 10)
              val result = anime ?: emptyList()
              withContext(Dispatchers.Main) {
                searchResult = result
              }
            } catch (e: Exception) {
              // 处理错误
              withContext(Dispatchers.Main) {
                searchResult = emptyList()
              }
              Log.e("Index-Search", "搜索失败", e)
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

    LazyVerticalStaggeredGrid(
      columns = StaggeredGridCells.Fixed(3),
      state = lazyGridState,
      contentPadding = PaddingValues(vertical = 5.dp),
      verticalItemSpacing = 5.dp,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(searchResult) {
        AnimeCard(anime = it) { animeId, animeName ->
          Navigation.navigateToAnimeDetail(
            navController = navController,
            animeSubId = animeId,
            animeName = animeName,
          )
        }
      }
    }
  }
}
