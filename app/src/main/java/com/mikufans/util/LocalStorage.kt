package com.mikufans.util

import android.content.Context
import androidx.core.content.edit
import com.alibaba.fastjson.JSON
import java.io.File

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

  fun clearCache(context: Context) {
    context.cacheDir?.let { deleteRecursively(it) }
  }

  /**
   * 辅助：删除单个文件或目录
   */
  private fun deleteRecursively(file: File) {
    if (file.isDirectory) {
      file.listFiles()?.forEach { deleteRecursively(it) }
    }
    file.delete()
  }
}


