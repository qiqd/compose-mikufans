package com.mikufans.ui.component

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ImageAspectRatio
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
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
import com.mikufans.view.CapVideoPlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapVideoPlayer(
  title: String,
  isLoading: Boolean = false,
  mediaItems: List<MediaItem> = emptyList(),
  episodeIndex: Int = 0,
  showHeader: Boolean = true,
  showNextButton: Boolean = true,
  showPreviousButton: Boolean = true,
  navController: NavController,
  onNextTab: () -> Unit = {},
  onPreviousTab: () -> Unit = {},
  onPlayPauseTab: (Boolean) -> Unit = {},
  onLeadingBackButtonTab: () -> Unit = {},
  onEpisodeTab: (Int) -> Unit = {},
  onLandscapeChange: (Boolean) -> Unit = {},
  onPlayerError: (Exception) -> Unit = {},
) {
  val current = LocalContext.current
  val window = (current as Activity).window
  val systemBars = WindowInsets.systemBars
  val deviceDensity = LocalDensity.current.density
  var isLandscape by rememberSaveable { mutableStateOf(false) }
  var isPlaying by rememberSaveable { mutableStateOf(false) }
  var showVideoController by rememberSaveable { mutableStateOf(false) }
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
  var playbackSpeed by rememberSaveable { mutableFloatStateOf(1f) }
  val capVideoPlayerViewModel: CapVideoPlayerViewModel = viewModel()
  var controllerLocked by remember { mutableStateOf(false) }

  val exoPlayer = remember {
    capVideoPlayerViewModel.getPlayer(current) { exception ->
      onPlayerError(exception)
    }
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
      capVideoPlayerViewModel.setCurrentPosition(currentPosition)
      sliderPosition = currentPosition.toFloat() / duration.toFloat()
    }
  }
  BackHandler(enabled = isLandscape) {
    isLandscape = false
    onLeadingBackButtonTab()
    onLandscapeChange(isLandscape)
    Orientation.forceOrientation(current, false)
  }
  // 同步系统栏隐藏/显示
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
// 显示/隐藏控制栏 （播放时自动隐藏，5秒无操作后显示）
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
    modifier = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      Modifier
        .fillMaxSize()
        .background(Color.Black)
    } else {
      Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
    }.then(
      Modifier
        .background(Color.Black)
        .pointerInput(Unit) {
          detectTapGestures(
            // 双击暂停/播放
            onDoubleTap = {
              Log.e("CapVideoPlayer", "onDoubleTap: $isPlaying")
              capVideoPlayerViewModel.pausePlayer(isPlaying)
            },
            // 按下即重置 5 s 计时器
            onPress = {
              resetControllerHideTimer()
              awaitRelease()
              // 必须消费事件，否则 Up 事件收不到
            },
            //单击显示 / 隐藏控制器
            onTap = {
              if (showEpisodeList) {
                showEpisodeList = false
                return@detectTapGestures
              }
              showVideoController = !showVideoController
            })
        })
  ) {

    //视频播放器区域
    AndroidView(factory = { ctx ->
      PlayerView(ctx).apply {
        useController = false
        player = exoPlayer
        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
        // 确保 SurfaceView 正确适配横屏
        setResizeMode(resizeMode)
      }
    }, update = { playerView ->
      playerView.setResizeMode(resizeMode) // 更新时也应用resizeMode
    }, modifier = Modifier.fillMaxSize().also {
      if (isLandscape) {
        Modifier.background(Color.Black)
      }
    })
    //自定义控制区域
    Box(Modifier.fillMaxSize()) {
      AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      Column(
        modifier = Modifier.padding(
          if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) PaddingValues(
            vertical = 15.dp, horizontal = 30.dp
          ) else PaddingValues(
            0.dp
          )
        ).also {
          if (isLandscape) {
            Modifier.background(Color.Black)
          }
        }) {
        //头部：返回按钮+标题
        AnimatedVisibility(visible = showVideoController && showHeader && !controllerLocked) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Start,
              modifier = Modifier.weight(1f)
            ) {
              IconButton(onClick = {
                if (isLandscape) {
                  isLandscape = false
                  Orientation.forceOrientation(current, false)
                } else {
                  navController.popBackStack()
                }
              }) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                  contentDescription = "Back"
                )
              }
              Text(text = title)
            }
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
              Row(modifier = Modifier.wrapContentWidth()) {
                Text(text = "${LocalDateTime.now().hour}:${LocalDateTime.now().minute}")
              }
              Row(modifier = Modifier.weight(1f)) {}
            }
          }
        }
        //中间：
        Row(
          modifier = Modifier
            .weight(1f)/* ① 水平手势：进度微调 */.pointerInput(controllerLocked) {
              if (!isLandscape || controllerLocked) return@pointerInput
              detectHorizontalDragGestures(onDragStart = {
                showMediaPropertyChangeText = true
                seekAccumulatePx = 0f          // 重置累计位移
              }, onDragEnd = {
                showMediaPropertyChangeText = false
                // 换算成毫秒并跳转
                val seekMs = (seekAccumulatePx / 80f * 1_000L).toLong()
                val target = (exoPlayer.currentPosition + seekMs).coerceIn(0L, exoPlayer.duration)
                exoPlayer.seekTo(target)
              }) { _, dragAmount ->
                seekAccumulatePx += dragAmount        // 累计位移（右正左负）
                val seconds = (seekAccumulatePx / 80f).toInt()
                mediaPropertyChangeText =
                  if (seconds >= 0) "前进 ${seconds}s" else "后退 ${-seconds}s"
              }
            }) {
          //左侧：左半部分垂直手势监听控制画面亮度
          Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
              .pointerInput(controllerLocked) {
                if (!isLandscape || controllerLocked) return@pointerInput
                detectVerticalDragGestures(
                  onDragStart = {
                    showMediaPropertyChangeText = true
                    Log.d("Gesture-brightness", "Drag start")
                  },
                  onDragEnd = {
                    showMediaPropertyChangeText = false
                    Log.d("Gesture-brightness", "Drag end")
                  },
                ) { _, dragAmount ->

                  // 监听 Y 轴滑动事件，实现调整亮度功能
                  val attrs = window.attributes
                  val old = when (attrs.screenBrightness) {
                    // 首次为 -1，用系统亮度（0~1）代替
                    WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE -> {
                      // 0~255 → 0~1
                      Settings.System.getInt(
                        current.contentResolver, Settings.System.SCREEN_BRIGHTNESS
                      ) / 255f
                    }

                    else -> attrs.screenBrightness
                  }.coerceIn(0.01f, 1f)   // 保底限幅
                  val delta = dragAmount / 1000f
                  val new = (old - delta).coerceIn(0.01f, 1.0f)
                  Log.d("Gesture-brightness", "old=$old new=$new")
                  attrs.screenBrightness = new
                  window.attributes = attrs
                  val brightnessPercent = (new * 100).toInt()
                  mediaPropertyChangeText = "亮度: ${brightnessPercent}%"

                }
              }) {
            AnimatedVisibility(
              visible = showVideoController && isLandscape, enter = fadeIn(), exit = fadeOut()
            ) {
              IconButton(
                modifier = Modifier.clip(CircleShape),
                onClick = { controllerLocked = !controllerLocked }) {
                Icon(
                  imageVector = if (controllerLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                  contentDescription = "锁定/解锁控制器"
                )
              }
            }
          }

          //右侧：右半部分控制音量
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxSize()
              .pointerInput(controllerLocked) {
                if (!isLandscape || controllerLocked) return@pointerInput
                detectVerticalDragGestures(
                  onDragStart = {
                    showMediaPropertyChangeText = true
                    Log.d("Gesture-volume", "Drag start")
                  },
                  onDragEnd = {
                    showMediaPropertyChangeText = false
                    Log.d("Gesture-volume", "Drag end")
                  },
                ) { _, dragAmount ->
                  val density = dragAmount / deviceDensity
                  if ((density % 5).toInt() != 0) return@detectVerticalDragGestures

                  val audioManager = current.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                  if (dragAmount < 0) {
                    audioManager.adjustStreamVolume(
                      AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0
                    )
                  } else {
                    audioManager.adjustStreamVolume(
                      AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0
                    )
                  }
                  val maxVolume =
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceIn(1, 100)
                  val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                  val volumePercent = (currentVolume.toFloat() / maxVolume * 100).toInt()
                  mediaPropertyChangeText = "音量: ${volumePercent}%"
                }
              }) {
            // 显示/隐藏剧集列表
            AnimatedVisibility(
              visible = showEpisodeList,
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
                if (mediaItems.isNotEmpty()) {
                  itemsIndexed(mediaItems) { index, item ->
                    if (index == currentEpisodeIndex) {
                      Button(onClick = {

                      }) {
                        Text(text = index.toString())
                      }
                    } else {
                      OutlinedButton(onClick = {
                        currentEpisodeIndex = index
                        onEpisodeTab(index)
                      }) {
                        Text(text = index.toString())
                      }
                    }
                  }
                }
              }
            }
          }
        }

        //控制器底部1:播放进度条
        AnimatedVisibility(visible = showVideoController && !controllerLocked) {
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
              // 自定义滑块拇指
              thumb = {
                Box(
                  modifier = Modifier
                    .size(15.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
              },
              // 自定义滑块轨道
              track = { sliderState ->
                BoxWithConstraints(
                  modifier = Modifier
                    .height(5.dp)
                    .fillMaxWidth()
                ) {
                  val widthPx = constraints.maxWidth.toFloat()
                  val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                  // 底层：缓冲进度
                  Canvas(modifier = Modifier.fillMaxSize()) {
                    val bufferRatio = if (duration > 0L) {
                      exoPlayer.bufferedPosition.toFloat() / duration.toFloat()
                    } else 0f
                    if (bufferRatio > 0f) {
                      drawRect(
                        color = color, size = Size(widthPx * bufferRatio, size.height)
                      )
                    }
                  }/* 上层：系统默认轨道（已播放进度） */
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
                }
              })
          }
        }
        Spacer(modifier = Modifier.height(1.dp))
        //控制器底部2：播放/暂停、上一个、下一个、进度条、全屏
        AnimatedVisibility(visible = showVideoController && !controllerLocked) {
          Row(
            modifier = Modifier
//          .background(Color.Red)
              .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
          ) {
            //上一个
            AnimatedVisibility(visible = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && showPreviousButton) {
              IconButton(onClick = {
                if (mediaItems.isEmpty() || currentEpisodeIndex <= 0) {
                  return@IconButton
                }
                onPreviousTab()
              }) {
                Icon(
                  imageVector = Icons.Filled.SkipPrevious, contentDescription = "Previous"
                )
              }
            }
            //播放/暂停
            IconButton(onClick = {
              onPlayPauseTab(exoPlayer.isPlaying)
              exoPlayer.playWhenReady = !exoPlayer.isPlaying
            }) {
              Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play or Pause"
              )
            }
            //下一个
            AnimatedVisibility(visible = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE && showNextButton) {
              IconButton(onClick = {
                if (mediaItems.isEmpty() || currentEpisodeIndex >= mediaItems.size - 1) {
                  return@IconButton
                }
                onNextTab()
              }) {
                Icon(
                  imageVector = Icons.Filled.SkipNext, contentDescription = "Next"
                )
              }
            }
            //当前时间/总时间
            Text(
              text = "${formatTime(currentPosition)}/${formatTime(duration)}",
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.weight(1f))

            //TODO 添加视频清晰度选择按钮 -2025-10-21

            //剧集选择按钮
            AnimatedVisibility(
              visible = isLandscape && mediaItems.isNotEmpty()
            ) {
              Row {
                IconButton(
                  onClick = { showEpisodeList = !showEpisodeList },
                  modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
                ) {
                  Icon(
                    Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = "Select episode"
                  )
                }
              }
            }
            //画面尺寸按钮
            AnimatedVisibility(visible = isLandscape) {
              Row {
                BadgedBox(
                  badge = {
                    Text(
                      text = when (resizeMode) {
                        AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH -> "16:9"
                        AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT -> "4:3"
                        AspectRatioFrameLayout.RESIZE_MODE_FILL -> "填充"
                        AspectRatioFrameLayout.RESIZE_MODE_FIT -> "适应"
                        else -> ""
                      }, fontSize = MaterialTheme.typography.titleSmall.fontSize
                    )
                  }) {
                  IconButton(
                    onClick = {
                      isAspectMenuOpen = !isAspectMenuOpen
                    }, modifier = Modifier.size(40.dp)
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
                    DropdownMenuItem(text = { Text("4:3") }, onClick = {
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
            }
            //倍速按钮
            AnimatedVisibility(visible = isLandscape) {
              Row {
                BadgedBox(
                  badge = {
                    Text(
                      text = "${playbackSpeed}x",
                      fontSize = MaterialTheme.typography.titleSmall.fontSize
                    )
                  }) {
                  IconButton(
                    onClick = { isSpeedMenuOpen = !isSpeedMenuOpen },
                    modifier = Modifier.size(40.dp)
                  ) {
                    Icon(Icons.Default.Speed, contentDescription = "Change speed")
                  }

                  DropdownMenu(
                    properties = PopupProperties(focusable = false),
                    expanded = isSpeedMenuOpen,
                    onDismissRequest = { isSpeedMenuOpen = false }) {
                    DropdownMenuItem(text = { Text("0.5x") }, onClick = {
                      exoPlayer.setPlaybackSpeed(0.5f)
                      playbackSpeed = 0.5f
                      isSpeedMenuOpen = false
                    })
                    DropdownMenuItem(text = { Text("1.0x") }, onClick = {
                      exoPlayer.setPlaybackSpeed(1.0f)
                      playbackSpeed = 1.0f
                      isSpeedMenuOpen = false
                    })
                    DropdownMenuItem(text = { Text("1.5x") }, onClick = {
                      exoPlayer.setPlaybackSpeed(1.5f)
                      playbackSpeed = 1.5f
                      isSpeedMenuOpen = false
                    })
                    DropdownMenuItem(text = { Text("2.0x") }, onClick = {
                      exoPlayer.setPlaybackSpeed(2.0f)
                      playbackSpeed = 2.0f
                      isSpeedMenuOpen = false
                    })
                  }

                }
              }
            }
            IconButton(onClick = {
              isLandscape = !isLandscape
              onLandscapeChange(isLandscape)
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
      enter = fadeIn(), exit = fadeOut(), modifier = Modifier.zIndex(Float.MAX_VALUE)
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
}

