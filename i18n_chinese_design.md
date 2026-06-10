# Komelia 中文多语言支持设计方案

## 1. 需求分析

用户希望为 Komelia 添加中文支持，包含：
- 核心 Kotlin Multiplatform 应用的中文界面
- EPUB 阅读器 Web UI 的中文界面
- 语言设置选项

## 2. 当前多语言架构分析

### 2.1 Kotlin Multiplatform 核心 (`komelia-core`)

**现状**：
- 使用 `AppStrings` 数据类定义所有可翻译字符串（约 600+ 个字段）
- `EnStrings.kt` 是唯一的语言实现
- 通过 `DependencyContainer.appStrings: StateFlow<AppStrings>` 提供
- 通过 `LocalStrings` CompositionLocal 在 UI 中使用
- **没有语言切换机制**

**文件位置**：
- [AppStrings.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/strings/AppStrings.kt) - 字符串数据类定义
- [EnStrings.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/strings/EnStrings.kt) - 英文翻译实现
- [DependencyContainer.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/DependencyContainer.kt) - 依赖注入接口
- [CompositionLocals.kt](file:///workspace/komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/ui/CompositionLocals.kt) - UI 组合上下文

### 2.2 EPUB Web UI (`komga-webui`)

**现状**：
- 使用 Vue I18n
- 只有 `locales/en.json`（约 1000+ 个翻译项）
- **没有其他语言支持**

### 2.3 EPUB Web UI (`ttu-ebook-reader`)

**现状**：
- 基于 Svelte 5
- 没有明显的多语言文件结构
- 需要检查具体实现

## 3. 可行性评估

| 维度 | 可行性 | 说明 |
|------|--------|------|
| 技术复杂度 | 低 | 现有架构已设计为可扩展，只需添加新语言实现 |
| 工作量 | 中等 | 需要翻译约 1600+ 个字符串 |
| 风险 | 低 | 不影响核心功能，向后兼容 |
| 收益 | 高 | 支持中文用户群 |

## 4. 实现方案

### 4.1 方案一：最小可行实现（推荐）

**范围**：
- 添加中文翻译文件
- 添加语言选择设置
- 不修改核心架构

**优点**：快速实现，低风险

**缺点**：语言切换需要重启应用

### 4.2 方案二：完整动态切换

**范围**：
- 添加中文翻译文件
- 实现运行时语言切换
- 更新所有依赖注入点

**优点**：用户体验更好

**缺点**：需要修改较多代码

## 5. 详细设计（方案一）

### 5.1 创建中文翻译文件

**文件**：`komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/strings/ZhStrings.kt`

```kotlin
package io.github.snd_r.komelia.strings

val ZhStrings = AppStrings(
    filters = FilterStrings(
        anyValue = "任意",
        filterTagsSearch = "搜索标签",
        filterTagsReset = "重置",
        filterTagsGenreLabel = "类型",
        filterTagsTagsLabel = "标签",
        filterTagsShowMore = "显示更多",
        filterTagsShowLess = "显示更少",
    ),
    // ... 其余字段需要翻译
)
```

### 5.2 添加语言枚举

**文件**：`komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/strings/Language.kt`

```kotlin
package io.github.snd_r.komelia.strings

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    CHINESE("zh", "中文")
}
```

### 5.3 添加语言设置到配置

**文件**：`komelia-core/src/commonMain/kotlin/io/github/snd_r/komelia/settings/CommonSettingsRepository.kt`

添加：
```kotlin
fun getLanguage(): AppLanguage
fun setLanguage(language: AppLanguage)
```

### 5.4 更新设置界面

在 `SettingsScreen` 添加语言选择下拉菜单

### 5.5 更新 DependencyContainer

修改各平台的 `DependencyContainer` 实现，根据设置加载对应语言：

```kotlin
// 在 DesktopDependencyContainer.kt 中
override val appStrings = MutableStateFlow(
    when (settingsRepository.getLanguage()) {
        AppLanguage.CHINESE -> ZhStrings
        else -> EnStrings
    }
)
```

### 5.6 EPUB Web UI 中文支持

**文件**：`epub-reader-webui/komga-webui/src/locales/zh.json`

复制 `en.json` 并翻译所有字段。

### 5.7 ttu-ebook-reader 中文支持

需要检查项目结构，添加类似的 i18n 机制。

## 6. 翻译工作量评估

| 模块 | 字符串数量 | 预估翻译时间 |
|------|-----------|-------------|
| komelia-core AppStrings | ~600 | 4-6 小时 |
| komga-webui en.json | ~1000 | 6-8 小时 |
| ttu-ebook-reader | ~300 | 2-3 小时 |
| **总计** | **~1900** | **12-17 小时** |

## 7. 实施步骤

1. **Phase 1**：创建语言枚举和中文翻译文件（ZhStrings.kt）
2. **Phase 2**：更新设置存储和加载逻辑
3. **Phase 3**：添加语言选择 UI
4. **Phase 4**：添加 EPUB Web UI 中文支持
5. **Phase 5**：测试和验证

## 8. 注意事项

1. **编码问题**：确保所有文件使用 UTF-8 编码
2. **RTL 支持**：中文不需要 RTL
3. **Pluralization**：中文复数形式简单，不需要特殊处理
4. **日期格式**：需要支持中文日期格式
5. **测试**：需要在各平台测试中文显示

## 9. 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| 字符串遗漏翻译 | 中 | 使用 IDE 检查未使用的字符串 |
| 字符编码问题 | 低 | 强制 UTF-8 BOM |
| 设置不生效 | 低 | 添加测试用例 |
| 平台兼容性 | 低 | 在所有目标平台测试 |

## 10. 结论

添加中文多语言支持**完全可行**，技术复杂度低，工作量中等。建议采用方案一（最小可行实现），后续可以扩展为动态切换。

下一步：创建中文翻译文件和必要的基础设施代码。
