package com.mikufans.xmd.miku.entiry

/**
 * 动画实体类，用于html解析
 * 实现了Serializable接口，支持序列化操作
 */
data class Anime(
  /** 动画唯一标识符 */
  var id: String? = null,
  /** 子ID，可能用于区分同一动画的不同版本，该字段补充html解析获得 */
  var subId: Int? = null,
  /** 动画名称（原文） */
  var name: String? = null,
  /** 动画中文名称 */
  var nameCn: String? = null,
  /** 动画描述信息 */
  var description: String? = null,
  /** 导演信息 */
  var director: String? = null,
  /** 主演信息 */
  var actor: String? = null,
  /** 动画类型/分类 */
  var type: String? = null,
  /** 首播日期 */
  var ariDate: String? = null,
  /** 评分 */
  var rating: Double? = null,
  /** 动画状态（如连载中、已完结等） */
  var status: String? = null,
  /** 更新时间 */
  var updateTime: String? = null,
  /** 总集数 */
  var totalEpisodes: Int? = null,
  /** 播放平台 */
  var platform: String? = null,
  /** 国家/地区 */
  var country: String? = null,
  /** 封面图片URL */
  var cover: String? = null
) : java.io.Serializable {
  /**
   * 封面图片URL
   * 自动将http链接转换为https链接以确保安全性
   */
  var coverUrl: String? = null
    get() = field?.takeIf { it.startsWith("https://", ignoreCase = true) }
      ?: field?.replaceFirst("http://", "https://", ignoreCase = true) ?: ""
}
