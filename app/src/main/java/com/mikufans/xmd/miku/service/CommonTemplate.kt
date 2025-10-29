package com.mikufans.xmd.miku.service

import android.util.Log
import com.alibaba.fastjson.JSON
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.miku.entiry.Episode
import com.mikufans.xmd.miku.entiry.PlayInfo
import com.mikufans.xmd.miku.entiry.PlayerData
import com.mikufans.xmd.miku.entiry.Schedule
import com.mikufans.xmd.miku.entiry.Source
import com.mikufans.xmd.util.HttpUtil
import com.mikufans.xmd.util.ValidateUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.Serializable
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern

open class CommonTemplate : HtmlParser, Serializable {
  val blankReg = "[\\s\u3000]+"

  override val name: String = "通用模板"
  override val logoUrl: String = ""
  override val baseUrl: String = ""

  override fun getSearchResult(keyword: String?, page: Int?, size: Int?): MutableList<Anime> {
    val client = HttpUtil.getClient()
    val request = HttpUtil.getRequest("$baseUrl$keyword")
    val animeList = ArrayList<Anime>()
    val response = client.newCall(request).execute()
    val html = ValidateUtil.validateResponse(response)
    if (html == null) return animeList

    val document = Jsoup.parse(html)
    val elements = document.select("div.public-list-box.search-box")

    for (element in elements) {
      val href = element.select("a.public-list-exp").attr("href")
      val cover = element.select("img.gen-movie-img").attr("data-src")
      val status = element.select("span.public-list-prb").text()
      val title = element.select("div.thumb-txt.cor4.hide").text()
      val type = element.select("div.thumb-else a").joinToString("•", transform = Element::text)
      val director = element.select("div.thumb-director a:not(:first-child)")
        .joinToString("•", transform = Element::text)
      val actor = element.select("div.thumb-actor a:not(:first-child)")
        .joinToString("•", transform = Element::text)
      val description =
        element.select("span.cor5.thumb-blurb").text().replace(blankReg.toRegex(), "")

      val anime = Anime().apply {
        id = href
        name = title
        coverUrl = cover
      }
      anime.description = description
      anime.director = director
      anime.actor = actor
      anime.type = type
      anime.status = status
      animeList.add(anime)
    }
    return animeList
  }

  override fun getAnimeDetail(videoId: String?): AnimeDetail? {
    val fullUrl = "$baseUrl$videoId"
    val client = HttpUtil.getClient()
    val request = HttpUtil.getRequest(fullUrl)
    val animeDetail = AnimeDetail()
    val response = client.newCall(request).execute()
    val s = ValidateUtil.validateResponse(response)
    val document = Jsoup.parse(s)
    val detailDiv = document.select("div.vod-detail.style-detail")
    val coverImg = detailDiv.select("img.lazy.lazy1.mask-1").attr("data-src")
    val title = detailDiv.select("h3.slide-info-name").text()
    val part1 =
      detailDiv.select("div.slide-info span.slide-info-remarks").firstOrNull()?.text() ?: ""
    val part2 = detailDiv.select("div.slide-info a").joinToString("•", transform = Element::text)
    val status = part2 + part1
    val director =
      detailDiv.select("div.slide-info")[1].select("a").joinToString("•", transform = Element::text)
    val actor =
      detailDiv.select("div.slide-info")[2].select("a").joinToString("•", transform = Element::text)
    val type =
      detailDiv.select("a.deployment.none.cor5 span").joinToString(transform = Element::text)
    val description = detailDiv.select("div#height_limit").text().replace(blankReg.toRegex(), "")
    val listBoxDiv = document.select("div.anthology-list-box.none")
    val sources = listBoxDiv.map { element ->
      Source().apply {
        episodes = element.select("a").map { item ->
          Episode(
            id = item.attr("href"),
            title = item.text(),
            playUrl = item.attr("href"),
            //todo
          )
        }
      }
    }
    val anime = Anime().apply {
      id = videoId
      name = title
      coverUrl = coverImg
    }
    anime.director = director
    anime.actor = actor
    anime.type = type
    anime.status = status
    anime.description = description
    animeDetail.anime = anime
    animeDetail.sources = sources
    animeDetail.anime = anime
    return animeDetail
  }

