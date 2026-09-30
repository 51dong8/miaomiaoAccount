package com.miaomiao.jizhang.core.data.repository

import com.miaomiao.jizhang.core.common.computeDeleteRollback
import com.miaomiao.jizhang.core.common.computeStreak
import com.miaomiao.jizhang.core.data.dao.CatStateDao
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatRepository @Inject constructor(
    private val dao: CatStateDao
) {
    fun observe(): Flow<CatStateEntity> = dao.observe().map { it ?: CatStateEntity() }

    suspend fun get(): CatStateEntity = dao.get() ?: CatStateEntity()

    /**
     * 记账成功时调用：
     * - 小鱼干 +1（每记一笔）
     * - 累计笔数 +1
     * - 连续天数：同日重复记账不增加；昨天有记账则 +1；否则重置为 1
     */
    suspend fun onRecordSaved(recordDate: LocalDate): CatStateEntity {
        val current = dao.get() ?: CatStateEntity()
        val today = recordDate.toString()
        val updated = current.copy(
            fishCount = current.fishCount + 1,
            totalCount = current.totalCount + 1,
            streakDays = computeStreak(current.streakDays, current.lastRecordDate, today),
            lastRecordDate = today
        )
        dao.insert(updated)
        return updated
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        val current = dao.get() ?: CatStateEntity()
        dao.insert(current.copy(animationsEnabled = enabled))
    }

    /**
     * 删除账单时调用，回滚猫咪数据：
     * - 小鱼干 -1（不低于 0）
     * - 累计笔数 -1（不低于 0）
     * - 若删的是最后记账日，连续天数 -1 并回退 lastRecordDate
     * @param lastRemainingDate 删除后当天仍有账单则传当天日期，否则传最近账单日期（无则 null）
     */
    suspend fun onRecordDeleted(recordDate: LocalDate, lastRemainingDate: String?): CatStateEntity {
        val current = dao.get() ?: CatStateEntity()
        val (newStreak, newLast) = computeDeleteRollback(
            current.streakDays,
            current.lastRecordDate,
            recordDate.toString(),
            lastRemainingDate
        )
        val updated = current.copy(
            fishCount = (current.fishCount - 1).coerceAtLeast(0),
            totalCount = (current.totalCount - 1).coerceAtLeast(0),
            streakDays = newStreak,
            lastRecordDate = newLast
        )
        dao.insert(updated)
        return updated
    }
}
