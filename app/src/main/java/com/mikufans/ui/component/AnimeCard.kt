package com.mikufans.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.mikufans.R
import com.mikufans.xmd.teto.entity.SubjectSearch

@Composable
fun AnimeCard(
  anime: SubjectSearch.Subject,
  onTap: (animeId: Int, animeName: String) -> Unit = { _, _ -> }
) {
  Column(
    modifier = Modifier
      .clip(shape = ShapeDefaults.Medium)
      .clickable { onTap(anime.id!!, anime.nameCn ?: anime.name ?: "") },
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {

    AsyncImage(
      modifier = Modifier.aspectRatio(2 / 3f),
      model = anime.images?.medium,
      contentDescription = null,
      placeholder = painterResource(R.drawable.ahhhh),

      )
    Text(
      text = anime.nameCn ?: anime.name ?: "暂无标题",
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

