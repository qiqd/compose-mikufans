package com.mikufans.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mikufans.xmd.miku.entiry.Anime

@Composable
fun Subscribe() {
    val anime = Anime();
    anime.coverUrl = "https://img.pan.kg/images/363957_pgptl.webp"
    anime.title = "夏日口袋"
    val lazyGridState = rememberLazyGridState()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = lazyGridState,
        modifier = Modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(20, key = { it }) { index ->
//            AnimeCard(anime)
        }
    }
}
