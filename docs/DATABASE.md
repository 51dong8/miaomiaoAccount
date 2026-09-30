# 喵喵记 · 数据库文档（DATABASE.md）

数据库：`miaomiao.db`，版本 **2**（v1→v2 新增周期账单规则表）。金额单位：**分（Long）**。时间单位：**epoch millis（Long）**。

## 表结构

### transactions（账单）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long PK 自增 | |
| type | String | EXPENSE / INCOME |
| amount | Long | 金额，分 |
| categoryId | Long | 分类 id |
| accountId | Long | 账户 id |
| note | String | 备注 |
| dateTime | Long | 记账时间 |
| createdAt | Long | 创建时间 |

索引：dateTime、categoryId、accountId。

### categories（分类）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long PK 自增 | 内置 1-8 支出、9-12 收入 |
| name | String | 名称 |
| icon | String | emoji |
| color | Long | ARGB |
| type | String | EXPENSE / INCOME |
| isDefault | Boolean | 内置分类不可删除 |
| sortOrder | Int | 排序 |

### accounts（账户）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long PK 自增 | 内置 13-16 |
| name / icon / color | | |
| isArchived | Boolean | 归档后不出现在记账选择 |
| isDefault / sortOrder | | |

### budgets（预算）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long PK 自增 | |
| month | String | "yyyy-MM"（预算周期起始日所在自然月） |
| categoryId | Long? | null = 月总预算 |
| amount | Long | 预算金额，分 |
| createdAt | Long | |

### cat_state（猫咪状态，单行 id=1）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Int PK = 1 | |
| fishCount | Int | 小鱼干（每记一笔 +1，删除账单 -1，不低于 0） |
| streakDays | Int | 连续记账天数 |
| lastRecordDate | String | "yyyy-MM-dd" |
| totalCount | Int | 累计笔数（删除账单 -1，不低于 0） |
| animationsEnabled | Boolean | 记账动画开关 |

### recurring_rules（周期账单规则，v2 新增）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long PK 自增 | |
| type | String | EXPENSE / INCOME |
| amount | Long | 金额，分 |
| categoryId / accountId | Long | 分类 / 账户 |
| note | String | 备注 |
| frequency | String | DAILY / WEEKLY / MONTHLY |
| dayOfWeek | Int? | WEEKLY：1(周一)..7(周日) |
| dayOfMonth | Int? | MONTHLY：1..31，当月无此日取月末 |
| startDate | String | 规则创建日 "yyyy-MM-dd"（当天账单手动记，不自动补） |
| lastGeneratedDate | String | 已生成到的日期，下次从次日补 |
| active | Boolean | 停用后不再生成 |
| createdAt | Long | |

生成逻辑：App 启动时与每日 WorkManager 任务调用 `RecurringRepository.generateDue(today)`，为 active 规则在 (lastGeneratedDate, today] 内补账单；同一条规则不重复生成。

## 种子数据
首次建库时写入（SeedCallback，事务内同步）：
- 支出分类：餐饮🍜、交通🚌、购物🛍️、居住🏠、娱乐🎮、医疗💊、教育📚、其他📦
- 收入分类：工资💰、奖金🎁、理财📈、其他收入💵
- 账户：现金💵、微信💬、支付宝💳、银行卡🏦

## DAO 要点
- 查询优先返回 `Flow`（响应式）；一次性统计用 `suspend`。
- 删除保护：删除分类前查 `countByCategory`；账户只用归档不硬删。
- 备份恢复在 `BackupManager` 内用 `db.withTransaction` 全量重建。

## Repository 接口（feature 层唯一入口）
TransactionRepository / CategoryRepository / AccountRepository / BudgetRepository / CatRepository / RecurringRepository / SettingsRepository(DataStore)

## 迁移
- v1 → v2：`MIGRATION_1_2` 新建 recurring_rules 表（无数据丢失）。
