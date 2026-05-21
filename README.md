# QuickIME Android

Android 平台的智能五笔输入法，支持知识库检索和 AI 生成建议回复。

## 功能特性

### 核心功能
- **五笔输入法** - 86/98 版编码支持
- **知识库检索** - SQLite FTS5 全文搜索，支持分类检索
- **AI 建议** - 本地/云端 AI 生成客服回复
- **客服场景** - FAQ 回复、投诉处理、订单咨询、退款流程

### 快捷回复标签
- `[知]` - 知识库匹配
- `[AI]` - AI 生成
- `[FAQ]` - FAQ 回复
- `[话术]` - 工作话术

## 技术栈

| 组件 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose |
| 数据库 | Room + SQLite FTS5 |
| AI | 本地 LLM / API |
| DI | Hilt |
| 架构 | MVVM + Clean Architecture |

## 项目结构

```
app/src/main/java/com/quickime/
├── core/                  # 核心业务逻辑（平台无关）
│   ├── wubi/            # 五笔编码引擎
│   ├── kb/              # 知识库 RAG
│   ├── ai/              # AI 推理
│   └── cs/              # 客服场景
├── android/             # Android 特定实现
│   ├── ime/             # InputMethodService
│   └── keyboard/        # Compose 键盘 UI
└── di/                   # Hilt 依赖注入
```

## 编译

### 前置要求
- Android Studio Hedgehog 或更高版本
- Android SDK API 34
- Kotlin 1.9.22

### 编译步骤

```bash
# 同步 Gradle
./gradlew sync

# 编译 Debug 版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

### 启用输入法

1. 安装后，进入 **设置 → 系统 → 键盘和输入法**
2. 找到 **QuickIME**，启用并设为默认
3. 在任意输入框切换到 QuickIME

## 客服场景使用

1. **启用客服模式**：长按输入法切换键
2. **触发建议**：输入关键词后点击候选栏
3. **一键发送**：点击建议后自动粘贴到输入框

## 配置

在 `shared_prefs` 中配置：

```json
{
    "ai_enabled": true,
    "kb_enabled": true,
    "ai_weight": 0.5,
    "kb_weight": 0.5,
    "max_suggestions": 5
}
```

## 开发

### 添加依赖

在 `app/build.gradle.kts` 中添加：

```kotlin
implementation("com.quickime:core:1.0.0")
```

### 运行测试

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 许可证

MIT License
