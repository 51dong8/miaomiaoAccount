# 喵喵记 · AI 提示词模板（PROMPTS.md）

> 每次让 AI 改代码前，先要求它读取对应文档；文档是唯一事实源。

## 新增页面
```
请读取 docs/ARCHITECTURE.md、docs/UI_GUIDE.md。
在 feature/<xxx> 下实现 <页面> 页面。
要求：Compose + ViewModel + Hilt + Flow；遵循现有包结构与组件（SectionCard/CategoryAvatar/GradientButton 等）；
金额用 Long 分，展示用 MoneyFormatter；空状态用 EmptyState；不引入未指定的依赖；完成后给出编译命令。
```

## 改数据库
```
请读取 docs/DATABASE.md。
新增 <表名> 表 / 修改 <表> 的 <字段>。
要求：同步更新 entity/dao/repository/DatabaseModule；Room 版本号 +1 并提供迁移；给出示例 SQL。
```

## 修编译错误
```
以下是编译错误：
【粘贴错误】
请只修改必要文件，不要重构无关代码；修复后说明原因并给出再次编译命令。
```

## 代码审查
```
请审查 <模块>，检查：
1. 是否符合 MVVM 与包级依赖规则（core/ui 不依赖 feature，feature 间不互相依赖）
2. 是否有内存泄漏（协程/Flow 收集）
3. 是否有重复代码
4. 是否缺少测试
只输出问题与修改建议，不要直接改代码。
```

## 验收清单（每个功能）
能编译 / 能运行 / 数据保存 / 退出重进不丢 / 空状态 / 错误状态 / 关键逻辑有测试 / 不破坏已有功能
