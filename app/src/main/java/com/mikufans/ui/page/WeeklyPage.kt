import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikufans.api.MetaService
import com.mikufans.ui.component.MediaCard
import com.mikufans.ui.nav.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.anime.entity.animation.Animation
import org.anime.entity.animation.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPage(
  navController: NavController,
  activity: ComponentActivity,
  baseHorizontalPadding: Dp,
) {
  val tabs = arrayOf("一", "二", "三", "四", "五", "六", "日")
  val coroutineScope = rememberCoroutineScope()
  var isLoading by rememberSaveable { mutableStateOf(false) }
  val pagerState = rememberPagerState(pageCount = { tabs.size })
  var weekly by rememberSaveable { mutableStateOf(List<Schedule?>(7) { null }) }
  val tabIndex = remember { derivedStateOf { pagerState.currentPage } }
  val todayIndex = remember {
    val calendar = java.util.Calendar.getInstance()
    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
    (dayOfWeek + 5) % 7
  }
  val metaService = MetaService.bangumi
  BackHandler { activity.moveTaskToBack(true) }
  LaunchedEffect(Unit) {
    isLoading = true
    try {
      coroutineScope.launch(Dispatchers.IO) {
        val fetchWeeklyUpdate = metaService.fetchWeeklyUpdateSync()
        weekly = fetchWeeklyUpdate
      }
    } catch (e: Exception) {
      Toast.makeText(
        navController.context, "错误:${e.message}", Toast.LENGTH_SHORT
      ).show()
    } finally {
      isLoading = false
    }
  }
  LaunchedEffect(todayIndex) {
    coroutineScope.launch {
      pagerState.animateScrollToPage(todayIndex)
    }
  }
  Scaffold(
    modifier = Modifier.padding(horizontal = baseHorizontalPadding),
    topBar = { TopAppBar(title = { Text("周更表") }) }) { innerPadding ->
    Column(
      modifier = Modifier.padding(innerPadding)
    ) {
      PrimaryTabRow(
        selectedTabIndex = tabIndex.value
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            interactionSource = null,
            text = { Text(title) },
            selected = tabIndex.value == index,
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = {
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
        WeeklyPageContent(weekDay = weekly[page]?.animations, navController)

      }
    }
  }
}

@Composable
fun WeeklyPageContent(weekDay: List<Animation>?, navController: NavController) {
  val lazyGridState = rememberLazyListState()
  if (weekDay == null) {
    Box(
      modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator()
    }
  } else {
    LazyColumn(
      state = lazyGridState,
      contentPadding = PaddingValues(vertical = 5.dp),
      verticalArrangement = Arrangement.spacedBy(5.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(weekDay.size) { index ->
        val item = weekDay[index]
        if (item.titleCn.isNotBlank()) {
          MediaCard(
            id = "",
            title = item.title,
            titleCn = item.titleCn,
            coverUrl = item.coverUrls[0],
            status = item.status,
            genre = item.genre,
            onTap = { _ ->
              Navigation.navigateToDetail(
                navController = navController, id = "", title = item.titleCn ?: ""
              )
            })
        }
      }
    }
  }
}

