package com.mikufans.ui.page

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.mikufans.xmd.access.AAFunAccessPoint
import com.mikufans.xmd.teto.entity.SubjectSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Index(navController: NavController) {
  var keyword by rememberSaveable { mutableStateOf("") }
  var searchResult by rememberSaveable {
    mutableStateOf<List<SubjectSearch.Subject>>(emptyList())
  }
  var isLoading by rememberSaveable { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val coroutineScope = rememberCoroutineScope()

  Column(modifier = Modifier.padding(8.dp)) {
    TextField(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape = ShapeDefaults.Small),
      value = keyword,
      onValueChange = { keyword = it },
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      placeholder = { Text("请输入关键字") },
      keyboardActions = KeyboardActions(
        onSearch = {
          Log.i("Index-Search", keyword)
          coroutineScope.launch(Dispatchers.IO) {
            try {
              isLoading = true
              val search = AAFunAccessPoint().search(keyword, 1, 20)
              val result = search.data ?: emptyList()
              withContext(Dispatchers.Main) {
                searchResult = result
//                Log.i("search", search.toString())
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

    LazyVerticalGrid(
      modifier = Modifier.padding(top = 5.dp),
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(searchResult) {
        AnimeCard(anime = it) { animeId, animeName ->
          navController.navigate("animeDetail/$animeId/$animeName") {
//            // 隐藏底部导航栏
//            popUpTo(navController.graph.findStartDestination().id) {
//              saveState = true
//            }
            launchSingleTop = true
            restoreState = true
          }
        }
      }
    }
  }
}
