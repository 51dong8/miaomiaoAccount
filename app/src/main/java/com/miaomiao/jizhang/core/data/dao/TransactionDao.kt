package com.miaomiao.jizhang.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC, id DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateTime BETWEEN :start AND :end ORDER BY dateTime DESC, id DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateTime BETWEEN :start AND :end ORDER BY dateTime DESC, id DESC")
    suspend fun getBetween(start: Long, end: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE note LIKE '%' || :keyword || '%' ORDER BY dateTime DESC, id DESC")
    fun searchByNote(keyword: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countByCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun countByAccount(accountId: Long): Int

    /** 某时间区间（一天）内的账单数，用于删除回滚判断 */
    @Query("SELECT COUNT(*) FROM transactions WHERE dateTime BETWEEN :start AND :end")
    suspend fun countBetween(start: Long, end: Long): Int

    /** 最近一条账单的时间戳（删除后回退连续天数用） */
    @Query("SELECT dateTime FROM transactions ORDER BY dateTime DESC, id DESC LIMIT 1")
    suspend fun getLastDateTime(): Long?

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
