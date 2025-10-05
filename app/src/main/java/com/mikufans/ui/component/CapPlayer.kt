package com.mikufans.ui.component

import android.content.pm.ActivityInfo
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.mikufans.ui.page.formatTime
import com.mikufans.ui.page.isLandscape
import com.mikufans.util.Orientation
import com.mikufans.view.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
        /* ====================== 播放器 UI 壳 ====================== */
fun CapPlayer(

  videoUrl: String,
  position: Long,
  modifier: Modifier,
  activity: ComponentActivity,
  onFullScreenButtonClick: (Boolean) -> Unit = {},
  onProcessChange: (Long) -> Unit = {}
) {
  val content = LocalContext.current
  val vm: PlayerViewModel = viewModel()
  vm.initPlayer(content)
  val exoPlayer = vm.exoPlayer!!   // ① 保证非空
  var currentPosition by rememberSaveable { mutableLongStateOf(position) }
  var progress by rememberSaveable { mutableFloatStateOf(0f) }
  var isDragging by rememberSaveable { mutableStateOf(false) }
  val tempUrl by rememberSaveable { mutableStateOf(videoUrl) }
  val coroutineScope = rememberCoroutineScope()
  var showController by rememberSaveable { mutableStateOf(false) }
  var isFullscreen by rememberSaveable { mutableStateOf(false) }
  val total by remember(exoPlayer.duration) {
    derivedStateOf {
      exoPlayer.duration.coerceAtLeast(
        0L
      )
    }
  }
  //1.但传入的url不同的时候更新播放器，场景一般是剧集切换，或者初次加载的时候
  LaunchedEffect(tempUrl) {
    exoPlayer.pause()
    progress = 0f
    isDragging = false
//    currentUrl = tempUrl
    exoPlayer.setMediaItem(MediaItem.fromUri(tempUrl.toUri()))
    exoPlayer.prepare()
    exoPlayer.playWhenReady = true
    exoPlayer.seekTo(position.coerceAtLeast(0L))
    isDragging = true
  }
  //2.定时保存进度与播放状态
  LaunchedEffect(exoPlayer) {
    while (true) {
      kotlinx.coroutines.delay(500)
      currentPosition = exoPlayer.currentPosition
      onProcessChange(exoPlayer.currentPosition)
    }
  }
  LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
    exoPlayer.pause()
  }
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    exoPlayer.play()
  }
  BackHandler(enabled = isLandscape()) {
    Orientation().forceOrientation(content, false)
  }

  /* ② 首次/历史进度：拿到总时长后一次性同步 progress */
  LaunchedEffect(total) {
    Log.d("CapPlayer", "Total duration: $total, Current position: ${exoPlayer.currentPosition}")
    if (total > 0) {
      progress = exoPlayer.currentPosition.toFloat() / total
    }
  }
//视频的播放位置改变的时候同时改变进度条
  LaunchedEffect(currentPosition) {
    while (true) {
      kotlinx.coroutines.delay(16)
      val dur = total
      if (!isDragging && dur > 0) {
        progress = exoPlayer.currentPosition.toFloat() / dur
      }
    }
  }
  /* ① 同步系统栏隐藏/显示 */
  DisposableEffect(showController) {
    if (!isFullscreen) return@DisposableEffect onDispose { }
    val window = (content as android.app.Activity).window
    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
    if (showController) {
      // 退出沉浸：显示状态栏+导航栏
      insetsController.show(WindowInsetsCompat.Type.systemBars())
      insetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    } else if (isFullscreen) {
      // 沉浸：隐藏状态栏+导航栏
      insetsController.hide(WindowInsetsCompat.Type.systemBars())
      insetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
    onDispose { }
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
          setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
          useController = false
          setFullscreenButtonState(isFullscreen)
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
          IconButton(onClick = {
            isFullscreen = !isFullscreen
            onFullScreenButtonClick(isFullscreen)
            activity.requestedOrientation = if (isFullscreen) {
              ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
              ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
          }) {
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
