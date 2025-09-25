package com.mikufans.xmd.miku.entiry


data class Anime(
  var id: String? = null,
  var title: String? = null,
  var description: String? = null,
  var director: String? = null,
  var actor: String? = null,
  var type: String? = null,
  var year: Int? = null,
  var rating: Double? = null,
  var status: String? = null,
  var updateTime: String? = null,
) : java.io.Serializable {
  var coverUrl: String? = null
    get() = field?.takeIf { it.startsWith("https://", ignoreCase = true) }
      ?: field?.replaceFirst("http://", "https://", ignoreCase = true) ?: ""
}
