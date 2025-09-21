package com.mikufans.xmd.miku.entiry

data class PlayInfo(
    var id: Int? = null,
    var episodeId: String? = null,
    var preEpisodeUrl: String? = null,
    var currentEpisodeUrl: String? = null,
    var nextEpisodeUrl: String? = null,
    var resolution: String? = null
) {
    /**
     * 无参构造函数供 FastJSON 使用
     */
    constructor() : this(null, null, null, null, null, null)
}
