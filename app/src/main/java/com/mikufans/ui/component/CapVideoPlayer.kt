package com.mikufans.ui.component

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.mikufans.util.Orientation
import com.mikufans.util.RelativeTime.formatTime
import com.mikufans.view.CapPlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapVideoPlayer(
  modifier: Modifier = Modifier,
  videoUrl: String? = null,
  title: String,
  episodeIndex: Int = 0,
  showNextButton: Boolean = true,
  showPreviousButton: Boolean = true,
  navController: NavController? = null,
  playList: List<String> = listOf(),
  onLeadingBackButtonTab: () -> Unit = {},
  onEpisodeTab: (Int) -> Unit = {},
  onPositionChange: (Long) -> Unit = {},
  onDurationChange: (Long) -> Unit = {},
) {
  val current = LocalContext.current
  val window = (current as Activity).window
  val systemBars = WindowInsets.systemBars
  var isLandscape by rememberSaveable { mutableStateOf(false) }
  var isPlaying by rememberSaveable { mutableStateOf(false) }
  var showVideoController by rememberSaveable { mutableStateOf(true) }
  var showEpisodeList by rememberSaveable { mutableStateOf(false) }
  var sliderPosition by rememberSaveable { mutableFloatStateOf(0f) }
  var currentPosition by rememberSaveable { mutableLongStateOf(0L) }
  var duration by rememberSaveable { mutableLongStateOf(0L) }
  var isSpeedMenuOpen by rememberSaveable { mutableStateOf(false) }
  var isAspectMenuOpen by rememberSaveable { mutableStateOf(false) }
  var currentEpisodeIndex by rememberSaveable { mutableIntStateOf(episodeIndex) }
  var mediaPropertyChangeText by rememberSaveable { mutableStateOf("") }
  var showMediaPropertyChangeText by rememberSaveable { mutableStateOf(false) }
  var seekAccumulatePx by rememberSaveable { mutableFloatStateOf(0f) }
  var controllerHideJob by remember { mutableStateOf<Job?>(null) }
  val scope = rememberCoroutineScope()
  var resizeMode by rememberSaveable { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
  val playerViewModel: CapPlayerViewModel = viewModel()
  val exoPlayer = remember { playerViewModel.getPlayer(current) }
//初次加载播放器
  LaunchedEffect(Unit) {
    val url = videoUrl ?: playList.getOrNull(episodeIndex) ?: return@LaunchedEffect
    exoPlayer.setMediaItem(MediaItem.fromUri(url))
    exoPlayer.prepare()
    exoPlayer.playWhenReady = true
  }
  // 2. 切换视频时更换 MediaItem，不会重建播放器
  LaunchedEffect(currentEpisodeIndex) {
    val newUrl = videoUrl ?: playList.getOrNull(currentEpisodeIndex) ?: return@LaunchedEffect
    if (currentPosition == 0L) {
      exoPlayer.setMediaItem(MediaItem.fromUri(newUrl))
    } else {
      exoPlayer.seekTo(currentPosition)
    }
    exoPlayer.prepare()
    exoPlayer.playWhenReady = true
  }

  LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
    exoPlayer.pause()
  }
  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    exoPlayer.play()
  }
  LaunchedEffect(Unit) {
    while (true) {
      delay(1000)
      currentPosition = exoPlayer.currentPosition
      duration = exoPlayer.duration
      isPlaying = exoPlayer.isPlaying
      playerViewModel.setCurrentPosition(currentPosition)
      sliderPosition = currentPosition.toFloat() / duration.toFloat()
      onPositionChange(currentPosition)
      onDurationChange(duration)
    }
  }
  BackHandler(enabled = isLandscape()) {
    isLandscape = false
    onLeadingBackButtonTab()
    Orientation.forceOrientation(current, false)
  }/* ① 同步系统栏隐藏/显示 */
  LaunchedEffect(isLandscape) {

    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
    if (!isLandscape) {
      // 退出沉浸：显示状态栏+导航栏
      insetsController.show(WindowInsetsCompat.Type.systemBars())
      insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    } else {
      // 沉浸：隐藏状态栏+导航栏
      insetsController.hide(WindowInsetsCompat.Type.systemBars())
      insetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
  }

  LaunchedEffect(isPlaying, showVideoController) {
    if (!isPlaying || !showVideoController) {
      controllerHideJob?.cancel()
      return@LaunchedEffect
    }
    controllerHideJob?.cancel()
    controllerHideJob = launch {
      delay(5_000)
      showVideoController = false
    }
  }

  val resetControllerHideTimer = {
    controllerHideJob?.cancel()
    if (isPlaying && showVideoController) {
      controllerHideJob = scope.launch { delay(5_000); showVideoController = false }
    }
  }
  Box(
    modifier = if (isLandscape()) {
      modifier.fillMaxSize()
    } else {
      modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
    }.then(
      Modifier
        .background(Color.Black)
        .pointerInput(Unit) {
          // 只监听“按下”事件，不消费，仅用于重置计时器
          awaitPointerEventScope {
            while (true) {
              val event = awaitPointerEvent(PointerEventPass.Initial)
              // 只要检测到任何手指按下就重置
              if (event.changes.any { it.pressed }) {
                resetControllerHideTimer()
              }
              // 不调用 change.consume()，事件继续下发给子级
            }
          }
        }
        .clickable(
          indication = null, interactionSource = remember { MutableInteractionSource() }) {
          if (showEpisodeList) {
            showEpisodeList = false
            return@clickable
          }
          showVideoController = !showVideoController
        })
  ) {

    //视频播放器区域
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = false
          setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
          // 确保 SurfaceView 正确适配横屏
          setResizeMode(resizeMode)
        }
      }, update = { playerView ->
        playerView.setResizeMode(resizeMode) // 更新时也应用resizeMode
      }, modifier = Modifier.fillMaxSize()
    )
    //自定义控制区域
    Column(
      modifier = Modifier.padding(
        if (isLandscape()) PaddingValues(vertical = 15.dp, horizontal = 30.dp) else PaddingValues(
          0.dp
        )
      )
    ) {
      //头部：返回按钮+标题
      AnimatedVisibility(visible = showVideoController) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          IconButton(onClick = {
            if (isLandscape) {
              Orientation.forceOrientation(current, false)
            } else {
              navController?.popBackStack()
            }
          }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "Back"
            )
          }
          Text(text = title)
        }
      }
      //中间：
      Row(
        modifier = Modifier
          .weight(1f)
          /* ① 水平手势：进度微调 */
          .pointerInput(Unit) {
            if (!isLandscape) {
              return@pointerInput
            }
            detectHorizontalDragGestures(
              onDragStart = {
                showMediaPropertyChangeText = true
                seekAccumulatePx = 0f          // 重置累计位移
              },
              onDragEnd = {
                showMediaPropertyChangeText = false
                // 换算成毫秒并跳转
                val seekMs = (seekAccumulatePx / 80f * 1_000L).toLong()
                val target = (exoPlayer.currentPosition + seekMs)
                  .coerceIn(0L, exoPlayer.duration)
                exoPlayer.seekTo(target)
              }
            ) { _, dragAmount ->
              seekAccumulatePx += dragAmount        // 累计位移（右正左负）
              val seconds = (seekAccumulatePx / 80f).toInt()
              mediaPropertyChangeText =
                if (seconds >= 0) "前进 ${seconds}s" else "后退 ${-seconds}s"
            }
          }
      ) {
        //左侧：左半部分垂直手势监听控制画面亮度
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .pointerInput(Unit) {
              detectVerticalDragGestures(
                onDragStart = {
                  showMediaPropertyChangeText = true
                  Log.d("Gesture-brightness", "Drag start")
                },
                onDragEnd = {
                  showMediaPropertyChangeText = false
                  Log.d("Gesture-brightness", "Drag end")
                },
              ) { change, dragAmount ->
                if (!isLandscape) return@detectVerticalDragGestures
                // 监听 Y 轴滑动事件，实现调整亮度功能
                val delta = dragAmount / 1000f
                val attrs = window.attributes
                val old = attrs.screenBrightness
                val new = (old - delta).coerceIn(0.05f, 1.0f)
                Log.d("Gesture-brightness-newValue-oldValue", "Drag: $delta, old: $old, new: $new")
                attrs.screenBrightness = new          // ① 设置新值
                window.attributes = attrs               // ② 重新写回，立即生效
                val brightnessPercent = (new * 100).toInt()
                mediaPropertyChangeText = "亮度: ${brightnessPercent}%"
              }
            }) {}
        //右侧：右半部分控制音量
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxSize()
//            .background(Color.Yellow.copy(alpha = 0.5f))
            .pointerInput(Unit) {
              detectVerticalDragGestures(
                onDragStart = {
                  showMediaPropertyChangeText = true
                  Log.d("Gesture-volume", "Drag start")
                },
                onDragEnd = {
                  showMediaPropertyChangeText = false
                  Log.d("Gesture-volume", "Drag end")
                },
              ) { change, dragAmount ->
                if (!isLandscape) return@detectVerticalDragGestures
                val audioManager = current.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

                if (dragAmount < 0) {
                  // 向上滑动，增大音量
                  if (currentVolume < maxVolume) {
                    audioManager.adjustStreamVolume(
                      AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0
                    )
                    val volumePercent = ((currentVolume + 1) * 100 / maxVolume)
                    mediaPropertyChangeText = "音量: ${volumePercent}%"
                  }
                } else {
                  // 向下滑动，减小音量
                  if (currentVolume > 0) {
                    audioManager.adjustStreamVolume(
                      AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0
                    )
                    val volumePercent = ((currentVolume - 1) * 100 / maxVolume)
                    mediaPropertyChangeText = "音量: ${volumePercent}%"
                  }
                }
              }
            }) {
          AnimatedVisibility(
            visible = showEpisodeList && playList.isNotEmpty(),
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()) {
            LazyVerticalGrid(
              columns = GridCells.Fixed(5),
              contentPadding = PaddingValues(5.dp),
              horizontalArrangement = Arrangement.spacedBy(5.dp),
              verticalArrangement = Arrangement.spacedBy(5.dp),
              modifier = Modifier
                .align(Alignment.End)
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .background(
                  MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium
                )// 先裁剪出圆角
                .shadow(0.dp, MaterialTheme.shapes.medium)  // 再添加阴影，使用相同形状


            ) {
              itemsIndexed(playList) { index, item ->
                if (index == currentEpisodeIndex) {
                  Button(onClick = {

                  }) {
                    Text(text = item)
                  }
                } else {
                  OutlinedButton(onClick = {
                    currentEpisodeIndex = index
                    onEpisodeTab(index)
                  }) {
                    Text(text = item)
                  }
                }
              }
            }
          }
        }
      }

      ////控制器底部1:播放进度条
      AnimatedVisibility(visible = showVideoController) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
        ) {
          Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it.coerceIn(0f, 1f) },
            onValueChangeFinished = {
              Log.d("PlayerSlider", "onValueChangeFinished: $sliderPosition")
              exoPlayer.seekTo((sliderPosition * duration).toLong())
            },
            thumb = {
              Box(
                modifier = Modifier
                  .size(15.dp)
                  .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
              )
            },
            track = { sliderState ->
              SliderDefaults.Track(
                thumbTrackGapSize = 0.dp,
                trackInsideCornerSize = 0.dp,
                drawStopIndicator = {},
                modifier = Modifier.height(5.dp),
                colors = colors(
                  activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
                enabled = true,
                sliderState = sliderState
              )
            })
        }
      }
      Spacer(modifier = Modifier.height(1.dp))
      //控制器底部2：播放/暂停、上一个、下一个、进度条、全屏
      AnimatedVisibility(visible = showVideoController) {
        Row(
          modifier = Modifier
//          .background(Color.Red)
            .height(50.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          //上一个
          AnimatedVisibility(visible = isLandscape() && showPreviousButton) {
            IconButton(onClick = {
              if (playList.isEmpty() || currentEpisodeIndex <= 0) {
                return@IconButton
              }
              exoPlayer.setMediaItem(MediaItem.fromUri(playList[currentEpisodeIndex - 1]))
              exoPlayer.prepare()
              exoPlayer.seekTo(0L)
            }) {
              Icon(
                imageVector = Icons.Filled.SkipPrevious, contentDescription = "Previous"
              )
            }
          }
          //播放/暂停
          IconButton(onClick = { exoPlayer.playWhenReady = !exoPlayer.isPlaying }) {
            Icon(
              imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
              contentDescription = "Play or Pause"
            )
          }
          //下一个
          AnimatedVisibility(visible = isLandscape() && showNextButton) {
            IconButton(onClick = {
              if (playList.isEmpty() || currentEpisodeIndex >= playList.size - 1) {
                return@IconButton
              }
              exoPlayer.setMediaItem(MediaItem.fromUri(playList[currentEpisodeIndex + 1]))
              exoPlayer.prepare()
              exoPlayer.seekTo(0L)
            }) {
              Icon(
                imageVector = Icons.Filled.SkipNext, contentDescription = "Next"
              )
            }
          }
          //当前时间/总时间
          Text(
            text = "${formatTime(currentPosition)}/${
              formatTime(
                duration
              )
            }",
            textAlign = TextAlign.Center,
//          modifier = Modifier
//            .background(Color.Yellow)
          )
          Spacer(modifier = Modifier.weight(1f))

          //剧集选择按钮
          AnimatedVisibility(
            visible = isLandscape
          ) {
            Row {
              IconButton(
                onClick = { showEpisodeList = !showEpisodeList },
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = "Select episode")
              }
            }
          }
          //画面尺寸按钮
          AnimatedVisibility(visible = isLandscape) {
            Row {
              IconButton(
                onClick = {
                  isAspectMenuOpen = !isAspectMenuOpen
                },
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp)
              ) {
                Icon(Icons.Default.ImageAspectRatio, contentDescription = "Change aspect ratio")
              }

              DropdownMenu(
                properties = PopupProperties(focusable = false),
                expanded = isAspectMenuOpen,
                onDismissRequest = { isAspectMenuOpen = false }) {
                DropdownMenuItem(text = { Text("16:9") }, onClick = {
                  resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                  isAspectMenuOpen = false
                })
                DropdownMenuItem(text = { Text("4/3") }, onClick = {
                  resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                  isAspectMenuOpen = false
                })
                DropdownMenuItem(text = { Text("填充") }, onClick = {
                  resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                  isAspectMenuOpen = false
                })
                DropdownMenuItem(text = { Text("适应") }, onClick = {
                  resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                  isAspectMenuOpen = false
                })
              }

            }
          }
          //倍速按钮
          AnimatedVisibility(visible = isLandscape) {
            Row {
              IconButton(
                onClick = { isSpeedMenuOpen = !isSpeedMenuOpen },
                modifier = Modifier
                  .size(40.dp)
                  .padding(end = 8.dp)
              ) {
                Icon(Icons.Default.Speed, contentDescription = "Change speed")
              }

              DropdownMenu(
                properties = PopupProperties(focusable = false),
                expanded = isSpeedMenuOpen,
                onDismissRequest = { isSpeedMenuOpen = false }) {
                DropdownMenuItem(text = { Text("0.5x") }, onClick = {
                  exoPlayer.setPlaybackSpeed(0.5f)
                  isSpeedMenuOpen = false
                })
                DropdownMenuItem(text = { Text("1.0x") }, onClick = {
                  exoPlayer.setPlaybackSpeed(1.0f)
                  isSpeedMenuOpen = false
                })
                DropdownMenuItem(text = { Text("1.5x") }, onClick = {
                  exoPlayer.setPlaybackSpeed(1.5f)
                  isSpeedMenuOpen = false
                })
                DropdownMenuItem(text = { Text("2.0x") }, onClick = {
                  exoPlayer.setPlaybackSpeed(2.0f)
                  isSpeedMenuOpen = false
                })
              }

            }
          }
          IconButton(onClick = {
            isLandscape = !isLandscape
            Orientation.forceOrientation(current, isLandscape)
          }) {
            Icon(
              imageVector = Icons.Filled.Fullscreen, contentDescription = "Fullscreen"
            )
          }
        }
      }
    }
  }
  // 媒体属性变更提示
  AnimatedVisibility(
    visible = showMediaPropertyChangeText && mediaPropertyChangeText.isNotEmpty() && isLandscape,   // 用变量控制显隐
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
    ) {
      Text(
        text = mediaPropertyChangeText,
        textAlign = TextAlign.Center,
        color = Color.White,
        modifier = Modifier
          .padding(top = 32.dp)
          .background(
            Color.Black.copy(alpha = 0.6f), shape = CircleShape
          )
          .padding(horizontal = 12.dp, vertical = 6.dp)
      )
    }
  }
}


@Composable
fun isLandscape(): Boolean =
  LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

