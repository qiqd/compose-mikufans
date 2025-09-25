package com.mikufans.util

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import androidx.compose.runtime.Composable
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

object GifLoader {
  /**
   * 返回支持 GIF 的 ImageLoader（调用方传 applicationContext 即可）
   */
  private fun buildImageLoader(context: Context): ImageLoader =
    ImageLoader.Builder(context)
      .components {
        if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
        else add(GifDecoder.Factory())
      }
      .build()

  /**
   * 对外唯一 API：传入 resId 与 Context（建议 applicationContext），
   * 返回可播放 GIF 的 Painter
   */
  @Composable
  fun gifPlaceholder(resId: Int, context: Context) = rememberAsyncImagePainter(
    model = resId,
    imageLoader = buildImageLoader(context)
  )
}
