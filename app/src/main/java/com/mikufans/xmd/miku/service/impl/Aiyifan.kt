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

class Aiyifan : HtmlParser, Serializable {
  override val name: String = "爱壹番"
  override val logoUrl: String = "https://www.aiyifan.sbs/static/images/logo.jpg"
  override val baseUrl: String = "https://www.aiyifan.sbs"
  var currentPrefix = ""
  private val sourcePrefix =
    mapOf(
      "2" to "",
      "1" to "https://s3.bfengbf.com",
      "3" to "https://sd7.taopianplay1.com:43333",
      "4" to "https://sd7.taopianplay1.com:43333",
    )

  override fun getSearchResult(
    keyword: String?,
    page: Int?,
    size: Int?
  ): MutableList<Anime> {
    ///ayf.sbssearch/-------------.html?wd=JOJO
    val searchUrl = "/ayf.sbssearch/-------------.html?wd=$keyword"
    val request = HttpUtil.getRequest(baseUrl + searchUrl)
    val response = HttpUtil.getClient().newCall(request).execute()
    val html = ValidateUtil.validateResponse(response)
    val document = Jsoup.parse(html)
    val animeItems = document.select("div.details-info-min")
    val animeList = mutableListOf<Anime>()
    animeItems.forEach { it ->
      val imageElement = it.select("a.video-pic")
      val id = imageElement.attr("href")
      val title = imageElement.attr("title")
      val cover = imageElement.attr("data-original")
      val li = it.select("ul.info li")
      val status = li[2].text()
      val type = li[3].select("a").text()
      val actor = li[4].select("a").joinToString(",") { i -> i.text() }
      val director = li[5].select("a").joinToString(",") { i -> i.text() }
      val country = li[6].text()
      val year = li[8].text()
      val updateTime = li[7].text()
      val description = li[10].text()
      val anime = Anime(
        id = id,
        description = description,
        name = title,
        cover = cover,
        status = status,
        type = type,
        actor = actor,
        director = director,
        country = country,
        ariDate = year,
        updateTime = updateTime
      )
      animeList.add(anime)
    }
    return animeList
  }

  override fun getAnimeDetail(videoId: String?): AnimeDetail? {
    val fullUrl = baseUrl + videoId
    val request = HttpUtil.getRequest(fullUrl)
    val response = HttpUtil.getClient().newCall(request).execute()
    val html = ValidateUtil.validateResponse(response)
    val document = Jsoup.parse(html)
    val playSource = document.select("div.playlist-mobile ul.clearfix")
    val sources = playSource.map {
      val episode = it.select("li a").mapIndexed { index, element ->
        Episode(id = element.attr("href"), playUrl = element.attr("href"))
      }.toList()
      Source(episodes = episode)
    }.toList()
    return AnimeDetail(
      sources = sources,
    )
  }

  override fun getPlayInfo(episodeId: String?): PlayInfo? {
    val fullUrl = baseUrl + episodeId
    val request = HttpUtil.getRequest(fullUrl)
    val response = HttpUtil.getClient().newCall(request).execute()
    val html = ValidateUtil.validateResponse(response)
    val document = Jsoup.parse(html)
    val jsScripts = document.select("script[type='text/javascript']")
    val targetScript = jsScripts.find { it.data().contains("player_aaaa") }?.data()
    val urlRegex = """"url"\s*:\s*"([^"]+)"""".toRegex()
    val matchResult = urlRegex.find(targetScript ?: "")
    val url = matchResult?.groups?.get(1)?.value?.replace("\\/", "/")
    val lastIndexOf = episodeId?.lastIndexOf("-")!!
    val preFixIndex = episodeId.substring(lastIndexOf - 1, lastIndexOf)
    return PlayInfo(
      currentEpisodeUrl = sourcePrefix[preFixIndex] + url?.split("/")?.drop(3)
        ?.joinToString("/", prefix = "/")
    )
  }

  override fun getRecommendations(html: String?): String? {
    TODO("Not yet implemented")
  }

  override fun weeklySchedule(): List<Schedule> {
    TODO("Not yet implemented")
  }
}