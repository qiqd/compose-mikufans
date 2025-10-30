package com.mikufans.xmd.miku.service.impl

import com.alibaba.fastjson.JSON
import org.junit.Test


class AiyifanTest {
  val aiyifan = Aiyifan()

  @Test
  fun getAnimeDetail() {

    val searchResult = aiyifan.getSearchResult("JOJO的奇妙冒险", 1, 10)
    println(searchResult[0])
    //Anime(id=/ayf.sbs-vod/95251.html, subId=null, name=JOJO的奇妙冒险 星尘斗士, nameCn=null, description=详细介绍：1989年，日本。　　乔纳森·乔斯达与DIO决战后的一百年，DIO复活了。同时，乔瑟夫的孙子，空条承太郎发现自己有幽波纹（替身）能力，DIO的复活影响了没有替身抵抗能力的母亲，陷入病危情况；为了拯救命…, director=津田尚克,加藤敏幸,铃木健一,小仓宏文,副岛惠文,米田光宏,大脊户聪,藤本次朗,高村雄太,山田弘和,町谷俊辅,玉村仁,江副仁美,吉川志我津, actor=小野大辅,石冢运升,三宅健太,平川大辅,小松史法,子安武人,高木礼子, type=日韩动漫, ariDate=年代：2014, rating=null, status=状态： 已完结, updateTime=语言/字幕：日语, totalEpisodes=null, platform=null, country=国家/地区：日本, cover=https://bftuvip.com/upload/vod/20230424-18/cd6de39ee3de6e191715fa411af8f39c.jpg)
  }

  @Test
  fun getPlayInfo() {
    val animeDetail = aiyifan.getAnimeDetail("/ayf.sbs-vod/95251.html")
    println(JSON.toJSONString(animeDetail))
    // "episodes": [
    //        {
    //          "id": "/ayf.sbs-play/95251-2-1.html",
    //          "playUrl": "/ayf.sbs-play/95251-2-1.html"
    //        },
  }

  @Test
  fun getSearchResult() {
    val playInfo = aiyifan.getPlayInfo("/ayf.sbs-play/95251-1-15.html")
    println(JSON.toJSONString(playInfo))
  }

}