  override fun getPlayInfo(episodeId: String?): PlayInfo? {
    val fullUrl = "$baseUrl$episodeId"
    val client = HttpUtil.getClient()
    val request = HttpUtil.getRequest(fullUrl)
    val response = client.newCall(request).execute()
    val html = ValidateUtil.validateResponse(response)
    if (html == null) return null
    val doc = Jsoup.parse(html)
    val scriptElements = doc.getElementsByTag("script")
    var playerData: String? = null
    for (script in scriptElements) {
      val scriptText = script.data()
      if (scriptText.contains("player_aaaa")) {
        playerData = scriptText
        break
      }
    }
    val jsonStr = extractJsonFromScript(playerData)
    if (jsonStr == null) return null
    val player = JSON.parseObject(jsonStr, PlayerData::class.java)
    val playInfo = PlayInfo().apply {
      currentEpisodeUrl = decodeUrl(player.url!!)
      nextEpisodeUrl = decodeUrl(player.url_next!!)
    }
    return playInfo
  }

  override fun getRecommendations(html: String?): String? {
    return null
  }

  override fun weeklySchedule(): List<Schedule> {
    val client = HttpUtil.getClient()
    val request = HttpUtil.getRequest(baseUrl)

    val response = client.newCall(request).execute()
    val s = ValidateUtil.validateResponse(response)
    if (s == null) return emptyList()
    val document = Jsoup.parse(s)
    val schedules = ArrayList<Schedule>()
    for (i in 0 until 7) {
      val schedule = Schedule().apply {
        day = i + 1
        anime = ArrayList()
      }
      val week = "div#week-module-" + (i + 1)
      val listBox = document.select("$week div.public-list-box")
      for (box in listBox) {
        val temp = box.select("a.public-list-exp")
        val href = temp.attr("href")
        val title = temp.attr("name")
        val coverImg = box.select("img.lazy.gen-movie-img").attr("data-src")
        val status = box.select("div.public-list-subtitle").text().replace(blankReg.toRegex(), "")

        val anime = Anime().apply {
          id = href
          name = title
          coverUrl = if (coverImg.contains("http")) coverImg else "$baseUrl$coverImg"
        }
        anime.status = status
        schedule.anime?.add(anime)
      }
      schedules.add(schedule)
    }
    return schedules
  }

  private fun decodeUrl(encodedUrl: String): String {
    if (encodedUrl.isEmpty()) return encodedUrl

    var decodedUrl: String
    try {
      // 首先尝试Base64解码
      try {
        val decodedBytes = Base64.getDecoder().decode(encodedUrl)
        decodedUrl = String(decodedBytes, StandardCharsets.UTF_8)
      } catch (base64Exception: Exception) {
        Log.d("decodeUrl", "Base64 decoding failed: $base64Exception")
        // 如果Base64解码失败，则尝试URL解码
        try {
          decodedUrl = URLDecoder.decode(encodedUrl, "UTF-8")
        } catch (urlException: Exception) {
          Log.d("decodeUrl", "URL decoding failed: $urlException")
          decodedUrl = encodedUrl
        }
      }

      // 再次尝试URL解码（以防是双重编码）
      try {
        val doubleDecodedUrl = URLDecoder.decode(decodedUrl, "UTF-8")
        if (!doubleDecodedUrl.equals(decodedUrl)) {
          decodedUrl = doubleDecodedUrl
        }
      } catch (e: Exception) {
        // 保持第一次解码的结果
        Log.d("decodeUrl", "Double URL decoding failed: $e")
      }
      return decodedUrl
    } catch (e: Exception) {
      Log.d("decodeUrl", "Decoding failed: $e")
      return encodedUrl // 解码失败时返回原始URL
    }
  }

  private fun extractJsonFromScript(scriptText: String?): String? {
    if (scriptText == null) return null

    val pattern = Pattern.compile("var\\s+player_aaaa\\s*=\\s*(\\{.*?\\})\\s*;")
    val matcher = pattern.matcher(scriptText)
    if (matcher.find()) {
      return matcher.group(1)
    }
    // 如果上面的模式没匹配到，尝试另一种模式
    val pattern2 = Pattern.compile("var\\s+player_aaaa\\s*=\\s*(\\{.*?\\})\\s*$")
    val matcher2 = pattern2.matcher(scriptText)
    if (matcher2.find()) {
      return matcher2.group(1)
    }
    return null
  }
}


