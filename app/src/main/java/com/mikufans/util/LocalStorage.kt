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

  fun getString(context: Context, key: String): String =
    context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
      .getString(key, "") ?: ""

  fun getInt(context: Context, key: String, exceptionHandler: (Exception) -> Unit = {}): Int =
    try {
      context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        .getInt(key, 0)
    } catch (e: Exception) {
      exceptionHandler(e)
      0
    }

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

  /**
   * 计算 /cache 目录总大小（字节）
   */
  fun getCacheSize(context: Context): Long {
    return getFolderSize(context.cacheDir)
  }

  /**
   * 递归统计目录大小
   */
  private fun getFolderSize(dir: File?): Long {
    if (dir == null || !dir.exists()) return 0L
    var size = 0L
    dir.listFiles()?.forEach { file ->
      size += if (file.isFile) file.length() else getFolderSize(file)
    }
    return size
  }

  /**
   * 格式化字节为 MB/GB 等
   */
  fun formatSize(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
      bytes < kb -> "${bytes} B"
      bytes < mb -> String.format("%.2f KB", bytes / kb)
      bytes < gb -> String.format("%.2f MB", bytes / mb)
      else -> String.format("%.2f GB", bytes / gb)
    }
  }
}


