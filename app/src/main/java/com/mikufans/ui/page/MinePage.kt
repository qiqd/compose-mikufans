package com.mikufans.ui.page


import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.mikufans.ui.nav.Navigation

@Composable
fun MinePage(navController: NavController, activity: ComponentActivity) {
  BackHandler { activity.moveTaskToBack(true) }
  LazyColumn {
    item {
      ListItem(
        modifier = Modifier.clickable {
          Navigation.navigateToHistory(navController)
        },
        headlineContent = { Text("历史记录") },
        leadingContent = {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "历史记录"
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
        modifier = Modifier.clickable {
          Navigation.navigateToAbout(navController)
        },
        headlineContent = { Text("关于") },
        leadingContent = {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "关于"
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

