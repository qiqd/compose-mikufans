package com.mikufans.ui.component

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.util.GifLoader
import com.mikufans.util.RelativeTime
import org.anime.api.AnimeApi
import org.anime.entity.Animation

@Composable
fun AnimeCard(
  anime: Animation? = null,
  isSimple: Boolean = true,
  episodeIndex: Int = 0,
  dateTime: Long = 0L,
  onTap: (animeSubId: Int, animeName: String) -> Unit = { _, _ -> }
) {
  val context = LocalContext.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(150.dp)
      .clip(MaterialTheme.shapes.medium)  // 先裁剪出圆角
      .shadow(0.dp, MaterialTheme.shapes.medium)  // 再添加阴影，使用相同形状
      .clickable {
        if (AnimeApi.SOURCES_WITH_DELAY.isEmpty()) {
          Toast.makeText(context, "数据源加载中，请稍后再试", Toast.LENGTH_SHORT).show()
          return@clickable
        }
        onTap(anime?.subId!!, anime.titleCn ?: anime.title ?: "")
      },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(5.dp)
  ) {

    Column {
      AsyncImage(
        modifier = Modifier
          .fillMaxHeight()
          .aspectRatio(2.5f / 3f)
          .clip(MaterialTheme.shapes.medium),
        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
        model = anime?.coverUrls[0],
        contentDescription = anime?.titleCn ?: "暂无标题",
        placeholder = GifLoader.gifPlaceholder(R.drawable.loading, LocalContext.current),
      )
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 3.dp),
      verticalArrangement = Arrangement.Top
    ) {
      Text(
        text = anime?.titleCn ?: "暂无中文标题",
        textAlign = TextAlign.Start,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        fontSize = MaterialTheme.typography.bodyMedium.fontSize
      )
      Text(
        text = anime?.title ?: "暂无标题",
        textAlign = TextAlign.Start,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = Color.Gray,
        fontSize = MaterialTheme.typography.bodySmall.fontSize
      )
      Text(
        text = anime?.status ?: "暂无更新信息",
        color = MaterialTheme.colorScheme.primary,
        fontSize = MaterialTheme.typography.labelSmall.fontSize
      )
      AnimatedVisibility(
        visible = isSimple
      ) {
        Column(
          modifier = Modifier.padding(vertical = 2.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Text(
            color = Color.Gray,
            text = anime?.ariDate ?: "暂无放送日期",
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            color = Color.Gray,
            text = anime?.genre ?: "暂无类型",
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
      Spacer(modifier = Modifier.weight(1f))

      AnimatedVisibility(
        visible = !isSimple
      ) {
        Column(
          modifier = Modifier.padding(vertical = 2.dp),
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
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

  }
}

