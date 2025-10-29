package com.mikufans.xmd.miku.entiry


data class Schedule(
  var day: Int? = null,
  var anime: MutableList<Anime>? = null
) : java.io.Serializable {
  /**
   * 无参构造函数供 FastJSON 使用
   */
  constructor() : this(null, null)
}
