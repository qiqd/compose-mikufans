package com.mikufans.xmd.miku.entiry

data class AnimeDetail(
  /**
   * 动漫信息
   */
  var anime: Anime? = null,
  /**
   * 动漫播放路线
   */
  var sources: List<Source>? = null,
) : java.io.Serializable {
  /**
   * 无参构造函数供 FastJSON 使用
   */
  constructor() : this(null, null)
}
