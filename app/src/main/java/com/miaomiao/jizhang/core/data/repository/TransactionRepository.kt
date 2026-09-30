package com.miaomiao.jizhang.core.data.repository

import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.data.dao.TransactionDao
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao
) {
    fun observeAll(): Flow<List<TransactionEntity>> = dao.observeAll()

    fun observeBetween(start: Long, end: Long): Flow<List<TransactionEntity>> =
        dao.observeBetween(start, end)

    suspend fun getBetween(start: Long, end: Long): List<TransactionEntity> =
        dao.getBetween(start, end)

    suspend fun getById(id: Long): TransactionEntity? = dao.getById(id)

    suspend fun insert(transaction: TransactionEntity): Long = dao.insert(transaction)

    suspend fun update(transaction: TransactionEntity) = dao.update(transaction)

    suspend fun delete(transaction: TransactionEntity) = dao.delete(transaction)

    suspend fun countByCategory(categoryId: Long): Int = dao.countByCategory(categoryId)

    suspend fun countByAccount(accountId: Long): Int = dao.countByAccount(accountId)

    /** 某天的账单数量 */
    suspend fun countByDate(date: LocalDate): Int {
        val (start, end) = DateUtils.dayRangeMillis(date)
        return dao.countBetween(start, end)
    }

    /** 最近一条账单的日期字符串 "yyyy-MM-dd"，无账单返回 null */
    suspend fun getLastRecordDate(): String? =
        dao.getLastDateTime()?.let { DateUtils.toLocalDate(it).toString() }

    suspend fun getAll(): List<TransactionEntity> = dao.getAll()
}
