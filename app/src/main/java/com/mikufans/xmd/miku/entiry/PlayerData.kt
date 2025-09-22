package com.mikufans.xmd.miku.entiry

data class PlayerData(
  var flag: String? = null,
  var encrypt: Int? = null,
  var trysee: Int? = null,
  var points: Int? = null,
  var link: String? = null,
  var link_next: String? = null,
  var link_pre: String? = null,
  var url: String? = null,
  var url_next: String? = null,
  var from: String? = null,
  var server: String? = null,
  var note: String? = null,
  var id: String? = null,
  var sid: Int? = null,
  var nid: Int? = null
) : java.io.Serializable
