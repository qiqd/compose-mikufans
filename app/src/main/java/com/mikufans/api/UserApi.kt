package com.mikufans.api

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import com.mikufans.xmd.miku.entiry.History
import com.mikufans.xmd.miku.entiry.Page
import com.mikufans.xmd.miku.entiry.Result
import com.mikufans.xmd.miku.entiry.User
import com.mikufans.xmd.util.HttpUtil
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

object UserApi {
  const val SERVICE_URL = "http://localhost:56438";
  val client: OkHttpClient = HttpUtil.getClient()

  @Throws(IOException::class)
  fun login(user: User): String {
    val requestBody = JSON.toJSONString(user).toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
      .url("$SERVICE_URL/api/user/login")
      .post(requestBody)
      .build()
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
      val result =
        JSON.parseObject(response.body?.string(), object : TypeReference<Result<String>>() {})
      if (result.code == 200) {
        return result.data
      }
    }
    return ""
  }

  @Throws(IOException::class)
  fun register(user: User): Boolean {
    val requestBody = JSON.toJSONString(user).toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
      .url("$SERVICE_URL/api/user/register")
      .post(requestBody)
      .build()
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
      val result =
        JSON.parseObject(response.body?.string(), object : TypeReference<Result<String>>() {})
      if (result.code == 200) {
        return true
      }
    }
    return false
  }

  @Throws(IOException::class)
  fun updateAccount(user: User): Boolean {
    val requestBody = JSON.toJSONString(user).toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
      .url("$SERVICE_URL/api/user/update")
      .post(requestBody)
      .build()
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
      val result =
        JSON.parseObject(response.body?.string(), object : TypeReference<Result<String>>() {})
      if (result.code == 200) {
        return true
      }
    }
    return false
  }

  @Throws(IOException::class)
  fun getHistory(): Page<History> {
    val request = Request.Builder()
      .url("$SERVICE_URL/api/history/batch")
      .get()
      .build()
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
      val result =
        JSON.parseObject(
          response.body?.string(),
          object : TypeReference<Result<Page<History>>>() {})
      if (result.code == 200) {
        return result.data
      }
    }
    return Page()
  }

  @Throws(IOException::class)
  fun updateHistory(historyList: List<History>): Boolean {
    val requestBody =
      JSON.toJSONString(historyList).toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
      .url("$SERVICE_URL/api/history/batch")
      .post(requestBody)
      .build()
    val response = client.newCall(request).execute()
    if (response.isSuccessful) {
      val result =
        JSON.parseObject(
          response.body?.string(),
          object : TypeReference<Result<Page<History>>>() {})
      if (result.code == 200) {
        return true
      }
    }
    return false
  }
}