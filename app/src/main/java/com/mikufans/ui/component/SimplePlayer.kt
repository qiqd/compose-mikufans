package com.mikufans.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.mikufans.ui.page.formatTime
import com.mikufans.ui.page.isLandscape
import com.mikufans.xmd.miku.entiry.PlayInfo

@Composable
        /* ====================== 播放器 UI 壳 ====================== */
fun SimplePlayer(
  exoPlayer: ExoPlayer,
  playInfo: PlayInfo,
  modifier: Modifier,
  isFullScreen: Boolean,
  onFullScreenButtonClick: () -> Unit = {},
  onCurrentPosition: (Long) -> Unit = {}
) {
//  val exoPlayer = globalPlayer ?: return/* 实时进度 & 拖动状态 */
  var progress by rememberSaveable { mutableFloatStateOf(0f) }
  var isDragging by rememberSaveable { mutableStateOf(false) }
  val total by remember(exoPlayer.duration) {
    derivedStateOf {
      exoPlayer.duration.coerceAtLeast(
        0L
      )
    }
  }/* ② 首次/历史进度：拿到总时长后一次性同步 progress */
  LaunchedEffect(total) {
    if (!isDragging && total > 0) {
      progress = exoPlayer.currentPosition.toFloat() / total
    }
  }
  var showController by rememberSaveable { mutableStateOf(false) }/* 播放中更新进度（拖动时不更新） */

  /* 切集后重置进度 */
  LaunchedEffect(playInfo.currentEpisodeUrl) {
    progress = 0f
    isDragging = false
  }
  LaunchedEffect(exoPlayer) {
    while (true) {
      kotlinx.coroutines.delay(200)
      val dur = total
      if (!isDragging && dur > 0) {
        progress = exoPlayer.currentPosition.toFloat() / dur
        onCurrentPosition(exoPlayer.currentPosition)
      }
    }
  }

  /* ① 同步系统栏隐藏/显示 */
  val view = LocalView.current
  DisposableEffect(showController) {
    if (!isFullScreen) return@DisposableEffect onDispose { }
    val window = (view.context as android.app.Activity).window
    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
    if (showController) {
      // 退出沉浸：显示状态栏+导航栏
      insetsController.show(WindowInsetsCompat.Type.systemBars())
      insetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    } else {
      // 沉浸：隐藏状态栏+导航栏
      insetsController.hide(WindowInsetsCompat.Type.systemBars())
      insetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    onDispose { /* 页面销毁时无需额外恢复 */ }
  }

  Box(
    modifier = modifier
      .clickable { showController = !showController }
      .padding(if (isLandscape()) PaddingValues(vertical = 30.dp) else PaddingValues(0.dp))) {
    /* 原来的 PlayerView */
    AndroidView(
      factory = {
        PlayerView(it).apply {
          player = exoPlayer
          useController = false
        }
      }, modifier = Modifier
        .fillMaxHeight()
        .padding(if (isLandscape()) PaddingValues(10.dp) else PaddingValues(0.dp))
    )

    AnimatedVisibility(
      modifier = Modifier.align(Alignment.BottomCenter),
      visible = showController,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      /* 底部控制栏：按钮 + 进度条 */
      Column(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        /* 第二行：进度条 */
        Slider(
          value = progress,
          onValueChange = { isDragging = true; progress = it },
          onValueChangeFinished = {
            val dur = exoPlayer.duration
            if (dur > 0) exoPlayer.seekTo((progress * dur).toLong())
            isDragging = false
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
        )/* 第一行：播放/暂停、时间、全屏 */
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { exoPlayer.playWhenReady = !exoPlayer.playWhenReady }) {
              Icon(
                imageVector = if (exoPlayer.playWhenReady) Icons.Outlined.Pause
                else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            }
            Text(
              text = "${formatTime(exoPlayer.currentPosition)} / ${formatTime(total)}",
              style = MaterialTheme.typography.bodyMedium
            )
          }
          IconButton(onClick = onFullScreenButtonClick) {
            Icon(
              imageVector = Icons.Outlined.Fullscreen,
              contentDescription = "Fullscreen",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  }
}
