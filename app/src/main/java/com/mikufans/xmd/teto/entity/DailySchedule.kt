package com.mikufans.xmd.teto.entity

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 每日放送实体类
 */
data class DailySchedule(
  /**
   * 星期信息
   */
  var weekday: Weekday? = null,

  /**
   * 条目列表
   */
  var items: List<Item>? = null
) : java.io.Serializable {

  /**
   * 星期信息实体类
   */

  data class Weekday(
    /**
     * 英文星期名称
     */
    var en: String? = null,

    /**
     * 中文星期名称
     */
    var cn: String? = null,

    /**
     * 日文星期名称
     */
    var ja: String? = null,

    /**
     * 星期ID (1-7)
     */
    var id: Int? = null
  ) : java.io.Serializable

  /**
   * 条目信息实体类
   */
  data class Item(
    /**
     * 条目ID
     */
    var id: Int? = null,

    /**
     * 条目URL
     */
    var url: String? = null,

    /**
     * 条目类型
     */
    var type: Int? = null,

    /**
     * 条目名称
     */
    var name: String? = null,

    /**
     * 条目中文名称
     */
    var name_cn: String? = null,

    /**
     * 条目简介
     */
    var summary: String? = null,

    /**
     * 放送日期
     */
    @JsonProperty("ari_date")
    var air_date: String? = null,

    /**
     * 放送星期
     */
    @JsonProperty("air_weekday")
    var air_weekday: Int? = null,

    /**
     * 评分信息
     */
    var rating: Rating? = null,

    /**
     * 排名
     */
    var rank: Int? = null,

    /**
     * 图片信息
     */
    var images: Images? = null
  ) : java.io.Serializable {

    /**
     * 评分信息实体类
     */

    data class Rating(
      /**
       * 平均分
       */
      var score: Double? = null
    ) : java.io.Serializable

    /**
     * 图片信息实体类
     */

    data class Images(
      /**
       * 大图链接
       */
      var large: String? = null,

      /**
       * 通用尺寸图片链接
       */
      var common: String? = null,

      /**
       * 中等尺寸图片链接
       */
      var medium: String? = null,

      /**
       * 小图链接
       */
      var small: String? = null,

      /**
       * 网格图链接
       */
      var grid: String? = null
    ) : java.io.Serializable
  }
}
