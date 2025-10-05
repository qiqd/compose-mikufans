package com.mikufans.ui.page

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun FullSearch(keyword: String, navController: NavController) {
  Text("FullSearch: $keyword")
}
