package com.miaomiao.jizhang.feature.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import com.miaomiao.jizhang.core.data.repository.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountManageViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {

    val accounts: StateFlow<List<AccountEntity>> = accountRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String, icon: String, color: Long) {
        viewModelScope.launch {
            accountRepository.add(name, icon, color)
        }
    }

    fun update(account: AccountEntity, name: String, icon: String, color: Long) {
        viewModelScope.launch {
            accountRepository.update(account.copy(name = name, icon = icon, color = color))
        }
    }

    fun setArchived(account: AccountEntity, archived: Boolean) {
        viewModelScope.launch {
            accountRepository.setArchived(account, archived)
        }
    }
}

/** 可选账户 emoji。 */
val AccountEmojis = listOf(
    "💵", "💬", "💳", "🏦", "🐷", "👛", "💰", "🏧", "🪙", "⭐", "🧧", "📱"
)
