package com.mikufans.xmd.miku.service.impl

import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.miku.entiry.Episode
import com.mikufans.xmd.miku.entiry.PlayInfo
import com.mikufans.xmd.miku.entiry.Schedule
import com.mikufans.xmd.miku.entiry.Source
import com.mikufans.xmd.miku.service.HtmlParser
import com.mikufans.xmd.util.HttpUtil
import com.mikufans.xmd.util.ValidateUtil
import org.jsoup.Jsoup
import java.io.Serializable

class Omofun() : HtmlParser, Serializable {
  override val name: String = "Omofun"
  override val logoUrl: String =
    "https://omofun.in/upload/mxprocms/20220402-1/9bc9db05cafd0fdec0a1f9cec2c1ce7a.png"
  override val baseUrl: String = "https://omofun.in"
  override fun getSearchResult(
    keyword: String?,
    page: Int?,
    size: Int?
  ): MutableList<Anime> {
//    https://omofun.in/vod/search.html?wd=JOJO
    val searchUrl = "/vod/search.html?wd=$keyword"
    val request = HttpUtil.getRequest(baseUrl + searchUrl)
    val response = HttpUtil.getClient().newCall(request).execute()
    val html = ValidateUtil.validateResponse(response)
    val document = Jsoup.parse(html)
    val animeList = mutableListOf<Anime>()
    val animeItem = document.select("div.module-card-item")
    animeItem.forEach {
      val country = it.select("div.module-card-item-class").text()
      val img = it.select("img.lazy")
      val coverUrl = img.attr("data-original")
      val title = img.attr("alt")
      val id = it.select("a.module-card-item-poster").attr("href")
      val status = it.select("div.module-item-note").text()
      val typeAndDescription = it.select("div.module-info-item-content")
      val type = typeAndDescription[0].text()
      val description = typeAndDescription[1].text()
      val anime = Anime(
        id = id,
        name = title,
        nameCn = title,
        description = description,
        type = type,
        status = status,
        country = country,
        cover = coverUrl
      )
      animeList.add(anime)
    }
    return animeList
  }

  override fun getAnimeDetail(videoId: String?): AnimeDetail? {
    val request = HttpUtil.getRequest(baseUrl + videoId)
    val response = HttpUtil.getClient().newCall(request).execute()
    val html = ValidateUtil.validateResponse(response)
    val document = Jsoup.parse(html)
    val title = document.select("div.module-info-heading h1").text()
    document.select("div.module-info-tag-link a").joinToString(",") { it.text() }
    val defaultCover = document.select("img.ls-is-cached").attr("src")
    val infoBox = document.select("div.module-info-item")
    val director =
      infoBox[1].select("div.module-info-item-content a").joinToString(",") { it.text() }
    val actor = infoBox[2].select("div.module-info-item-content a").joinToString(",") { it.text() }
    val updateTime =
      infoBox[3].select("div.module-info-item-content a").joinToString(",") { it.text() }
    val episodes = document.select("a.module-play-list-link").mapIndexed { index, it ->
      val episodeId = it.attr("href")
      Episode(
        id = episodeId
      )
    }.toList()
    val anime = Anime(
      id = videoId,
      name = title,
      cover = defaultCover,
      nameCn = title,
      description = document.select("div.module-info-desc p").text(),
      director = director,
      actor = actor,
      type = document.select("div.module-info-tag-link a").joinToString(",") { it.text() },
      updateTime = updateTime,
      totalEpisodes = episodes.size,
    )
    return AnimeDetail(
      anime = anime,
      sources = listOf(Source(episodes = episodes)),
    )
  }

  override fun getPlayInfo(episodeId: String?): PlayInfo? {
    TODO("Not yet implemented")
  }

  override fun getRecommendations(html: String?): String? {
    TODO("Not yet implemented")

  }

  override fun weeklySchedule(): List<Schedule> {
    TODO("Not yet implemented")
  }


}