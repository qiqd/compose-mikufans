package com.mikufans.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class CapPlayerViewModel : ViewModel() {
  private var exoPlayer: ExoPlayer? = null
  private var currentPosition: Long = 0L
  private var currentUrl = ""
  private var errorListener: Player.Listener? = null
  fun getPlayer(
    context: Context,
    onError: (PlaybackException) -> Unit = {}
  ): ExoPlayer {
    if (exoPlayer == null) {
      exoPlayer = ExoPlayer.Builder(context.applicationContext).build()
    }
    // 2. 每次获取时都先清掉旧监听，再注册新监听
    exoPlayer?.let { player ->
      errorListener?.let { player.removeListener(it) }
      val newListener = object : Player.Listener {
        override fun onPlayerError(error: PlaybackException) {
          onError(error)
        }
      }
      player.addListener(newListener)
      errorListener = newListener
    }
    return exoPlayer!!
  }

  fun getCurrentPosition(): Long = currentPosition

  fun setCurrentPosition(position: Long) {
    currentPosition = position
  }

  override fun onCleared() {
    super.onCleared()
    releasePlayer()
  }

  fun releasePlayer() {
    exoPlayer?.release()
    exoPlayer = null
  }

  fun getCurrentUrl(): String = currentUrl

  fun setCurrentUrl(url: String) {
    currentUrl = url
  }
}
