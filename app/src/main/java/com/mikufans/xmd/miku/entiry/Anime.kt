package com.mikufans.xmd.miku.entiry

data class Anime(
    var id: String? = null,
    var title: String? = null,
    var description: String? = null,
    var coverUrl: String? = null,
    var director: String? = null,
    var actor: String? = null,
    var type: String? = null,
    var year: Int? = null,
    var rating: Double? = null,
    /**
     * 更新状态
     */
    var status: String? = null,
    /**
     * 更新时间
     */
    var updateTime: String? = null
) {
    /**
     * 无参构造函数供 FastJSON 使用
     */
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null)
}
