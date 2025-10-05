package com.mikufans.view

import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer

class PlayerViewModel : ViewModel() {
  var exoPlayer: ExoPlayer? = null
    private set

  fun initPlayer(context: android.content.Context) {
    if (exoPlayer == null) {
      exoPlayer = ExoPlayer.Builder(context).build()

    }
  }

  override fun onCleared() {
    exoPlayer?.release()
    exoPlayer = null
  }

  // 添加重置方法
  fun reset() {
    exoPlayer?.stop()
    exoPlayer?.release()
    exoPlayer = null
  }
}