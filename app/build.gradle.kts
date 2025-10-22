import com.android.build.api.dsl.Packaging

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.mikufans"
  compileSdk = 36
  splits {
    abi {
      isEnable = true
      reset()
      include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
      isUniversalApk = false
    }
  }

  defaultConfig {
    applicationId = "com.mikufans"
    minSdk = 26
    targetSdk = 36
    versionCode = 9
    versionName = "1.0.9"
    androidResources {
      localeFilters += listOf("en", "zh-rCN")
    }
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  // 新增：把 kotlin 模块描述符、版本文件、签名冗余全部扔掉
  // 如果以后用 Room/KAPT，还可以再加 exclude("META-INF/licenses/**")
  fun Packaging.() {
    // 新增：把 kotlin 模块描述符、版本文件、签名冗余全部扔掉
    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/*.version")
    exclude("META-INF/versions/**")
    // 如果以后用 Room/KAPT，还可以再加 exclude("META-INF/licenses/**")
  }
  buildTypes {
    release {
      isMinifyEnabled = true
      isShrinkResources = true
//      signingConfig = signingConfigs.getByName("release")
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      // 按需保留，如果不用 mips 可删掉
      fun Packaging.() {
        jniLibs.keepDebugSymbols.add("*/mips/*.so")          // 按需保留，如果不用 mips 可删掉
      }
      ndk {
        debugSymbolLevel = "none"        // 完全不要符号表
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
  }
}

dependencies {
  // Compose Video 视频播放库
  implementation("androidx.compose.material:material-icons-extended:1.7.8")
  implementation("androidx.media3:media3-exoplayer-hls:1.8.0")
  implementation("androidx.media3:media3-exoplayer:1.8.0")
  implementation("androidx.media3:media3-exoplayer-dash:1.8.0")
  implementation("androidx.media3:media3-ui:1.8.0")
  // Coil 图片加载库
  implementation("io.coil-kt:coil-compose:2.6.0")
  implementation("io.coil-kt:coil-gif:2.6.0")


  implementation(libs.material3)
  implementation(libs.androidx.foundation)
  implementation(libs.androidx.runtime.livedata)
  // 添加新依赖项
  compileOnly(libs.lombok)
  implementation(libs.fastjson)
  implementation(libs.okhttp)
  implementation(libs.jsoup)
  implementation(libs.commons.text)
  implementation(libs.jackson.annotations)
  implementation("io.coil-kt:coil-compose:2.7.0")
  implementation("androidx.compose.material:material-icons-extended:1.7.8")
  implementation("org.jetbrains.kotlin:kotlin-reflect:${libs.versions.kotlin.get()}")

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.navigation.compose)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}