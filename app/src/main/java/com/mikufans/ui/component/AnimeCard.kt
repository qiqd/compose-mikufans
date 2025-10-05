package com.mikufans.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.util.GifLoader
import com.mikufans.util.RelativeTime
import com.mikufans.xmd.miku.entiry.Anime

@Composable
fun AnimeCard(
  anime: Anime? = null,
  isSimple: Boolean = true,
  episodeIndex: Int = 0,
  dateTime: Long = 0L,
  onTap: (animeId: String, animeName: String) -> Unit = { _, _ -> }
) {

  Column(
    modifier = Modifier
      .clip(shape = ShapeDefaults.Medium)
      .clickable { onTap(anime?.id!!, anime.title ?: "") },
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    AsyncImage(
      modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(2f / 3f),
      model = anime?.coverUrl,
      contentDescription = anime?.title ?: "暂无标题",
      placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current),
    )
    Text(
      text = anime?.title ?: "暂无标题",
      textAlign = TextAlign.Center,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis,
      fontSize = MaterialTheme.typography.bodySmall.fontSize
    )
    if (!isSimple) {
      Text(
        text = RelativeTime.relativeTime(dateTime),
        fontSize = MaterialTheme.typography.bodySmall.fontSize,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = "观看至第${episodeIndex + 1}集",
        fontSize = MaterialTheme.typography.bodySmall.fontSize,
        textAlign = TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

