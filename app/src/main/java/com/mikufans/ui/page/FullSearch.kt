package com.mikufans.ui.page

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.mikufans.xmd.util.SourceUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSearch(keyword: String, navController: NavController) {
  val sources = rememberSaveable { mutableListOf(SourceUtil.getSourceWithDelay()) }
  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = { navController.popBackStack() }) {
            Icons.Default.ArrowBackIosNew
          }
        },
        title = { Text(text = "全站搜索") }
      )
    },
    content = { innerPadding ->

      Column(modifier = Modifier.padding(innerPadding)) {
        Button(onClick = {
          Log.i("FullSearch", sources.toString())
        }) { }


      }
    }
  )

}
