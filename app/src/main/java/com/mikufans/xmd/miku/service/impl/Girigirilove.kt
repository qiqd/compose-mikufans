package com.mikufans.xmd.miku.service.impl

import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.miku.entiry.PlayInfo
import com.mikufans.xmd.miku.entiry.Schedule
import com.mikufans.xmd.miku.service.CommonTemplate
import com.mikufans.xmd.util.HttpUtil
import com.mikufans.xmd.util.ValidateUtil
import org.jsoup.Jsoup
import java.io.Serializable


class Girigirilove : CommonTemplate(), Serializable {
  override val name: String = "Girigiri爱动漫"
  override val logoUrl: String =
    "https://bgm.girigirilove.com/upload/site/20251010-1/b84e444374bcec3a20419e29e1070e1b.png"
  override val baseUrl: String = "https://bgm.girigirilove.com"

  fun schedule(): List<Schedule> {
    return super.weeklySchedule()
  }

  override fun getSearchResult(keyword: String?, page: Int?, size: Int?): MutableList<Anime> {
    val searchUrl = "/search/-------------/?wd=$keyword"
    val client = HttpUtil.getClient()
    val request = HttpUtil.getRequest(baseUrl + searchUrl)
    val animeList = mutableListOf<Anime>()
    val response = client.newCall(request).execute()
    val html = ValidateUtil.validateResponse(response)
    if (html == null) {
      return animeList
    }
    val document = Jsoup.parse(html)
    val elements = document.select("div.search-list")
    for (element in elements) {
      val href = element.select("div.detail-info a").attr("href")
      val cover = element.select("img.gen-movie-img").attr("data-src")
      val status = element.select("span.public-list-prb").text()
      val title = element.select("img.gen-movie-img").attr("alt")
      val type = element.select("div.thumb-else a").joinToString("•") { it.text() }
      val director = element.select("div.thumb-director a:not(:first-child)")
        .joinToString("•") { it.text() }
      val actor = element.select("div.thumb-actor a:not(:first-child)")
        .joinToString("•") { it.text() }
      val description = element.select("span.cor5.thumb-blurb").text()
        .replace(super.blankReg, "")
      val anime = Anime().apply {
        id = href
        nameCn = title
        this.description = description
        this.director = director
        this.actor = actor
        this.type = type
        this.status = status
        coverUrl = "${super.baseUrl}$cover"
      }
      animeList.add(anime)
    }
    return animeList
  }


  @Throws(Exception::class)
  fun animeDetail(videoId: String?): AnimeDetail? {
    return super.getAnimeDetail(videoId)
  }

  @Throws(Exception::class)
  fun playInfo(episodeId: String?): PlayInfo? {
    return super.getPlayInfo(episodeId)
  }
}