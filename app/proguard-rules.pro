# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class kotlin.** { *; }

-keep class org.jetbrains.** { *; }
#FastJson反混淆
-keepattributes Signature
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.**{*; }



#
-keep class kotlin.jvm.** {*;}
-keep class kotlin.reflect.jvm.** {*;}


-keep class kotlin.jvm.** {*;}
-keep class kotlin.reflect.jvm.** {*;}

# === 新增：OkHttp + Retrofit ===
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# === 新增：JSoup ===
-keep class org.jsoup.** { *; }

# === 新增：Coil ===
-keep class coil.** { *; }
-keep class * extends coil.ComponentRegistry { *; }

# === 新增：业务实体（按你实际包名改） ===
# 如果周更表、番剧详情等 JSON Bean 都在 com.mikufans.xxx 包下
-keep class com.mikufans.** { *; }
-keepclassmembers class com.mikufans.** { *; }



# === 忽略 fastjson 所有可选扩展 ===
-dontwarn java.awt.**
-dontwarn javax.money.**
-dontwarn javax.ws.rs.**
-dontwarn org.glassfish.jersey.**
-dontwarn org.javamoney.moneta.**
-dontwarn org.joda.time.**
-dontwarn springfox.documentation.**

# 如果 R8 仍提示，可直接丢弃这些扩展代码
-assumenosideeffects class com.alibaba.fastjson.serializer.AwtCodec { *; }
-assumenosideeffects class com.alibaba.fastjson.support.** { *; }


