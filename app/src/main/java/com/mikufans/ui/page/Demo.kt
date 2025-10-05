package com.mikufans.ui.page

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DemoCompose() {
  val current = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  Scaffold(
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    }
  ) { paddingValues ->
    Button(
      modifier = Modifier
        .padding(paddingValues)
        .fillMaxWidth(),
      onClick = {
//        Toast.setText(current, "点击了图标按钮", Toast.LENGTH_SHORT).show()
        val makeText = Toast.makeText(current, "点击了图标按钮", Toast.LENGTH_SHORT)

      }
    ) {
      Text("图标按钮")
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DemoPreview() {
  DemoCompose()
}
