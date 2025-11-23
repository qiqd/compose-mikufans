package com.mikufans.ui.page

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.mikufans.util.LocalStorage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun SettingPage(navController: NavController, baseHorizontalPadding: Dp) {

  /* 用 remember 保存缓存大小 */
  val cacheSizeText = remember { mutableStateOf("计算中…") }

  /* 进入页面时异步刷新一次 */
  LaunchedEffect(Unit) {
    val bytes = withContext(Dispatchers.IO) {
      LocalStorage.getCacheSize(navController.context)
    }
    cacheSizeText.value = LocalStorage.formatSize(bytes)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
          }
        },
        title = { Text("设置") })
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .padding(innerPadding)
        .padding(horizontal = baseHorizontalPadding)
    ) {
      Row(modifier = Modifier.fillMaxWidth()) {
        Button(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            LocalStorage.clearCache(navController.context)
            Toast.makeText(navController.context, "清除缓存成功", Toast.LENGTH_SHORT).show()

            /* 清除后重新计算并更新文案 */
            kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
              val bytes = LocalStorage.getCacheSize(navController.context)
              withContext(Dispatchers.Main) {
                cacheSizeText.value = LocalStorage.formatSize(bytes)
              }
            }
          }
        ) {
          Text("清除缓存（当前 ${cacheSizeText.value}）")
        }
      }
    }
  }
}
