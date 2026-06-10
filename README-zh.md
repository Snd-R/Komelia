# Komelia

Kotlin Multiplatform Comic/Manga Reader for Komga

Kamelia 是一款基于 Kotlin Multiplatform 构建的漫画/漫画阅读器，专为 Komga 服务器设计。

## 功能特点

### 核心功能
- 📚 **Komga 集成**: 与 Komga 服务器无缝集成，支持浏览和阅读您的漫画库
- 📖 **多格式支持**: 支持 CBZ、CBR、PDF 和 EPUB 格式
- 🎨 **自定义主题**: 支持深色、浅色和 OLED 主题
- 🌐 **多语言支持**: 支持英语和中文界面

### 阅读功能
- 📱 **响应式布局**: 支持单行、双页和网页漫画布局
- 🔄 **阅读方向**: 支持从左到右、从右到左和垂直阅读
- 🎯 **智能裁剪**: 自动检测和裁剪页面边框
- ⚡ **性能优化**: 流畅的页面渲染和快速翻页

### 高级功能
- 🎭 **颜色校正**: 支持亮度、对比度和曲线调整
- 🖼️ **面板检测**: 自动检测漫画面板布局
- 📊 **阅读进度**: 自动跟踪阅读进度
- 🔔 **通知提醒**: 支持新章节通知

## 平台支持

| 平台 | 状态 | 说明 |
|------|------|------|
| 🖥️ Desktop (Windows/macOS/Linux) | ✅ 支持 | 完整功能 |
| 📱 Android | ✅ 支持 | 完整功能 |
| 🌐 Web (WASM) | ✅ 支持 | 部分功能 |

## 快速开始

### 前置要求

- Komga 服务器 (v0.16.x 或更高版本)
- Java 21 或更高版本 (仅桌面版)

### 安装

**桌面版**
```bash
# 下载最新版本
# 从 GitHub Releases 下载对应平台的安装包
```

**Android**
```bash
# 从 Google Play 或 GitHub Releases 下载
```

### 配置

1. 启动应用
2. 在设置中添加 Komga 服务器地址
3. 输入您的 Komga 凭据
4. 开始浏览您的漫画库

## 技术栈

- **框架**: Compose Multiplatform
- **语言**: Kotlin 2.0
- **后端**: Komga API
- **数据库**: SQLite (桌面/Android) / IndexedDB (Web)
- **图像处理**: Coil 3, ONNX Runtime

## 构建说明

### 构建桌面版

```bash
./gradlew desktopApp:run
```

### 构建 Android 版

```bash
./gradlew androidApp:installDebug
```

### 构建 Web 版

```bash
./gradlew wasmJsApp:wasmBrowserDevelopmentRun
```

## 贡献

欢迎贡献代码！请先阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

MIT License - 详见 [LICENSE](LICENSE)

## 致谢

- [Komga](https://komga.org/) - 优秀的漫画服务器
- [JetBrains](https://www.jetbrains.com/) - Kotlin 和 Compose Multiplatform
- [Coil](https://coil-kt.github.io/coil/) - 图像加载库

---

⭐ 如果您喜欢这个项目，请给它一个星标！