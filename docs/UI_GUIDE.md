# 喵喵记 · UI 规范（UI_GUIDE.md）

## 品牌
暖橘猫咪风格。所有页面使用 Material3 主题，见 `core/ui/theme/`。

## 配色（亮色 / 暗色）
| 用途 | 亮色 | 暗色 |
| --- | --- | --- |
| 主色（橘） | #FF8A3D | #FF9E5C |
| 背景（奶油） | #FFF8F1 | #1A1613 |
| 卡片 | #FFFFFF | #272019 |
| 支出红 | #EB5A5A | #FF8A80 |
| 收入绿 | #27AE60 | 同亮色 |
| 主文本 | #3A2E26 | #F5EDE4 |
| 次要文本 | #9A8C80 | #AB9D90 |

## 形状与尺寸
- 卡片圆角：20dp；输入框/按钮：12-18dp；色块/头像：圆形。
- 卡片内边距：16dp；页面水平边距：20dp。
- 底部导航：4 Tab + 中央 FAB（60dp 圆形，凸出 26dp）。
- 金额展示：粗体大字号（记一笔页面 displaySmall）。

## 猫咪 IP（`core/ui/components/CatFace.kt`）
- Canvas 手绘猫脸，四种表情：SLEEPY（未记账）/ HAPPY / PROUD（连续≥7天）/ SAD（超支）。
- 表情推断统一走 `moodFor(streakDays, recordedToday, overBudget)`。
- 呼吸动画：`animate=true` 时 1.0↔1.05 循环；列表内的小猫默认关闭动画。

## 组件清单
SectionCard / EmptyState / CategoryAvatar / GradientButton / MoneyText / StatCard / DonutChart / TrendLineChart / ProgressBar / NumberPad / CatFace

## 空状态 / 错误状态
- 所有列表为空时显示 EmptyState（猫咪 + 提示语），禁止白屏。
- 删除受限（内置分类/有账单）弹 AlertDialog 说明原因。
- 导入失败弹 Toast 提示「文件格式不正确」。

## 动画规范
- 记账成功：全屏半透明遮罩 + 猫咪放大淡入 + 「小鱼干 +1 🐟」，1.5s 后自动返回。
- 可在「猫咪系统」页关闭该动画（`animationsEnabled`）。
- 页面切换使用 Navigation Compose 默认转场，不额外加动画。

## 图标
- 页面图标统一使用 material-icons-extended（底部导航、按钮、入口列表）。
- 分类/账户图标使用 emoji（可自定义），渲染为彩色圆底。
- 启动图标：自适应图标（奶油底 + 橘色猫爪），minSdk 26 无需 PNG 各密度。
