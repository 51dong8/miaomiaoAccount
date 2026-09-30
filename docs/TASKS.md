# 喵喵记 · 任务清单（TASKS.md）

## 里程碑 M1 ✅ 项目骨架 + 记一笔 + 账单列表
- [x] Gradle 工程（AGP 9.0.1 / Gradle 9.1.0 / Kotlin 2.4.0 / KSP 2.3.10 / Hilt 2.60.1）
- [x] Room 数据层 + 种子数据 + 首次启动
- [x] 主题（亮/暗/跟随系统）+ 猫咪 IP + 图表组件
- [x] 底部导航 4 Tab + 中央 FAB
- [x] 记一笔（金额键盘/分类/账户/日期/备注/保存动画）
- [x] 账单列表（按日分组/搜索/筛选/编辑/删除）

## 里程碑 M2 ✅ 分类、账户、统计
- [x] 分类管理（默认+自定义、删除保护）
- [x] 账户管理（归档）
- [x] 统计（月收支、环形图、排行、趋势线）

## 里程碑 M3 ✅ 预算、猫咪系统、设置
- [x] 预算（月/分类、进度、超支提醒、每月起始日）
- [x] 猫咪系统（小鱼干、连续天数、成就、动画开关）
- [x] 设置（主题、货币、备份导出/导入恢复）

## 里程碑 M4 ✅ 测试、发布（2026-09-30 完成）
- [x] JVM 单元测试（金额格式化、日期/预算周期、连续记账/删除回滚、周期账单生成）✅ 全部通过
- [x] 首次编译验证（assembleDebug 成功，APK 19.7MB）
- [x] Release 签名（keystore/release.jks，别名 miaomiao，密码 miaomiao2026）+ assembleRelease 签名 APK
- [x] 版本 Tag `v1.0.0`（commit 8c1ba8e）
- [x] 隐私政策（docs/PRIVACY_POLICY.md）
- [x] 商店素材（store_assets/icon_512.png、feature_graphic.png、STORE_README.md）
- [ ] 真机手动验收（见 docs/SELF_TEST_REPORT.md 的自测清单）

## 已实现 backlog（2026-09-30）
- [x] 重复记账：账单列表每条记录带「复制再记」按钮，一键预填再记
- [x] 周期账单：记一笔选「每天/每周/每月」建规则；启动时 + 每日 WorkManager 自动补账单；「我的 → 周期账单」管理（启停/删除）
- [x] 预算超支系统通知：WorkManager 每日任务 + 启动时检查，月预算/分类预算超支发系统通知（NotificationChannel）
- [x] 自定义分类/账户图标上传（Coil 3）：编辑对话框「上传图片」，存本地私有目录，任意界面渲染；可清除回 emoji
- [x] 修复：删除账单后小鱼干/累计笔数/连续天数同步回滚（computeDeleteRollback，含单元测试）

## 下一步候选（backlog）
- 账户余额与转账
- 桌面小组件（Glance）
- 生物识别锁
- 云同步
