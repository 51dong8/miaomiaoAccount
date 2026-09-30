# 喵喵记 · 架构文档（ARCHITECTURE.md）

## 技术栈（v1.0.0 实际采用）
| 项 | 选型 | 版本 |
| --- | --- | --- |
| 语言 | Kotlin | 2.4.0 |
| UI | Jetpack Compose + Material3 | BOM 2026.09.00 |
| 构建 | AGP + Gradle | AGP 9.1.0 / Gradle 9.3.1 |
| 注解处理 | KSP | 2.3.10 |
| DI | Hilt | 2.60.1 |
| 本地库 | Room | 2.8.5 |
| 偏好 | DataStore Preferences | 1.1.7 |
| 导航 | Navigation Compose | 2.10.2 |
| 异步 | Coroutines + Flow | 随 Kotlin |
| 图表 | 自绘 Canvas（环形图/折线） | — |

> 说明：Vico/Coil/WorkManager/Glance/Biometric 按「阶段引入」原则暂未加入，降低编译面；后续里程碑按需添加。
> AGP 9 默认启用内置 Kotlin 与新 DSL，本项目在 gradle.properties 显式 `android.builtInKotlin=false` + `android.newDsl=false`，使用标准 KGP 以兼容 KSP/Hilt。
> compileSdk 37 / targetSdk 35 / minSdk 26（本机 SDK 仅有 API 34 与 37 平台，取 37）。

## 包结构（单模块，包级边界）
```
com.miaomiao.jizhang
├── core/
│   ├── common/     # 金额、日期、猫咪逻辑等纯工具（可单测）
│   ├── data/       # Room 实体/DAO/DB、Repository、BackupManager、Hilt Module
│   └── ui/         # theme、通用组件、导航（AppRoot / MainScaffold）
└── feature/
    ├── home/       # 首页
    ├── add/        # 记一笔（新增/编辑共用）
    ├── records/    # 账单列表
    ├── stats/      # 统计 + 预算
    ├── cat/        # 猫咪系统
    ├── settings/   # 设置 + 备份
    ├── manage/     # 分类/账户管理
    └── profile/    # 我的
```

## 分层与依赖规则
- **UI（feature）→ ViewModel → Repository → Room**，单向依赖。
- ViewModel 只依赖 Repository 接口实现类（Hilt 注入），不直接碰 DAO。
- 金额一律以 **Long 分** 存储与计算，展示层才格式化。
- 统计/预算聚合在 ViewModel 内完成（数据量小，内存聚合可靠且可测）。
- core/ui 不依赖 feature；feature 之间不互相依赖（跨页复用走导航参数 + Repository）。

## 关键约定
- 新页面 = feature/xxx 下的 Screen + ViewModel，注册进 `core/ui/navigation/AppRoot.kt`。
- 新数据库表 = entity + dao + repository + DatabaseModule 提供者，版本号 +1 并写迁移。
- 时间统一 epoch millis（Room 存 Long），日期显示走 `DateUtils`。
