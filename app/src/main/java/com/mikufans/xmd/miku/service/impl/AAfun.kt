package com.mikufans.xmd.miku.service.impl

import android.util.Log
import com.alibaba.fastjson.JSON
import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.miku.entiry.Episode
import com.mikufans.xmd.miku.entiry.PlayInfo
import com.mikufans.xmd.miku.entiry.PlayerData
import com.mikufans.xmd.miku.entiry.Schedule
import com.mikufans.xmd.miku.entiry.Source
import com.mikufans.xmd.miku.service.HtmlParser
import com.mikufans.xmd.util.HttpUtil
import com.mikufans.xmd.util.ValidateUtil
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.Serializable
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AAfun : HtmlParser, Serializable {
  override val name: String = "风铃动漫"
  override val logoUrl: String = "https://p.upyun.com/demo/tmp/Hds66ovM.png"
  override val baseUrl: String = "https://www.aafun.cc"

  override fun getSearchResult(keyword: String?, page: Int?, size: Int?): MutableList<Anime> {
    val searchUrl = "/feng-s.html?wd=$keyword&submit="
    val client = HttpUtil.getClient()
    Log.i("getSearchResult-Url:", "$baseUrl$searchUrl")
    val request = HttpUtil.getRequest("$baseUrl$searchUrl")
    client.newCall(request).execute().use { response ->
      val body = ValidateUtil.validateResponse(response)
      val document = Jsoup.parse(body)
      val elements = document.select("div.hl-list-wrap")
      val li = elements.select("li.hl-list-item")
      return li.map { item ->
        val a = item.select("div.hl-item-div a")
        val status = item.select("span.hl-lc-1.remarks").text()
        Anime().apply {
          id = a.attr("href")
          nameCn = a.attr("title")
          coverUrl = a.attr("data-original")
          this.status = status
        }
      }.toMutableList()
    }
  }


  override fun getAnimeDetail(videoId: String?): AnimeDetail? {
    Log.i("getAnimeDetail-Url", "$baseUrl$videoId")
    val request = HttpUtil.getRequest("$baseUrl$videoId")
    HttpUtil.getClient().newCall(request).execute().use { response ->
      val body = ValidateUtil.validateResponse(response)
      val animeDetail = AnimeDetail()
      val document = Jsoup.parse(body)
      val div = document.select("div.hl-tabs-box")
      val sources = div.map { item ->
        Source().apply {
          episodes = item.select("li.hl-col-xs-4 a").map { a ->
            Episode().apply {
              id = a.attr("href")
              title = a.text()
              playUrl = a.attr("href")
            }
          }
        }
      }
      val anime = Anime()
      // 解析封面图片
      val picElements = document.select(".hl-dc-pic .hl-item-thumb")
      if (picElements.isNotEmpty()) {
        val picElement = picElements.first()
        anime.coverUrl = picElement?.attr("data-original")
      }
      // 解析标题
      val titleElements = document.select(".hl-dc-name")
      if (titleElements.isNotEmpty()) {
        anime.nameCn = titleElements.first()?.text()
      }
      //解析状态
      val statusElements = document.select(".hl-vod-data .hl-col-xs-12 span.hl-text-conch")
      if (statusElements.isNotEmpty()) {
        anime.status = statusElements.first()?.text()
      }
      // 解析主演
      val actorElements = document.select(".hl-vod-data .hl-col-xs-12").eq(2)
      if (actorElements.isNotEmpty()) {
        val actorLinks = actorElements.select("a")
        val actorBuilder = StringBuilder()
        for (i in 0 until actorLinks.size) {
          if (i > 0) actorBuilder.append(" ")
          actorBuilder.append(actorLinks[i].text())
        }
        anime.actor = actorBuilder.toString()
      }
      // 解析导演
      val directorElements = document.select(".hl-vod-data .hl-col-xs-12").eq(3)
      if (directorElements.isNotEmpty()) {
        val directorLinks = directorElements.select("a")
        val directorBuilder = StringBuilder()
        for (i in 0 until directorLinks.size) {
          if (i > 0) directorBuilder.append(" ")
          directorBuilder.append(directorLinks[i].text())
        }
        anime.director = directorBuilder.toString()
      }
      // 解析年份
      val yearElements = document.select(".hl-vod-data .hl-col-xs-12.hl-col-sm-4")
      if (yearElements.isNotEmpty()) {
        val yearElement = yearElements[0]
        val yearText = yearElement.text()
        val year = yearText.replace("\\D+".toRegex(), "")
        if (year.isNotEmpty()) {
          try {
            anime.ariDate = year
          } catch (e: NumberFormatException) {
            Log.e("解析年份失败: {}", yearText ?: e.message!!)
          }
        }
      }
      // 解析类型
      val typeElements = document.select(".hl-vod-data .hl-col-xs-12.hl-col-sm-4")
      if (typeElements.size > 2) {
        val typeElement = typeElements[2]
        val typeLinks = typeElement.select("a")
        val typeBuilder = StringBuilder()
        for (i in 0 until typeLinks.size) {
          if (i > 0) typeBuilder.append(" ")
          typeBuilder.append(typeLinks[i].text())
        }
        anime.type = typeBuilder.toString()
      }
      // 解析上映时间
      val releaseElements = document.select(".hl-vod-data .hl-col-xs-12.hl-col-sm-4")
      if (releaseElements.size > 4) {
        val releaseElement = releaseElements[4]
        val releaseText = releaseElement.text()
        anime.updateTime = releaseText
      }
      // 解析简介
      val descriptionElements = document.select(".hl-vod-data .hl-col-xs-12.blurb")
      if (descriptionElements.isNotEmpty()) {
        var description = descriptionElements.first()?.text() ?: ""
        if (description.startsWith("简介：")) {
          description = description.substring(3)
        }
        anime.description = description
      }
      // 解析评分
      val ratingElements = document.select(".hl-score-nums span")
      if (ratingElements.isNotEmpty()) {
        try {
          val rating = ratingElements.first()?.text()?.toDouble()
          anime.rating = rating
        } catch (e: NumberFormatException) {
          Log.e("解析评分失败: {}", ratingElements.first()?.text() ?: e.message!!)
        }
      }
      animeDetail.anime = anime
      animeDetail.sources = sources
      return animeDetail
    }
  }

  override fun getPlayInfo(episodeId: String?): PlayInfo? {
    Log.i("getPlayInfo-Url:", "$baseUrl$episodeId")
    val request = HttpUtil.getRequest("$baseUrl$episodeId")

    HttpUtil.getClient().newCall(request).execute().use { response ->
      val body = ValidateUtil.validateResponse(response)
      val document = Jsoup.parse(body)
      val script = document.select("script[type='text/javascript']")
      val playerScript = script.filter { it.data().contains("var player_aaaa") }
      val objectString = playerScript[0].data().substring(playerScript[0].data().indexOf("{"))
      val playerData = JSON.parseObject(objectString, PlayerData::class.java)
      val decodeUrl = URLDecoder.decode(playerData.url, StandardCharsets.UTF_8.name())
      val fullUrl = "$baseUrl/player/?url=$decodeUrl"
      val headers = HashMap<String, String>()
      headers["Host"] = "www.aafun.cc"
      headers["Referer"] = "https://www.aafun.cc$episodeId"
      val playerRequest = HttpUtil.getRequest(fullUrl, headers)
      HttpUtil.getClient().newCall(playerRequest).execute().use { playerResponse ->
        val playerBody = ValidateUtil.validateResponse(playerResponse)
        val parse = Jsoup.parse(playerBody)
        val element = parse.select("script").find { it.data().contains("const encryptedUrl") }
          ?: Element("no")
        val scriptContent = element.data()
        var encryptedUrl: String? = null
        var sessionKey: String? = null
        if (scriptContent.contains("const encryptedUrl")) {
          val encryptedUrlPattern = Pattern.compile("const\\s+encryptedUrl\\s*=\\s*\"([^\"]+)\"")
          val encryptedUrlMatcher = encryptedUrlPattern.matcher(scriptContent)
          if (encryptedUrlMatcher.find()) {
            encryptedUrl = encryptedUrlMatcher.group(1)
          }
        }
        if (scriptContent.contains("const sessionKey")) {
          val sessionKeyPattern = Pattern.compile("const\\s+sessionKey\\s*=\\s*\"([^\"]+)\"")
          val sessionKeyMatcher = sessionKeyPattern.matcher(scriptContent)
          if (sessionKeyMatcher.find()) {
            sessionKey = sessionKeyMatcher.group(1)
          }
        }
        val videoUrl = decryptAES(encryptedUrl!!, sessionKey!!)
        val currentUrl = videoUrl.replaceFirst("http://", "https://")
        val playInfo = PlayInfo()
        playInfo.currentEpisodeUrl = currentUrl
        return playInfo
      }
    }
  }


  override fun getRecommendations(html: String?): String? {
    return null
  }


  override fun weeklySchedule(): MutableList<Schedule> {
    Log.i("getWeeklySchedule-Url:", baseUrl)
    val request = HttpUtil.getRequest(baseUrl)
    HttpUtil.getClient().newCall(request).execute().use { response ->
      val body = ValidateUtil.validateResponse(response)
      val document = Jsoup.parse(body)
      val weekBoxItem = document.select("div.hl-rb-vod.hl-week-item")
      val weeklyDiv = weekBoxItem.select("div.row div.hl-list-wrap")
      val schedules = ArrayList<Schedule>()
      for (i in 0 until weeklyDiv.size) {
        val anime = weeklyDiv[i].select("li.hl-list-item").map { item ->
          Anime().apply {
            val a = item.select("a.hl-item-thumb ")
            id = a.attr("href")
            name = a.attr("name")
            coverUrl = a.attr("data-original")
          }
        }
        schedules.add(Schedule(i, anime.toMutableList()))
      }
      return schedules
    }
  }

  private fun decryptAES(ciphertext: String, key: String): String {
    try {
      val rawBytes = Base64.getDecoder().decode(ciphertext)
      val ivBytes = rawBytes.copyOfRange(0, 16)
      val encryptedBytes = rawBytes.copyOfRange(16, rawBytes.size)

      val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
      val keySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
      val ivSpec = IvParameterSpec(ivBytes)
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

      val plainText = cipher.doFinal(encryptedBytes)
      return String(plainText, StandardCharsets.UTF_8)
    } catch (e: Exception) {
      println("URL解密失败: ${e.message}")
      throw RuntimeException(e)
    }
  }

}