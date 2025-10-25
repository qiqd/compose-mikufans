package com.mikufans.ui.page


import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import com.mikufans.ui.nav.Navigation
import com.mikufans.util.LocalStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinePage(
  navController: NavController,
  activity: ComponentActivity,
  baseHorizontalPadding: Dp,
) {
  val userEmail = LocalStorage.get(navController.context, "email", String::class.java) ?: ""
  BackHandler { activity.moveTaskToBack(true) }
  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = { TopAppBar(title = { Text("我的") }) }) { innerPadding ->
    LazyColumn(
      contentPadding = innerPadding
    ) {
      item {
        ListItem(
          modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable {
              Navigation.navigateToLogin(navController, userEmail)
            },
          headlineContent = { Text(userEmail.ifEmpty { "未登录" }) },
          leadingContent = {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "person"
            )
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null
            )
          }
        )
      }
      item {
        ListItem(
          modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable {
              Navigation.navigateToHistory(navController)
            },
          headlineContent = { Text("历史记录") },
          leadingContent = {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "history"
            )
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null
            )
          }
        )
      }
      item {
        ListItem(
          modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable {
              Navigation.navigateToAbout(navController)
            },
          headlineContent = { Text("关于") },
          leadingContent = {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "about"
            )
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null
            )
          }
        )
      }
      item {
        ListItem(
          modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable {
              Navigation.navigateToSetting(navController)
            },
          headlineContent = { Text("设置") },
          leadingContent = {
            Icon(
              Icons.Default.Settings,
              contentDescription = null
            )
          },
          trailingContent = {
            Icon(
              Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null
            )
          }
        )
      }
    }
  }
}

