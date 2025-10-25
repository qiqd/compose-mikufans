package com.mikufans.xmd.miku.entiry

import java.io.Serializable
import java.time.LocalDateTime

data class User(
  val name: String? = null,
  val email: String? = null,
  val password: String? = null,
  val avatar: String? = null,
  val updateTime: LocalDateTime? = null,
) : Serializable