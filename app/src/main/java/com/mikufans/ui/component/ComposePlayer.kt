package com.mikufans.ui.component

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.mikufans.util.RelativeTime.formatTime
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun ComposePlayer(

  activity: ComponentActivity,
  videoUrl: String,
  onPositionChange: (Long) -> Unit,
  onFullscreenToggle: (Boolean) -> Unit
) {
  var isFullscreen by rememberSaveable { mutableStateOf(false) }
  val url by rememberSaveable { mutableStateOf(videoUrl) }
  BackHandler(enabled = isFullscreen) {
    isFullscreen = false
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
  }

  SimpleExoPlayer(
    videoUrl = url,
    isFullscreen = isFullscreen,
    onFullscreenToggle = {
      isFullscreen = !isFullscreen
      onFullscreenToggle(isFullscreen)
      activity.requestedOrientation = if (isFullscreen) {
        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
      } else {
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      }
    },
    onPositionChange = onPositionChange,
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(16 / 10f)
  )
}

/* --------------------- 播放器实现 --------------------- */
private var globalPlayer: ExoPlayer? = null   // 进程级单例

@Composable
fun SimpleExoPlayer(
  videoUrl: String,
  isFullscreen: Boolean,
  onFullscreenToggle: () -> Unit,
  onPositionChange: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  var showController by rememberSaveable { mutableStateOf(true) }
  var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
  var duration by rememberSaveable { mutableLongStateOf(0L) }
  val isPlaying by rememberSaveable { mutableStateOf(false) }
  /* 1. Application 级单例：进程活着就永不 release */
  val app = context.applicationContext as android.app.Application
  val exoPlayer = remember {
    globalPlayer ?: ExoPlayer.Builder(app).build().also { globalPlayer = it }
  }

  /* 2. 保存/恢复进度（横竖屏可存活） */
  var lastPosition by rememberSaveable { mutableLongStateOf(0L) }
  var wasPlaying by rememberSaveable { mutableStateOf(true) }

  /* 3. 真正换集时才更换 MediaItem；横竖屏不会走到这里 */
  LaunchedEffect(videoUrl) {
    lastPosition = exoPlayer.currentPosition
    exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
    exoPlayer.prepare()
    exoPlayer.playWhenReady = wasPlaying
    /* 4. 恢复上一次的进度 */
    exoPlayer.seekTo(lastPosition)
  }

  /* 5. 每秒上报进度，同时更新保存值 */
  LaunchedEffect(exoPlayer) {
    while (true) {
      kotlinx.coroutines.delay(1000)
      lastPosition = exoPlayer.currentPosition
      wasPlaying = exoPlayer.isPlaying
      onPositionChange(lastPosition)
    }
  }
  /* 6. 生命周期只做暂停/继续，不做 release */
  DisposableEffect(lifecycle) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
        Lifecycle.Event.ON_RESUME -> if (wasPlaying) exoPlayer.play()
        else -> Unit
      }
    }
    lifecycle.addObserver(observer)
    onDispose { lifecycle.removeObserver(observer) }
  }

// ... 原有代码 ...
  Box(modifier.fillMaxWidth()) {
    /* 1. 先把手势挂到最外层，保证不被子组件拦截 */
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val window = (context as Activity).window
    var tipText by remember { mutableStateOf("") }

    val gestureModifier = Modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        awaitEachGesture {
          val down = awaitFirstDown()
          val downX = down.position.x
          val drag = awaitDragOrCancellation(down.id) ?: return@awaitEachGesture
          val dx = drag.position.x - downX
          val dy = drag.position.y - down.position.y

          if (abs(dx) > abs(dy)) {               // 左右滑动
            if (dx > 0) {
              exoPlayer.seekTo(exoPlayer.currentPosition + 5_000)
              tipText = "前进 5 秒"
            } else {
              exoPlayer.seekTo(exoPlayer.currentPosition - 5_000)
              tipText = "后退 5 秒"
            }
          } else {                                 // 上下滑动
            val percent = (-dy / size.height * 100).toInt()
            val centerX = size.width / 2
            if (downX < centerX) {               // 左半边 → 亮度
              val layout = window.attributes
              val newBright = (layout.screenBrightness * 255 + percent * 2.55f)
                .coerceIn(0f, 255f)
              layout.screenBrightness = newBright / 255f
              window.attributes = layout
              tipText = "亮度 ${(newBright / 255f * 100).toInt()}%"
            } else {                             // 右半边 → 音量
              val newVolume = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) +
                      percent * maxVolume / 100)
                .toInt().coerceIn(0, maxVolume)
              audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
              tipText = "音量 ${newVolume * 100 / maxVolume}%"
            }
          }
        }
      }

    /* 2. 顶部提示条（1.5 秒后自动消失） */
    if (tipText.isNotEmpty()) {
      Box(
        Modifier
          .fillMaxWidth()
          .then(gestureModifier)
          .padding(top = 32.dp),
        contentAlignment = Alignment.TopCenter
      ) {
        Text(
          text = tipText,
          color = MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.bodyLarge
        )
      }
      LaunchedEffect(tipText) {
        delay(1500)
        tipText = ""
      }
    }

    /* 3. 视频画面（不挂手势，避免重复） */
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = false
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .clickable { showController = !showController }
    )

    /* 4. 控制器（不再挂手势） */
    CustomPlayerController(
      exoPlayer = exoPlayer,
      showControl = showController,
      isFullscreen = isFullscreen,
      onFullscreenToggle = onFullscreenToggle,
      currentPosition = currentPosition,
      duration = duration,
      isPlaying = isPlaying,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
    )
  }

}

