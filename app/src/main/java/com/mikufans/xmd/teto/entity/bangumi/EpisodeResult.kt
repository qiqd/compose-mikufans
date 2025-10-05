package com.mikufans.xmd.teto.entity.bangumi

import java.io.Serializable

/**
 * 剧集搜索结果实体类
 */
data class EpisodeResult(
  /**
   * 剧集列表
   */
  var data: List<Episode>? = null,

  /**
   * 总剧集数
   */
  var total: Int? = null,

  /**
   * 每页剧集数
   */
  var limit: Int? = null,

  /**
   * 偏移量
   */
  var offset: Int? = null
) : Serializable {

  /**
   * 剧集信息实体类
   */
  data class Episode(
    /**
     * 放送日期
     */
    var airdate: String? = null,

    /**
     * 剧集名称
     */
    var name: String? = null,

    /**
     * 剧集中文名称
     */
    var nameCn: String? = null,

    /**
     * 时长
     */
    var duration: String? = null,

    /**
     * 简介
     */
    var desc: String? = null,

    /**
     * 剧集内的集数，从1开始
     */
    var ep: Int? = null,

    /**
     * 同类条目的排序和集数
     */
    var sort: Double? = null,

    /**
     * 章节ID
     */
    var id: Int? = null,

    /**
     * 条目ID
     */
    var subject_id: Int? = null,

    /**
     * 回复数量
     */
    var comment: Int? = null,

    /**
     * 章节类型
     */
    var type: Int? = null,

    /**
     * 音乐曲目的碟片数
     */
    var disc: Int? = null,

    /**
     * 服务器解析的时长，单位秒
     */
    var duration_seconds: Int? = null
  ) : Serializable
}
