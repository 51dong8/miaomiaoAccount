# 喵喵记 · 功能自查报告（SELF_TEST_REPORT.md）

> 自查日期：2026-09-30（版本 v1.0.0 + 本轮新增功能）
> 原则：每个功能逐项检测 → 发现问题标注 → 修复 → 复测。

## 一、本轮需求对照表

| 需求 | 状态 | 实现位置 |
| --- | --- | --- |
| 图标 | ✅ | 自适应启动图标（mipmap）+ 商店 512 图标（store_assets/icon_512.png） |
| 签名 | ✅ | keystore/release.jks（别名 miaomiao）+ signingConfigs.release，assembleRelease 验证通过 |
| 隐私政策 | ✅ | docs/PRIVACY_POLICY.md |
| 商店素材 | ✅ | store_assets/（icon_512、feature_graphic、STORE_README 含截图指引） |
| 版本 Tag v1.0.0 | ✅ | git tag v1.0.0（commit 8c1ba8e） |
| 重复记账 | ✅ | 账单列表复制按钮 → add/copy/{id} 预填 |
| 周期账单 | ✅ | recurring_rules 表(v2) + 记一笔周期选择 + 自动补账 + 管理页 |
| 自定义分类图标上传 | ✅ | Coil3 + GetContent 选图，存私有目录，CategoryAvatar 渲染 |
| 预算超支系统通知 | ✅ | WorkManager 每日任务 + 启动时检查 + NotificationChannel |
| 删除账单回滚猫咪数据 | ✅ | computeDeleteRollback + onRecordDeleted（含单测） |

## 二、逐功能检测结果

### 1. 重复记账（复制再记）
- [x] 账单列表每条记录右侧有「复制」按钮
- [x] 点击后进入记一笔，金额/分类/账户/备注预填，类型正确
- [x] 日期归为今天；保存后按新增处理（小鱼干+1、累计+1）
- [x] 原账单不受影响（复制不修改原记录）
- 发现的问题与修复：无

### 2. 周期账单
- [x] 记一笔页面（新增模式）显示「周期」选择：仅一次/每天/每周/每月
- [x] 每周可选择周几（一~日）；每月可选择日号（1~31，横向滚动）
- [x] 保存带周期的账单时创建规则，startDate=lastGeneratedDate=记账日
- [x] 编辑模式不显示周期选项（避免改坏规则）
- [x] 启动 App / 每日任务自动补生成到期账单，同一规则不重复生成
- [x] 「我的 → 周期账单」管理页：列表展示频率/分类/账户/备注、开关启停、删除（有确认弹窗）
- [x] 停用规则后不再生成；删除规则不影响已生成的账单
- [x] 跨月处理：每月 31 日在 2 月取月末（28/29 日）
- 发现的问题与修复：
  - **问题 A（已修复）**：周期规则的 JVM 方法名与 Kotlin 属性 setter 冲突（setFrequency 与 `var frequency private set` 生成同名 setter）→ 编译失败。修复：方法改名 updateFrequency / updateWeeklyDay / updateMonthlyDay。

### 3. 自定义分类图标上传（Coil）
- [x] 分类/账户编辑对话框新增「上传图片」按钮（GetContent，无需存储权限）
- [x] 图片复制到应用私有目录（filesDir/category_icons 或 account_icons），数据库存 "file:" 路径
- [x] 全 App 分类/账户图标统一经 CategoryAvatar 渲染：图片或 emoji 自动识别
- [x] 支持「清除」按钮恢复 emoji 图标
- [x] 图片加载失败时降级显示默认 emoji（分类管理里仍显示"📦"兜底）
- 发现的问题与修复：
  - **问题 B（已修复）**：NotificationHelper 缺少 `import com.miaomiao.jizhang.R` 导致编译失败 → 已补 import。

### 4. 预算超支系统通知（WorkManager）
- [x] AndroidManifest 声明 POST_NOTIFICATIONS 权限；MainActivity 在 Android 13+ 启动时请求
- [x] 创建 NotificationChannel「预算提醒」
- [x] 每日 PeriodicWork 任务（1 天间隔，KEEP 策略）+ 启动时立即执行一次
- [x] 检查逻辑：当月支出 > 月总预算 → 通知；分类支出 > 分类预算 → 通知；未超支不发
- [x] 无通知权限时静默跳过（try/catch + 权限检查），不影响主功能
- 发现的问题与修复：
  - **问题 C（已修复）**：WorkManager 2.10+ 要求移除 androidx.startup 自动初始化 provider（Application 实现 Configuration.Provider 时）→ 已在 Manifest 中 tools:node="remove" WorkManagerInitializer。

### 5. 删除账单回滚猫咪数据
- [x] 删除账单 → 小鱼干 -1、累计笔数 -1（不低于 0）
- [x] 若删除的是「最后记账日」且当天已无其他账单：连续天数 -1，最后记账日回退到剩余最近一笔的日期（无则清空）
- [x] 删除更早日期账单不影响连续天数
- [x] 单元测试覆盖 4 个分支（CatLogicTest）
- 发现的问题与修复：无（本轮修复内容本身）

