# 喵喵记账（MiaoMiao JiZhang）

🐱 离线优先的猫咪陪伴记账应用 —— 记一笔、看账单、做统计、设预算，每天都有小鱼干。

> 纯本地存储，无网络依赖；数据不离开手机，可随时 CSV / JSON 备份恢复。

## ✨ 功能

- **记一笔**：支出 / 收入，金额大键盘，分类、账户、日期、备注，3 秒完成
- **账单列表**：按日分组（今天 / 昨天 / 日期头），搜索、筛选、编辑、删除、复制再记
- **重复记账 / 周期账单**：按日 / 周 / 月设置周期，后台自动生成，可管理启停
- **分类与账户管理**：默认 + 自定义，支持上传自定义图标（Coil），内置分类可删除
- **统计**：月收支卡片、分类饼图、趋势折线、分类排行；月预算 + 分类预算 + 进度条
- **预算超支系统通知**：WorkManager 每日检查，超支提醒（含自定义分类预算）
- **猫咪系统**：记账得小鱼干、连续记账、成就、猫咪反馈动画（可在设置中关闭）
- **设置**：主题（亮 / 暗 / 跟随系统）、货币符号、每月起始日、CSV / JSON 导入导出备份
- **深色模式**：全局主题色适配，无黑字残留

## 🛠 技术栈

| 层 | 技术 |
| --- | --- |
| 语言 / UI | Kotlin · Jetpack Compose · Material 3 |
| 架构 | MVVM + 模块化（core / feature）· Navigation Compose |
| 异步 | Coroutines + Flow |
| 依赖注入 | Hilt |
| 本地存储 | Room（SQLite，v2 含周期账单表）· DataStore |
| 图表 | Vico |
| 图片 | Coil（自定义分类图标） |
| 后台 | WorkManager（预算超支通知 / 周期账单补账） |
| 导出 | SAF + CSV / JSON |

- 包名 `com.miaomiao.jizhang`，minSdk 26，targetSdk 35，JDK 17
- 版本：`1.0.1`（git tag `v1.0.1`）

## 📦 构建

```bash
# 依赖：JDK 17、Android SDK（compileSdk 36）

# Debug APK
gradlew.bat assembleDebug

# Release APK（需本地 keystore.properties + keystore/release.jks）
gradlew.bat assembleRelease

# 单元测试（23 个用例）
gradlew.bat testDebugUnitTest
```

签名说明：`keystore/` 与 `keystore.properties` 均不入库，请自行妥善备份（丢失将无法更新已发布应用）。

## 📁 目录结构

```
app/
  core/       # 通用：数据层、主题、组件、日期/猫咪/周期纯逻辑
  feature/    # 业务模块：home / records / add / stats / budget / cat / settings / manage / recurring
docs/         # PRODUCT / ARCHITECTURE / DATABASE / UI_GUIDE / TASKS / SELF_TEST_REPORT / PRIVACY_POLICY
store_assets/ # 商店素材：512 图标、feature graphic
```

## 📄 其他

- 隐私政策：见 [docs/PRIVACY_POLICY.md](docs/PRIVACY_POLICY.md)
- 功能自查与修复记录：见 [docs/SELF_TEST_REPORT.md](docs/SELF_TEST_REPORT.md)
