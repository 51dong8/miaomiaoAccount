package com.miaomiao.jizhang.core.data.repository

import com.miaomiao.jizhang.core.data.dao.TransactionDao
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
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

    suspend fun getAll(): List<TransactionEntity> = dao.getAll()
}
