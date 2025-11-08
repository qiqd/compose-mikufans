package com.mikufans.xmd.miku.service.impl

import com.alibaba.fastjson.JSON
import org.junit.Test


class AAfunTest {
  val aafun = AAfun()

  @Test
  fun fetchPlayInfo() {
    println(JSON.toJSONString(aafun.fetchPlayInfo("/f/p6CCCS-2-1.html")))
  }

  @Test
  fun fetchDetail() {
    println(JSON.toJSONString(aafun.fetchDetail("/feng-n/hxCCCS.html")))
  }

  @Test
  fun fetchSearch() {
    val fetchSearch = aafun.fetchSearch("未来日记", 1, 10)
    println(JSON.toJSONString(fetchSearch))
  }

}