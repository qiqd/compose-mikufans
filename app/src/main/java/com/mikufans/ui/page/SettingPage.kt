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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.mikufans.util.LocalStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingPage(navController: NavController, baseHorizontalPadding: Dp) {
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
            LocalStorage.clearCache(
              navController.context
            )
            Toast.makeText(navController.context, "清除缓存成功", Toast.LENGTH_SHORT).show()
          }) { Text("清除缓存") }
      }
    }
  }
}