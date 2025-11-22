package com.mikufans.ui.component

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mikufans.R
import com.mikufans.util.GifLoader
import com.mikufans.util.RelativeTime

/**
 * 媒体卡片组件
 * @param id 媒体id
 * @param title 标题
 * @param titleCn 标题中文
 * @param author 作者
 * @param coverUrl 封面图片
 * @param status 状态
 * @param genre 类型
 * @param isLove 是否收藏
 * @param airDate 上映日期
 * @param episodeIndex 最新一集
 * @param lastViewAt 最后观看时间
 * @param onLoveBtnTab 收藏按钮点击事件
 * @param onViewBtnTab 观看按钮点击事件
 * @param onDetailBtnTab 详情按钮点击事件
 * @param onTap 卡片点击事件
 *
 */
@Composable
fun MediaCard(
  id: String,
  title: String? = null,
  titleCn: String? = null,
  author: String? = null,
  coverUrl: String? = null,
  status: String? = null,
  genre: String? = null,
  rating: String? = null,
  ratingCount: String? = null,
  duration: String? = null,
  totalEpisode: String? = null,
  language: String? = null,
  isLove: Boolean = false,
  airDate: String? = null,
  episodeIndex: Int? = null,
  lastViewAt: Long? = null,
  onLoveBtnTab: (Boolean) -> Unit = {},
  onViewBtnTab: (id: String) -> Unit = {},
  onDetailBtnTab: (id: String) -> Unit = {},
  onTap: (id: String) -> Unit = {}
) {
  val context = LocalContext.current
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(150.dp)
      .shadow(1.dp, MaterialTheme.shapes.medium)  // 再添加阴影，使用相同形状
      .clip(MaterialTheme.shapes.medium)
      .padding(5.dp)// 先裁剪出圆角
      .clickable { onTap(id) },
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
        model = coverUrl,
        contentDescription = titleCn ?: "暂无标题",
        placeholder = GifLoader.gifPlaceholder(R.drawable.loading, context),
      )
    }

    Column(
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 3.dp),
      verticalArrangement = Arrangement.Top
    ) {
      titleCn?.let {
        Text(
          text = titleCn,
          textAlign = TextAlign.Start,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
      }
      title?.let {
        Text(
          text = title,
          textAlign = TextAlign.Start,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = Color.Gray,
          fontSize = MaterialTheme.typography.bodySmall.fontSize
        )
      }
      rating?.let {
        Text(
          text = "$rating 分/$ratingCount 人评价",
          color = MaterialTheme.colorScheme.primary,
          fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
      }
      author?.let {
        Text(
          text = author,
          color = Color.Gray,
          fontSize = MaterialTheme.typography.bodySmall.fontSize
        )
      }
      status?.let {
        Text(
          text = status,
          color = MaterialTheme.colorScheme.primary,
          fontSize = MaterialTheme.typography.labelSmall.fontSize
        )
      }

      Column(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        airDate?.let {
          Text(
            color = Color.Gray,
            text = airDate,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        genre?.let {
          Text(
            color = Color.Gray,
            text = genre,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
      Spacer(modifier = Modifier.weight(1f))
      Column(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        lastViewAt?.let {
          Text(
            text = RelativeTime.relativeTime(lastViewAt),
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        episodeIndex?.let {
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

@Preview(showBackground = true)
@Composable
fun AnimeCardPreview() {
  MediaCard(
    id = "1",
    title = "赛马娘 芦毛灰姑娘",
    rating = "4.5",
    ratingCount = "1000",
    author = "author",
    titleCn = "赛马娘 芦毛灰姑娘",
    coverUrl = "https://bgm.girigirilove.com/upload/vod/20250501-1/3b36510a5360692dd83321e45d5024d0.webp",
    status = "更新至第1集",
    genre = " genre",
    isLove = true,
    airDate = "2021-09-05",
  )
}
