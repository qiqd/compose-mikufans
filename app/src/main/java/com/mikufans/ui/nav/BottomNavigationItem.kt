package com.mikufans.ui.nav


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavigationItem(val route: String, val title: String, val icon: ImageVector) {
    object Index : BottomNavigationItem("index", "首页", Icons.Default.Home)
    object Weekly : BottomNavigationItem("weekly", "周更表", Icons.Default.DateRange)
    object Subscribe : BottomNavigationItem("subscribe", "订阅", Icons.Default.Favorite)
    object Me : BottomNavigationItem("me", "我的", Icons.Default.AccountCircle)
}