package com.mikufans.xmd.teto.entity

import java.io.Serializable

/**
 * 条目搜索结果实体类
 */
data class SubjectSearch(
  /**
   * 条目列表
   */
  var data: List<Subject>? = null,

  /**
   * 总条目数
   */
  var total: Int? = null,

  /**
   * 每页条目数
   */
  var limit: Int? = null,

  /**
   * 每页偏移量
   */
  var offset: Int? = null
) : java.io.Serializable {

  /**
   * 条目信息实体类
   */
  data class Subject(
    /**
     * 条目ID
     */
    var id: Int? = null,

    /**
     * 条目名称
     */
    var name: String? = null,

    /**
     * 条目中文名称
     */
    var nameCn: String? = null,

    /**
     * 条目简介
     */
    var summary: String? = null,

    /**
     * 发行日期
     */
    var date: String? = null,

    /**
     * 条目图片信息
     */
    var images: Images? = null,

    /**
     * 条目元标签信息
     */
    var metaTags: List<String>? = null,

    /**
     * 条目信息框
     */
    var infobox: List<InfoBox>? = null,

    /**
     * 集数
     */
    var eps: Int? = null,

    /**
     * 平台信息（如TV、剧场版等）
     */
    var platform: String? = null,

    /**
     * 评分信息
     */
    var rating: Rating? = null,

    /**
     * 收藏信息
     */
    var collection: Collection? = null
  ) : java.io.Serializable {

    /**
     * 图片信息实体类
     */
    data class Images(
      /**
       * 小图链接
       */
      var small: String? = null,

      /**
       * 网格图链接
       */
      var grid: String? = null,

      /**
       * 大图链接
       */
      var large: String? = null,

      /**
       * 中等尺寸图片链接
       */
      var medium: String? = null,

      /**
       * 通用尺寸图片链接
       */
      var common: String? = null
    ) : Serializable

    /**
     * 信息框实体类
     */
    data class InfoBox(
      /**
       * 信息键名
       */
      var key: String? = null,

      /**
       * 信息值
       */
      var value: Any? = null
    ) : java.io.Serializable

    /**
     * 评分信息实体类
     */
    data class Rating(
      /**
       * 排名
       */
      var rank: Int? = null,

      /**
       * 总评分人数
       */
      var total: Int? = null,

      /**
       * 各分数段评分人数
       */
      var count: ScoreCount? = null,

      /**
       * 平均分
       */
      var score: Double? = null
    ) : java.io.Serializable {

      /**
       * 各分数段评分人数实体类
       */
      data class ScoreCount(
        /**
         * 1分人数
         */
        var one: Int? = null,

        /**
         * 2分人数
         */
        var two: Int? = null,

        /**
         * 3分人数
         */
        var three: Int? = null,

        /**
         * 4分人数
         */
        var four: Int? = null,

        /**
         * 5分人数
         */
        var five: Int? = null,

        /**
         * 6分人数
         */
        var six: Int? = null,

        /**
         * 7分人数
         */
        var seven: Int? = null,

        /**
         * 8分人数
         */
        var eight: Int? = null,

        /**
         * 9分人数
         */
        var nine: Int? = null,

        /**
         * 10分人数
         */
        var ten: Int? = null
      ) : java.io.Serializable
    }

    /**
     * 收藏信息实体类
     */
    data class Collection(
      /**
       * 搁置人数
       */
      var onHold: Int? = null,

      /**
       * 抛弃人数
       */
      var dropped: Int? = null,

      /**
       * 想看人数
       */
      var wish: Int? = null,

      /**
       * 已看人数
       */
      var collect: Int? = null,

      /**
       * 在看人数
       */
      var doing: Int? = null
    ) : java.io.Serializable
  }
}
