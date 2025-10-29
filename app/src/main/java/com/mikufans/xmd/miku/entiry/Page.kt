package com.mikufans.xmd.miku.entiry


class Page<T> : java.io.Serializable {
  val pageNum: Int? = null
  val pageSize: Long? = null
  val totalPage: Long? = null
  val total: Long? = null
  val data: MutableList<T?>? = null
}
