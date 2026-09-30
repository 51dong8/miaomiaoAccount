package com.miaomiao.jizhang.feature.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.common.MoneyFormatter
import com.miaomiao.jizhang.core.common.RecurringFrequency
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import com.miaomiao.jizhang.core.data.entity.RecurringRuleEntity
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import com.miaomiao.jizhang.core.data.repository.AccountRepository
import com.miaomiao.jizhang.core.data.repository.CatRepository
import com.miaomiao.jizhang.core.data.repository.CategoryRepository
import com.miaomiao.jizhang.core.data.repository.RecurringRepository
import com.miaomiao.jizhang.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val catRepository: CatRepository,
    private val recurringRepository: RecurringRepository
) : ViewModel() {

    /** 编辑模式下的账单 id；新增为 null */
    val editingId: Long? = savedStateHandle.get<Long>("transactionId")?.takeIf { it > 0 }

    /** 重复记账：来源账单 id（add/copy/{copyId} 路由） */
    private val copyId: Long? = savedStateHandle.get<Long>("copyId")?.takeIf { it > 0 }

    var isExpense by mutableStateOf(true)
        private set

    /** 金额输入串（如 "12.34"），展示时格式化 */
    var amountInput by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf<CategoryEntity?>(null)
        private set

    var selectedAccount by mutableStateOf<AccountEntity?>(null)
        private set

    var note by mutableStateOf("")
        private set

    var selectedDate by mutableStateOf(LocalDate.now())
        private set

    /** 新增保存成功后的猫咪状态（驱动动画） */
    var lastCatState by mutableStateOf<CatStateEntity?>(null)
        private set

    /** 是否展示保存成功动画 */
    var showSaveSuccess by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    private val _expenseCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val expenseCategories: StateFlow<List<CategoryEntity>> = _expenseCategories

    private val _incomeCategories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    val incomeCategories: StateFlow<List<CategoryEntity>> = _incomeCategories

    private val _activeAccounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    val activeAccounts: StateFlow<List<AccountEntity>> = _activeAccounts

    /** 编辑模式下保留的原始时间戳 */
    private var originalDateTime: Long? = null

    /** 周期频率（仅新增时生效，编辑模式禁用） */
    var frequency by mutableStateOf(RecurringFrequency.NONE)
        private set

    /** 每周周期的星期几 1..7 */
    var weeklyDay by mutableStateOf(LocalDate.now().dayOfWeek.value)
        private set

    /** 每月周期的日号 1..31 */
    var monthlyDay by mutableStateOf(LocalDate.now().dayOfMonth)
        private set

    init {
        viewModelScope.launch {
            categoryRepository.observeByType(TxType.EXPENSE).collectLatest { list ->
                _expenseCategories.value = list
                if (isExpense && selectedCategory?.type != TxType.EXPENSE) {
                    selectedCategory = list.firstOrNull()
                }
            }
        }
        viewModelScope.launch {
            categoryRepository.observeByType(TxType.INCOME).collectLatest { list ->
                _incomeCategories.value = list
                if (!isExpense && selectedCategory?.type != TxType.INCOME) {
                    selectedCategory = list.firstOrNull()
                }
            }
        }
        viewModelScope.launch {
            accountRepository.observeActive().collectLatest { list ->
                _activeAccounts.value = list
                if (selectedAccount == null) {
                    selectedAccount = list.firstOrNull { it.isDefault } ?: list.firstOrNull()
                }
            }
        }
        loadEditing()
        loadCopy()
    }

    private fun loadEditing() {
        val id = editingId ?: return
        viewModelScope.launch {
            transactionRepository.getById(id)?.let { t ->
                originalDateTime = t.dateTime
                isExpense = t.type == TxType.EXPENSE
                amountInput = MoneyFormatter.formatPlain(t.amount).replace(",", "")
                selectedCategory = categoryRepository.getById(t.categoryId)
                selectedAccount = accountRepository.getById(t.accountId)
                note = t.note
                selectedDate = DateUtils.toLocalDate(t.dateTime)
            }
        }
    }

    /** 重复记账：复制原账单的金额/分类/账户/备注，日期归为今天，按新增保存。 */
    private fun loadCopy() {
        val id = copyId ?: return
        viewModelScope.launch {
            transactionRepository.getById(id)?.let { t ->
                isExpense = t.type == TxType.EXPENSE
                amountInput = MoneyFormatter.formatPlain(t.amount).replace(",", "")
                selectedCategory = categoryRepository.getById(t.categoryId)
                selectedAccount = accountRepository.getById(t.accountId)
                note = t.note
                selectedDate = LocalDate.now()
            }
        }
    }

    val currentCategories: List<CategoryEntity>
        get() = if (isExpense) _expenseCategories.value else _incomeCategories.value

    fun switchType(expense: Boolean) {
        if (isExpense == expense) return
        isExpense = expense
        selectedCategory = currentCategories.firstOrNull()
    }

    fun setDate(date: LocalDate) {
        selectedDate = date
    }

    fun updateNote(text: String) {
        note = text
    }

    fun selectCategory(category: CategoryEntity) {
        selectedCategory = category
    }

    fun selectAccount(account: AccountEntity) {
        selectedAccount = account
    }

    fun updateFrequency(value: String) {
        frequency = value
    }

    fun updateWeeklyDay(day: Int) {
        weeklyDay = day.coerceIn(1, 7)
    }

    fun updateMonthlyDay(day: Int) {
        monthlyDay = day.coerceIn(1, 31)
    }

    /** 数字键盘输入。 */
    fun onKeyPress(key: String) {
        when (key) {
            "⌫" -> amountInput = amountInput.dropLast(1)
            "." -> {
                if (amountInput.contains(".")) return
                amountInput = if (amountInput.isEmpty()) "0." else amountInput + "."
            }
            else -> {
                if (!key.all { it.isDigit() }) return
                if (amountInput.length >= 10) return
                val parts = amountInput.split(".")
                if (parts.size == 2 && parts[1].length >= 2) return
                amountInput = if (amountInput == "0") key else amountInput + key
            }
        }
    }

    val canSave: Boolean
        get() = (MoneyFormatter.parseToFen(amountInput) ?: 0L) > 0 &&
            selectedCategory != null &&
            selectedAccount != null

    fun save(onSaved: () -> Unit) {
        val fen = MoneyFormatter.parseToFen(amountInput) ?: return
        val category = selectedCategory ?: return
        val account = selectedAccount ?: return
        if (isSaving) return
        isSaving = true

        viewModelScope.launch {
            val dateTime = originalDateTime
                ?: DateUtils.dayStartMillis(selectedDate) +
                    LocalTime.now().toSecondOfDay() * 1000L

            val transaction = TransactionEntity(
                id = editingId ?: 0,
                type = if (isExpense) TxType.EXPENSE else TxType.INCOME,
                amount = fen,
                categoryId = category.id,
                accountId = account.id,
                note = note.trim(),
                dateTime = dateTime
            )
            if (editingId != null) {
                transactionRepository.update(transaction)
                isSaving = false
                onSaved()
            } else {
                transactionRepository.insert(transaction)
                // 周期账单：登记规则（首次记账日即 startDate，下次从次日生成）
                if (frequency != RecurringFrequency.NONE) {
                    recurringRepository.add(
                        RecurringRuleEntity(
                            type = transaction.type,
                            amount = transaction.amount,
                            categoryId = transaction.categoryId,
                            accountId = transaction.accountId,
                            note = transaction.note,
                            frequency = frequency,
                            dayOfWeek = if (frequency == RecurringFrequency.WEEKLY) weeklyDay else null,
                            dayOfMonth = if (frequency == RecurringFrequency.MONTHLY) monthlyDay else null,
                            startDate = selectedDate.toString(),
                            lastGeneratedDate = selectedDate.toString(),
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
                lastCatState = catRepository.onRecordSaved(selectedDate)
                isSaving = false
                // 仅当猫咪动画开关开启时弹保存成功动画；关闭时直接返回
                if (lastCatState?.animationsEnabled == true) {
                    showSaveSuccess = true
                } else {
                    onSaved()
                }
            }
        }
    }

    fun consumeSuccess(onFinished: () -> Unit) {
        showSaveSuccess = false
        onFinished()
    }

    fun delete(onDeleted: () -> Unit) {
        val id = editingId ?: return
        viewModelScope.launch {
            transactionRepository.getById(id)?.let { t ->
                val day = DateUtils.toLocalDate(t.dateTime)
                val remainingToday = transactionRepository.countByDate(day)
                transactionRepository.delete(t)
                val lastRemaining = if (remainingToday <= 1) {
                    // 当天已无账单：回退到删除后的最近账单日期（可能为 null）
                    transactionRepository.getLastRecordDate()
                } else {
                    // 当天还有其他账单，连续记录不受影响
                    day.toString()
                }
                catRepository.onRecordDeleted(day, lastRemaining)
            }
            onDeleted()
        }
    }
}
