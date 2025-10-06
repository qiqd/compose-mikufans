package com.mikufans.xmd.miku.entiry


data class Anime(
  var id: String? = null,
  var subId: Int? = null,
  var name: String? = null,
  var nameCn: String? = null,
  var description: String? = null,
  var director: String? = null,
  var actor: String? = null,
  var type: String? = null,
  var date: String? = null,
  var rating: Double? = null,
  var status: String? = null,
  var updateTime: String? = null,
  var totalEpisodes: Int? = null,
  var platform: String? = null
) : java.io.Serializable {
  var coverUrl: String? = null
    get() = field?.takeIf { it.startsWith("https://", ignoreCase = true) }
      ?: field?.replaceFirst("http://", "https://", ignoreCase = true) ?: ""
}
