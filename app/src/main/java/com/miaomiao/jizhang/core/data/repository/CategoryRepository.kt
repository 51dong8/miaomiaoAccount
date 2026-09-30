package com.miaomiao.jizhang.core.data.repository

import com.miaomiao.jizhang.core.data.dao.CategoryDao
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val dao: CategoryDao
) {
    fun observeAll(): Flow<List<CategoryEntity>> = dao.observeAll()

    fun observeByType(type: String): Flow<List<CategoryEntity>> = dao.observeByType(type)

    suspend fun getAll(): List<CategoryEntity> = dao.getAll()

    suspend fun getById(id: Long): CategoryEntity? = dao.getById(id)

    suspend fun add(name: String, icon: String, color: Long, type: String): Long {
        val nextOrder = dao.countByType(type) + 1
        return dao.insert(
            CategoryEntity(name = name, icon = icon, color = color, type = type, isDefault = false, sortOrder = nextOrder)
        )
    }

    suspend fun update(category: CategoryEntity) = dao.update(category)

    suspend fun delete(category: CategoryEntity) = dao.delete(category)

    suspend fun countByType(type: String): Int = dao.countByType(type)
}
