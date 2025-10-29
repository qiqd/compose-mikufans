# Mikufans2

Mikufans2 是一款基于 Kotlin 和 Jetpack Compose 开发的安卓动漫视频应用，提供动漫观看、搜索、订阅和历史记录等功能。

## 功能特性

- 📱 **现代化 UI**：采用 Jetpack Compose 和 Material 3 构建的流畅界面
- 🔍 **动漫搜索**：快速搜索你喜欢的动漫内容
- 📋 **每周更新**：查看本周更新的动漫节目
- ⭐ **订阅管理**：订阅喜欢的动漫，不错过更新
- 🎬 **视频播放**：支持多种视频格式播放
- 📖 **历史记录**：自动保存观看历史
- 🌐 **多源支持**：支持多个动漫视频源

## 技术栈

- **开发语言**：Kotlin 2.0+
- **UI 框架**：Jetpack Compose
- **界面设计**：Material 3
- **视频播放**：AndroidX Media3 (ExoPlayer)
- **图片加载**：Coil Compose
- **网络请求**：OkHttp
- **JSON 解析**：FastJson
- **HTML 解析**：Jsoup
- **构建工具**：Gradle 8.13

## 项目结构

```
app/
├── src/main/
│   ├── java/com/mikufans/        # 源代码
│   │   ├── ui/                   # UI 相关代码
│   │   │   ├── component/        # 可复用组件
│   │   │   ├── nav/              # 导航相关
│   │   │   └── page/             # 页面组件
│   │   ├── util/                 # 工具类
│   │   └── xmd/                  # 核心功能模块
│   └── res/                      # 资源文件
└── build.gradle.kts              # 模块配置
```

## 主要页面

- **首页**：展示热门动漫和推荐内容
- **每周更新**：按星期分类展示本周更新的动漫
- **订阅**：管理已订阅的动漫列表
- **我的**：个人中心，包含历史记录、设置等
- **详情页**：动漫详情信息和剧集列表
- **播放页**：视频播放界面

## 安装说明

1. 克隆项目：

2. 使用 Android Studio 打开项目

3. 确保已安装 JDK 11 或更高版本

4. 构建并运行项目

## 版本信息

- 当前版本：v1.0.10
- 支持的 Android 版本：Android 8.0 (API 26) 及以上
- 支持的架构：x86, x86_64, armeabi-v7a, arm64-v8a

## 权限说明

- `INTERNET`：用于网络请求和内容加载
- `CLEAR_APP_CACHE`：用于清理应用缓存

## 开发说明

### 添加新的视频源

如需添加新的视频源，请参考 `SourceUtil` 类中的实现方式，添加对应的数据源解析逻辑。

### 构建发布版本

```bash
./gradlew assembleRelease
```

构建完成后，APK 文件将位于 `app/release/` 目录下。

## License

[GPL-3.0](LICENSE)

## 免责声明

本应用仅供学习和研究使用，所有内容均来源于网络，版权归原作者所有。如有侵权，请联系删除。
