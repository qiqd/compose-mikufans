package com.mikufans.ui.component

import androidx.compose.runtime.Composable

@Composable
fun LoadingOrShowMsg(
  errMsg: String?,
) {
  if (errMsg.isNullOrEmpty()) {
    LoadingCompose()
  } else {
    EmptyCompose(errMsg)
  }
}
