package com.miaomiao.jizhang.feature.manage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.repository.CategoryRepository
import com.miaomiao.jizhang.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 分类删除结果：null=成功，否则为失败原因。 */
enum class DeleteFailReason {
    HAS_TRANSACTIONS
}

@HiltViewModel
class CategoryManageViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var typeTab by mutableStateOf(TxType.EXPENSE)
        private set

    fun updateTypeTab(type: String) {
        typeTab = type
    }

    fun add(name: String, icon: String, color: Long) {
        viewModelScope.launch {
            categoryRepository.add(name, icon, color, typeTab)
        }
    }

    fun update(category: CategoryEntity, name: String, icon: String, color: Long) {
        viewModelScope.launch {
            categoryRepository.update(
                category.copy(name = name, icon = icon, color = color)
            )
        }
    }

    fun delete(category: CategoryEntity, onResult: (DeleteFailReason?) -> Unit) {
        viewModelScope.launch {
            // 内置分类同样允许删除（未使用的）；已被账单使用则提示
            if (transactionRepository.countByCategory(category.id) > 0) {
                onResult(DeleteFailReason.HAS_TRANSACTIONS)
                return@launch
            }
            categoryRepository.delete(category)
            onResult(null)
        }
    }
}

/** 可选 emoji 列表。 */
val CategoryEmojis = listOf(
    "🍜", "🍔", "☕", "🍺", "🍎", "🚗", "🚌", "✈️",
    "🛒", "👕", "🏠", "🎮", "💊", "📚", "🎁", "💄",
    "📱", "🎵", "🐱", "🐶", "⚽", "💇", "🧾", "🎬"
)
