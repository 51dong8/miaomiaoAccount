package com.miaomiao.jizhang.feature.cat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import com.miaomiao.jizhang.core.data.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatViewModel @Inject constructor(
    private val catRepository: CatRepository
) : ViewModel() {

    val catState: StateFlow<CatStateEntity> = catRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatStateEntity())

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            catRepository.setAnimationsEnabled(enabled)
        }
    }
}

/** 成就定义。 */
data class Achievement(
    val icon: String,
    val title: String,
    val condition: String,
    val unlocked: Boolean
)

fun buildAchievements(state: CatStateEntity): List<Achievement> = listOf(
    Achievement("🌟", "初次记账", "完成第一笔账单", state.totalCount >= 1),
    Achievement("✏️", "记账小能手", "累计记账 10 笔", state.totalCount >= 10),
    Achievement("📝", "记账达人", "累计记账 50 笔", state.totalCount >= 50),
    Achievement("🏆", "记账大师", "累计记账 100 笔", state.totalCount >= 100),
    Achievement("🔥", "三天之约", "连续记账 3 天", state.streakDays >= 3),
    Achievement("⚡", "七日坚持", "连续记账 7 天", state.streakDays >= 7),
    Achievement("👑", "月度之喵", "连续记账 30 天", state.streakDays >= 30),
    Achievement("🐟", "小鱼干富翁", "攒到 50 个小鱼干", state.fishCount >= 50)
)
