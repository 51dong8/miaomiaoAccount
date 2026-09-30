package com.miaomiao.jizhang.core.data.repository

import com.miaomiao.jizhang.core.common.RecurringLogic
import com.miaomiao.jizhang.core.data.dao.RecurringRuleDao
import com.miaomiao.jizhang.core.data.dao.TransactionDao
import com.miaomiao.jizhang.core.data.entity.RecurringRuleEntity
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringRepository @Inject constructor(
    private val ruleDao: RecurringRuleDao,
    private val transactionDao: TransactionDao
) {

    fun observeAll(): Flow<List<RecurringRuleEntity>> = ruleDao.observeAll()

    suspend fun add(rule: RecurringRuleEntity): Long = ruleDao.insert(rule)

    suspend fun update(rule: RecurringRuleEntity) = ruleDao.update(rule)

    suspend fun delete(rule: RecurringRuleEntity) = ruleDao.delete(rule)

    suspend fun setActive(rule: RecurringRuleEntity, active: Boolean) =
        ruleDao.update(rule.copy(active = active))

    /**
     * 为所有启用的规则补生成到期账单（从上次生成日的次日到今天）。
     * @return 本次生成的账单条数
     */
    suspend fun generateDue(today: LocalDate): Int {
        var count = 0
        ruleDao.getActive().forEach { rule ->
            val from = runCatching { LocalDate.parse(rule.lastGeneratedDate) }
                .getOrDefault(rule.startDate.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) } ?: today)
            val dueDates = RecurringLogic.dueDatesForRange(
                rule.frequency,
                rule.dayOfWeek,
                rule.dayOfMonth,
                from,
                today
            )
            if (dueDates.isNotEmpty()) {
                dueDates.forEach { date ->
                    transactionDao.insert(
                        TransactionEntity(
                            type = rule.type,
                            amount = rule.amount,
                            categoryId = rule.categoryId,
                            accountId = rule.accountId,
                            note = rule.note,
                            dateTime = RecurringLogic.transactionTime(date)
                        )
                    )
                }
                ruleDao.update(rule.copy(lastGeneratedDate = dueDates.last().toString()))
                count += dueDates.size
            }
        }
        return count
    }
}
