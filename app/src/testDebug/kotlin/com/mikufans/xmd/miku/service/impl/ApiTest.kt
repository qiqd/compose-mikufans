package com.mikufans.xmd.miku.service.impl

import org.animation.util.AnimationApi
import org.junit.Test

class ApiTest {

  @Test
  fun test() {
    val api = AnimationApi.HTML_PARSER_MAP
    api.values.forEach {
      val start = System.currentTimeMillis()
      val result = it.fetchSearchSync("JOJO的奇妙冒险", 1, 10)
      println(result)
      val end = System.currentTimeMillis()
      println("${it::class.java.simpleName} 耗时 ${end - start}ms")
    }
  }
}