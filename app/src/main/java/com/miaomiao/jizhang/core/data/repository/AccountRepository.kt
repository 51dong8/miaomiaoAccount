package com.miaomiao.jizhang.core.data.repository

import com.miaomiao.jizhang.core.data.dao.AccountDao
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val dao: AccountDao
) {
    fun observeAll(): Flow<List<AccountEntity>> = dao.observeAll()

    fun observeActive(): Flow<List<AccountEntity>> = dao.observeActive()

    suspend fun getAll(): List<AccountEntity> = dao.getAll()

    suspend fun getById(id: Long): AccountEntity? = dao.getById(id)

    suspend fun add(name: String, icon: String, color: Long): Long {
        val nextOrder = dao.getAll().size + 1
        return dao.insert(
            AccountEntity(name = name, icon = icon, color = color, sortOrder = nextOrder)
        )
    }

    suspend fun update(account: AccountEntity) = dao.update(account)

    suspend fun delete(account: AccountEntity) = dao.delete(account)

    suspend fun setArchived(account: AccountEntity, archived: Boolean) =
        dao.update(account.copy(isArchived = archived))
}
