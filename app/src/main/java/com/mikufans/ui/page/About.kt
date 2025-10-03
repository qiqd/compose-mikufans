package com.mikufans.ui.page


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(navController: NavController?) {
  val context = LocalContext.current

  // 库名 + 协议链接
  val libs = listOf(
    "AndroidX Core / Apache-2.0" to "https://github.com/androidx/androidx/blob/androidx-main/LICENSE",
    "Compose BOM / Apache-2.0" to "https://github.com/androidx/androidx/blob/androidx-main/LICENSE",
    "Media3 ExoPlayer / Apache-2.0" to "https://github.com/androidx/media/blob/main/LICENSE",
    "Coil / Apache-2.0" to "https://github.com/coil-kt/coil/blob/main/LICENSE",
    "OkHttp / Apache-2.0" to "https://github.com/square/okhttp/blob/master/LICENSE.txt",
    "Jsoup / MIT" to "https://github.com/jhy/jsoup/blob/master/LICENSE",
    "FastJSON / Apache-2.0" to "https://github.com/alibaba/fastjson2/blob/main/LICENSE",
    "Lombok / MIT" to "https://github.com/projectlombok/lombok/blob/master/LICENSE",
  )
  val apis = listOf(
    "Bangumi 番组计划（api.bgm.tv）" to "https://github.com/bangumi/api/blob/master/README.md",
    "Giligili 源（仅供测试）" to "https://github.com/xxx/giligili",   // 换成真实 ToS 或仓库
  )
  Scaffold(
    topBar = {
      TopAppBar(title = { Text("关于") }, navigationIcon = {
        IconButton(onClick = { navController?.popBackStack() }) {
          Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
        }
      })
    }) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        Spacer(Modifier.height(24.dp))
        // 应用图标占位，可换成真实 logo
        Card(
          modifier = Modifier.size(80.dp), shape = MaterialTheme.shapes.large
        ) {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("M", style = MaterialTheme.typography.headlineLarge)
          }
        }
        Spacer(Modifier.height(12.dp))
        Text(
          text = "Mikufans",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Version 1.0.0",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Spacer(Modifier.height(12.dp))
      }

      item {
        Spacer(Modifier.height(16.dp))
        Text(
          text = "第三方 API 声明",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 8.dp)
        )
      }

      val apis = listOf(
        "Bangumi 番组计划（api.bgm.tv）" to "https://github.com/bangumi/api/blob/master/README.md",
        "Giligili 源（仅供测试）" to "https://github.com/xxx/giligili",   // 换成真实 ToS 或仓库
      )

      items(apis.size) { i ->
        val (name, url) = apis[i]
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              cm.setPrimaryClip(ClipData.newPlainText("api-tos", url))
              Toast.makeText(context, "已复制服务条款链接", Toast.LENGTH_SHORT).show()
            }) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = name, style = MaterialTheme.typography.bodyLarge
            )
            Text(
              text = "查看条款",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }/* ===== 第三方 API 声明结束 ===== */

      /* ===== 开源库列表 ===== */
      item {
        Spacer(Modifier.height(16.dp))
        Text(
          text = "开源组件",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 8.dp)
        )
      }

      items(libs.size) { i ->
        val (name, url) = libs[i]
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              cm.setPrimaryClip(ClipData.newPlainText("license", url))
              Toast.makeText(context, "已复制协议链接", Toast.LENGTH_SHORT).show()
            }) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = name, style = MaterialTheme.typography.bodyLarge
            )
            Text(
              text = "查看协议",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      /* ===== 开源库列表结束 ===== */
      item {
        Spacer(Modifier.height(24.dp))
        Text(
          text = "本软件仅供学习交流，请勿用于商业用途。",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(24.dp))
      }
    }
  }
}