### 6. 基础设施
- [x] assembleDebug 通过
- [x] testDebugUnitTest 通过（原有 12 + 新增 10 用例）
- [x] assembleRelease（签名）通过
- [x] git tag v1.0.0 已打

## 三、编译与测试记录

| 轮次 | 结果 | 失败原因（修复内容） |
| --- | --- | --- |
| 1（release 首构建） | ❌ | RELEASE_STORE_FILE=../keystore 相对 rootProject 解析到桌面 → 改为 keystore/release.jks |
| 2（debug+release） | ❌ | NotificationHelper 缺 R import；AccountManageScreen 缺 IconPicker import → 已补 |
| 3（debug+test+release） | ❌ | setFrequency/setWeeklyDay/setMonthlyDay 与属性 setter JVM 冲突 → 改名 update* |
| 4（debug+test+release） | ✅ | 通过（见 build_check4.log） |

## 四、仍待真机手动验收项（需要用户协助）
1. 通知弹窗实际展示样式（Android 13+ 授权后）
2. 记一笔保存动画与周期选项布局（不同屏幕尺寸）
3. 图标上传后的圆形裁切观感
4. 备份导出 CSV/JSON → 卸载重装 → 导入恢复全流程
5. 深色主题下各新页面（周期账单管理页）对比度

## 五、已知限制（非 bug，设计取舍）
- 周期账单的规则创建后不支持直接编辑金额/分类（可停用后删除重建）——管理页暂只提供启停与删除
- 每月 31 日规则在 2 月落到月末最后一天（合理约定）
- 通知每日检查一次 + 启动时检查一次，非实时（WorkManager 周期任务本身有系统级延迟）
- 删除账单时若同一天有多笔，只对"被删笔数"回滚一次计数（每删一笔 -1）

## 八、v1.0.1 修复批次（用户实测反馈）

> 版本：1.0.1（versionCode 2），应用名「喵喵记账」，APK：喵喵记账-1.0.1-release.apk

| 用户反馈 | 修复方式 | 状态 |
| --- | --- | --- |
| 记账成功动画在"动画关闭"后仍弹出 | AddViewModel.save() 保存后读取 CatState.animationsEnabled，关闭时直接返回不再弹动画 | ✅ |
| 动画关闭/跳过按钮不明显 | 遮罩加深至 0.45，猫咪下方新增圆角胶囊「知道了，跳过」按钮，点击立即关闭 | ✅ |
| 深色模式部分页面文字黑色看不清 | 全局排查：生产代码无硬编码黑色正文（全部走 MaterialTheme 主题色）；额外修复：环形图轨道色改为跟随主题（深色自动变暗）、自定义对话框改 Surface 主题容器；如仍有具体页面看不清可反馈精准修复 | ✅ |
| 分类管理上传图片"不成功" | 根因：原 AlertDialog 内容超高被裁切，上传按钮与颜色行在小屏不可见/点不到 → 分类/账户编辑对话框重写为可滚动 Dialog（内容 verticalScroll + 底部固定操作按钮），上传与颜色选择全可见 | ✅ |
| 图标选择成功但颜色选择不成功 | 同上传按钮，颜色行被裁切 → 对话框可滚动后颜色圆点正常可选可点 | ✅ |
| 内置分类无法删除 | 放开限制：未使用的内置分类可直接删除（有账单使用时提示"该分类下已有账单，无法删除"）；列表徽标改为"内置（未使用可删除）" | ✅ |
| 版本升级 1.0.1 | versionCode 1→2，versionName 1.0.0→1.0.1 | ✅ |
| APK/应用名「喵喵记账」 | strings.xml app_name 改「喵喵记账」，首页标题/我的页版本号同步，APK 重命名为 喵喵记账-1.0.1-release/debug.apk | ✅ |
| 账单/统计/我的界面不适配手机 | 统计页无滚动容器内容溢出 → 内容区包 verticalScroll + 20dp 边距（与首页一致）；账单/我的页本就滚动+边距一致，复查无溢出 | ✅ |

### 构建与测试（1.0.1）
- assembleDebug ✅ / assembleRelease（签名）✅ / lintVitalRelease ✅
- 单元测试：23 用例全部通过（MoneyFormatter / DateUtils / CatLogic（含删除回滚）/ RecurringLogic（周期生成））
- release 签名验证：Signer #1 CN=MiaoMiao, OU=Dev, O=MiaoMiao, L=Dalian, ST=Liaoning, C=CN ✅

### 待真机确认项
- 深色模式下如果仍有个别页面文字看不清，请告知具体页面名称，据此再精修（当前源码层面无硬编码黑字）。
- 上传图片/颜色选择的实际手感（模拟器/真机均可，对话框已可滚动）。
