import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mikufans.R
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.miku.entiry.Source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetail(animeDetail: AnimeDetail) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 封面和标题部分
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // 封面图片
                AsyncImage(
                    model = animeDetail.anime?.coverUrl,
                    contentDescription = "动漫封面",
                    placeholder = painterResource(R.drawable.ahhhh),
                    error = painterResource(R.drawable.ahhhh),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Top)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 标题和基本信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = animeDetail.anime?.title ?: "未知标题",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    animeDetail.anime?.status?.let { status ->
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    animeDetail.anime?.type?.let { type ->
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // 简介部分
        animeDetail.anime?.description?.let { description ->
            item {
                Text(
                    text = "简介",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // 导演和演员信息
        item {
            animeDetail.anime?.director?.let { director ->
                Text(
                    text = "导演: $director",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            animeDetail.anime?.actor?.let { actor ->
                Text(
                    text = "主演: $actor",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // 播放源和剧集列表
        animeDetail.sources?.let { sources ->
            items(sources) { source ->
                SourceSection(source = source)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SourceSection(source: Source) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // 播放源标题
        source.name?.let { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 剧集列表
        source.episodes?.let { episodes ->
            FlowRow {
                episodes.forEach { episode ->
                    episode.title?.let { title ->
                        AssistChip(
                            onClick = { /* TODO: 处理剧集点击事件 */ },
                            label = { Text(text = title) },
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
