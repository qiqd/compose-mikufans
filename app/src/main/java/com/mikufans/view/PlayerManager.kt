package com.mikufans.view


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer

class CapPlayerViewModel : ViewModel() {
  private var exoPlayer: ExoPlayer? = null
  private var currentPosition: Long = 0L

  fun getPlayer(context: Context): ExoPlayer {
    if (exoPlayer == null) {
      exoPlayer = ExoPlayer.Builder(context.applicationContext).build()
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

  private fun releasePlayer() {
    exoPlayer?.release()
    exoPlayer = null
  }
}
