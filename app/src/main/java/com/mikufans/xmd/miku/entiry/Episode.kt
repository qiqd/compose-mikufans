package com.mikufans.xmd.miku.entiry

data class Episode(
    var id: String? = null,
    var videoId: String? = null,
    var title: String? = null,
    var episodeNumber: String? = null,
    var playUrl: String? = null,
    var releaseTime: String? = null,
    var duration: Long? = null
) {
    /**
     * 无参构造函数供 FastJSON 使用
     */
    constructor() : this(null, null, null, null, null, null, null)
}
