package com.miaomiao.jizhang.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 猫咪状态（单行表，id 恒为 1）。
 */
@Entity(tableName = "cat_state")
data class CatStateEntity(
    @PrimaryKey val id: Int = 1,
    /** 小鱼干数量（每记一笔 +1） */
    val fishCount: Int = 0,
    /** 连续记账天数 */
    val streakDays: Int = 0,
    /** 最近一次记账日期 "yyyy-MM-dd"，空串表示从未记账 */
    val lastRecordDate: String = "",
    /** 累计记账笔数 */
    val totalCount: Int = 0,
    /** 记账成功动画开关 */
    val animationsEnabled: Boolean = true
)