/* --------------------- 自定义控制器 --------------------- */
@Composable
fun CustomPlayerController(
  exoPlayer: ExoPlayer,
  showControl: Boolean,
  isFullscreen: Boolean,
  onFullscreenToggle: () -> Unit,
  currentPosition: Long,
  duration: Long,
  isPlaying: Boolean,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  val window = (context as Activity).window
  var tipText by remember { mutableStateOf("") }   // 顶部提示
// 2. 手势监听代码块替换（其余代码保持原样）
  val gestureModifier = Modifier.pointerInput(Unit) {
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    awaitEachGesture {
      val down = awaitFirstDown()
      val downX = down.position.x
      val downY = down.position.y
      var drag: PointerInputChange?
      do {
        drag = awaitDragOrCancellation(down.id)   // 传入 id 即可
      } while (drag != null && !drag.isConsumed)
      drag ?: return@awaitEachGesture

      val dx = drag.position.x - downX
      val dy = drag.position.y - downY

      if (abs(dx) > abs(dy)) {                       // 左右滑动
        if (dx > 0) {
          exoPlayer.seekTo(exoPlayer.currentPosition + 5_000)
          tipText = "前进 5 秒"
        } else {
          exoPlayer.seekTo(exoPlayer.currentPosition - 5_000)
          tipText = "后退 5 秒"
        }
      } else {                                         // 上下滑动
        val percent = (-dy / size.height * 100).toInt()
        val centerX = size.width / 2
        if (downX < centerX) {                       // 左半边 → 亮度
          val layout = window.attributes
          val newBright = (layout.screenBrightness * 255 + percent * 2.55f)
            .coerceIn(0f, 255f)
          layout.screenBrightness = newBright / 255f
          window.attributes = layout
          tipText = "亮度 ${(newBright / 255f * 100).toInt()}%"
        } else {                                     // 右半边 → 音量
          val newVolume = (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) +
                  percent * maxVolume / 100)
            .toInt().coerceIn(0, maxVolume)
          audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
          tipText = "音量 ${newVolume * 100 / maxVolume}%"
        }
      }
    }
  }

  /* 顶部提示条（1.5 秒后自动消失） */
  if (tipText.isNotEmpty()) {
    Box(
      Modifier
        .fillMaxWidth()
        .padding(top = 32.dp),
      contentAlignment = Alignment.TopCenter
    ) {
      Text(
        text = tipText,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge
      )
    }
    LaunchedEffect(tipText) {
      kotlinx.coroutines.delay(1500)
      tipText = ""
    }
  }

  /* 原有控制器 UI，仅在外层挂手势 */
  AnimatedVisibility(visible = showControl, enter = fadeIn(), exit = fadeOut()) {
    Column(
      modifier = modifier
        .fillMaxWidth()
        .padding(if (isFullscreen) 80.dp else 16.dp)
//        .then(gestureModifier)
    ) {
//      if (isFullscreen) {
//        Row {
//          IconButton(onClick = { if (isFullscreen) onFullscreenToggle() }) {
//            Icon(Icons.AutoMirrored.Outlined.ArrowBackIos, contentDescription = "Back")
//          }
//        }
//      }
      Spacer(Modifier.weight(1f))

      /* 进度条 */
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Slider(
          value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
          onValueChange = { progress ->
            exoPlayer.seekTo((progress * duration).toLong())
            exoPlayer.play()
          },
          modifier = Modifier.weight(1f)
        )
      }

      /* 控制按钮行 */
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() }) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play"
          )
        }
        IconButton(onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 10_000) }) {
          Icon(Icons.Default.SkipNext, contentDescription = "Skip Forward")
        }
        IconButton(onClick = { /* 音量逻辑 */ }) {
          Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume")
        }
        Text(
          text = formatTime(currentPosition) + " / " + formatTime(duration),
          style = MaterialTheme.typography.bodySmall
        )
        IconButton(onClick = { /* 设置逻辑 */ }) {
          Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
        IconButton(onClick = onFullscreenToggle) {
          Icon(Icons.Default.Fullscreen, contentDescription = "Toggle Fullscreen")
        }
      }
    }
  }
}
