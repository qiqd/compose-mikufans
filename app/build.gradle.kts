plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.example.mikufans"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.mikufans"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
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
  implementation("androidx.media3:media3-exoplayer:1.8.0")
  implementation("androidx.media3:media3-exoplayer-dash:1.8.0")
  implementation("androidx.media3:media3-ui:1.8.0")
  // Coil 图片加载库
  implementation("io.coil-kt:coil-compose:2.6.0")
  implementation("io.coil-kt:coil-gif:2.6.0")


  implementation(libs.material3)
  implementation(libs.androidx.foundation)
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