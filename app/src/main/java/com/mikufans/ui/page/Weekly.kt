import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikufans.ui.component.AnimeCard
import com.mikufans.xmd.access.AAFunAccessPoint
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.teto.entity.DailySchedule
import com.mikufans.xmd.teto.entity.SubjectSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Weekly() {
    val anime = Anime();
    anime.coverUrl = "https://img.pan.kg/images/363957_pgptl.webp"
    anime.title = "夏日口袋"
    val tabs = arrayOf("一", "二", "三", "四", "五", "六", "日")
    val coroutineScope = rememberCoroutineScope()
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var weekly by rememberSaveable { mutableStateOf(List<DailySchedule?>(7) { null }) }
    val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
    LaunchedEffect(Unit) {
        isLoading = true
        coroutineScope.launch(Dispatchers.IO) {
            val template = AAFunAccessPoint().weekly()
            weekly = template;
        }
        isLoading = false
    }
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        TabRow(selectedTabIndex = tabIndex.value) {
            tabs.forEachIndexed { index, title ->
                Tab(text = { Text(title) }, selected = tabIndex.value == index, onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                })
            }
        }
        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxSize()
        ) { page ->
            if (isLoading) {
                CircularProgressIndicator()
            }
            WeeklyPageContent(weekDay = weekly[page]?.items)

        }
    }
}

@Composable
fun WeeklyPageContent(weekDay: List<DailySchedule.Item>?) {
    val lazyGridState = rememberLazyGridState()
    if (weekDay == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = lazyGridState,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weekDay.size) { index ->
                val item = weekDay[index]
//                Log.i("weekly->>>", item.toString())
                AnimeCard(
                    anime = SubjectSearch.Subject(
                        id = item.id,
                        name = item.name,
                        nameCn = item.name_cn,
                        images = SubjectSearch.Subject.Images(
                            medium = item.images?.large?.replaceFirst(
                                "http://",
                                "https://"
                            )
                        ),
                    )
                )

            }

        }
    }

}

