package com.mikufans.xmd.miku.service

import com.mikufans.xmd.miku.entiry.Anime
import com.mikufans.xmd.miku.entiry.AnimeDetail
import com.mikufans.xmd.miku.entiry.PlayInfo
import com.mikufans.xmd.miku.entiry.Schedule
import java.io.Serializable


/**
 * HTML解析接口规范
 */
interface HtmlParser : Serializable {
  /**
   * 名称
   */
  val name: String

  /**
   * 网站logo地址
   */
  val logoUrl: String

  /**
   * 网站地址
   */
  val baseUrl: String

  /**
   * 解析视频搜索结果
   *
   * @param keyword 搜索关键词
   * @return List<Anime>
   * @throws Exception 解析异常
  </Anime> */
  @Throws(Exception::class)
  fun getSearchResult(keyword: String?, page: Int?, size: Int?): MutableList<Anime>

  /**
   * 解析视频详情信息
   *
   * @param videoId 视频ID
   * @return DetailPage
   * @throws Exception 解析异常
   */
  @Throws(Exception::class)
  fun getAnimeDetail(videoId: String?): AnimeDetail?

  /**
   * 解析播放信息
   *
   * @param episodeId 剧集id
   * @return PlayInfo
   * @throws Exception 解析异常
   */
  @Throws(Exception::class)
  fun getPlayInfo(episodeId: String?): PlayInfo?

  /**
   * 解析推荐视频
   *
   * @param html HTML内容
   * @return 推荐视频列表
   * @throws Exception 解析异常
   */
  @Throws(Exception::class)
  fun getRecommendations(html: String?): String?

  @Throws(Exception::class)
  fun weeklySchedule(): List<Schedule>
}
