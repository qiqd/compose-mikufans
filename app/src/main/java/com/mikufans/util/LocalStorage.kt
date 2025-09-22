package com.mikufans.util

import android.content.Context
import androidx.core.content.edit
import com.alibaba.fastjson.JSON

object LocalStorage {
  private const val FILE = "local_storage"

  fun <T> set(context: Context, key: String, value: T) {
    context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
      .edit {
        putString(key, JSON.toJSONString(value))
      }
  }

  fun <T> setList(context: Context, key: String, value: List<T>) {
    context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
      .edit {
        putString(key, JSON.toJSONString(value))
      }
  }

  fun <T> get(context: Context, key: String, clazz: Class<T>): T? =
    context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
      .getString(key, null)
      ?.let { JSON.parseObject(it, clazz) }

  fun <T> getList(context: Context, key: String, clazz: Class<T>): List<T>? =
    context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
      .getString(key, null)
      ?.let { JSON.parseArray(it, clazz) }

}


