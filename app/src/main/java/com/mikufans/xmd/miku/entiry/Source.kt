package com.mikufans.xmd.miku.entiry

/**
 * 番剧播放路线
 */


data class Source(
  var id: String? = null,
  /**
   * 路线
   */
  var name: String? = null,
  /**
   * 对应路线下的剧集
   */
  var episodes: List<Episode>? = null
) : java.io.Serializable {
  /**
   * 无参构造函数供 FastJSON 使用
   */
  constructor() : this(null, null, null)
}
