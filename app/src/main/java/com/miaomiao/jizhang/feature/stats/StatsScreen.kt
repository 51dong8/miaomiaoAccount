package com.miaomiao.jizhang.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.common.MoneyFormatter
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.BudgetEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.ui.components.CatFace
import com.miaomiao.jizhang.core.ui.components.CatMood
import com.miaomiao.jizhang.core.ui.components.CategoryAvatar
import com.miaomiao.jizhang.core.ui.components.ChartSlice
import com.miaomiao.jizhang.core.ui.components.DonutChart
import com.miaomiao.jizhang.core.ui.components.EmptyState
import com.miaomiao.jizhang.core.ui.components.SectionCard
import com.miaomiao.jizhang.core.ui.components.TrendLineChart
import com.miaomiao.jizhang.core.ui.theme.ExpenseRed
import com.miaomiao.jizhang.core.ui.theme.IncomeGreen

/** 预算编辑目标。categoryId = null 表示月预算。 */
data class BudgetEditTarget(
    val title: String,
    val categoryId: Long?,
    val currentAmountFen: Long
)

/** 统计：概览（收支/饼图/趋势）+ 预算。 */
@Composable
fun StatsScreen() {
    val vm: StatsViewModel = hiltViewModel()
    val summary by vm.monthSummary.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val currency by vm.currencySymbol.collectAsStateWithLifecycle()
    val budgetPeriod by vm.budgetPeriod.collectAsStateWithLifecycle()
    val periodExpense by vm.periodExpense.collectAsStateWithLifecycle()
    val periodExpenseByCategory by vm.periodExpenseByCategory.collectAsStateWithLifecycle()
    val monthlyBudget by vm.monthlyBudget.collectAsStateWithLifecycle()
    val categoryBudgets by vm.categoryBudgets.collectAsStateWithLifecycle()
    val monthStartDay by vm.monthStartDay.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var editTarget by remember { mutableStateOf<BudgetEditTarget?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶栏
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "统计",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            MonthSwitcher(
                label = DateUtils.formatMonth(vm.selectedMonth),
                onPrev = vm::prevMonth,
                onNext = vm::nextMonth
            )
        }

        // Tab 切换
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            StatsTab("概览", active = tab == 0, Modifier.weight(1f)) { tab = 0 }
            StatsTab("预算", active = tab == 1, Modifier.weight(1f)) { tab = 1 }
        }

        when (tab) {
            0 -> OverviewTab(
                summary = summary,
                categories = categories,
                symbol = currency,
                monthLabel = DateUtils.formatMonth(vm.selectedMonth)
            )
            1 -> BudgetTab(
                periodLabel = budgetPeriod.label,
                periodExpense = periodExpense,
                periodByCategory = periodExpenseByCategory,
                monthlyBudget = monthlyBudget,
                categoryBudgets = categoryBudgets,
                categories = categories,
                symbol = currency,
                monthStartDay = monthStartDay,
                onEditMonthly = {
                    editTarget = BudgetEditTarget(
                        title = "设置月预算",
                        categoryId = null,
                        currentAmountFen = monthlyBudget?.amount ?: 0
                    )
                },
                onEditCategory = { categoryId ->
                    val existing = categoryBudgets.find { it.categoryId == categoryId }
                    editTarget = BudgetEditTarget(
                        title = "设置分类预算",
                        categoryId = categoryId,
                        currentAmountFen = existing?.amount ?: 0
                    )
                },
                onAddCategory = { showCategoryPicker = true }
            )
        }
    }

    // 预算编辑对话框
    editTarget?.let { target ->
        BudgetEditDialog(
            target = target,
            symbol = currency,
            onDismiss = { editTarget = null },
            onConfirm = { amountFen ->
                if (target.categoryId == null) {
                    vm.setMonthlyBudget(amountFen)
                } else {
                    vm.setCategoryBudget(target.categoryId, amountFen)
                }
                editTarget = null
            },
            onClear = {
                target.categoryId?.let { vm.clearCategoryBudget(it) }
                editTarget = null
            }
        )
    }

    // 添加分类预算：先选分类
    if (showCategoryPicker) {
        CategoryBudgetPicker(
            categories = summary.byCategory.mapNotNull { categories[it.categoryId] } +
                categories.values.filter { it.type == TxType.EXPENSE },
            onPick = { category ->
                showCategoryPicker = false
                val existing = categoryBudgets.find { it.categoryId == category.id }
                editTarget = BudgetEditTarget(
                    title = "设置分类预算",
                    categoryId = category.id,
                    currentAmountFen = existing?.amount ?: 0
                )
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

@Composable
private fun MonthSwitcher(
    label: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Rounded.ChevronLeft, contentDescription = "上个月")
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(90.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Rounded.ChevronRight, contentDescription = "下个月")
        }
    }
}

@Composable
private fun StatsTab(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ---------- 概览 ----------

@Composable
private fun OverviewTab(
    summary: MonthSummary,
    categories: Map<Long, CategoryEntity>,
    symbol: String,
    monthLabel: String
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // 收支卡片
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCardExpense(
                label = "本月支出",
                value = MoneyFormatter.format(summary.expense, symbol),
                modifier = Modifier.weight(1f)
            )
            StatCardIncome(
                label = "本月收入",
                value = MoneyFormatter.format(summary.income, symbol),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))

        // 分类饼图
        SectionCard(Modifier.fillMaxWidth()) {
            Text("支出分类占比", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (summary.byCategory.isEmpty()) {
                EmptyState(text = "本月还没有支出记录")
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(
                        slices = summary.byCategory.map { sum ->
                            ChartSlice(
                                label = categories[sum.categoryId]?.name ?: "其他",
                                value = sum.amount.toFloat(),
                                color = Color(categories[sum.categoryId]?.color ?: 0xFFADB5BD)
                            )
                        },
                        modifier = Modifier.size(150.dp),
                        centerText = MoneyFormatter.format(summary.expense, symbol),
                        centerSubText = "总支出"
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        summary.byCategory.take(4).forEach { sum ->
                            val cat = categories[sum.categoryId]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(cat?.color ?: 0xFFADB5BD))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = cat?.name ?: "其他",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${(sum.amount * 100 / summary.expense.coerceAtLeast(1))}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                // 排行
                Text("分类排行", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                summary.byCategory.forEachIndexed { index, sum ->
                    val cat = categories[sum.categoryId]
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(20.dp)
                        )
                        CategoryAvatar(
                            icon = cat?.icon ?: "📦",
                            color = Color(cat?.color ?: 0xFFADB5BD),
                            size = 34.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = cat?.name ?: "其他",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            ProgressBar(
                                fraction = sum.amount.toFloat() / summary.expense.coerceAtLeast(1).toFloat(),
                                color = Color(cat?.color ?: 0xFFADB5BD)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = MoneyFormatter.format(sum.amount, symbol),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // 趋势
        SectionCard(Modifier.fillMaxWidth()) {
            Text("每日支出趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = monthLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            if (summary.dailyExpense.all { it == 0f }) {
                EmptyState(text = "这个月还没有支出")
            } else {
                TrendLineChart(
                    points = summary.dailyExpense,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatCardExpense(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ExpenseRed)
    }
}

@Composable
private fun StatCardIncome(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = IncomeGreen)
    }
}

// ---------- 预算 ----------

@Composable
private fun BudgetTab(
    periodLabel: String,
    periodExpense: Long,
    periodByCategory: Map<Long, Long>,
    monthlyBudget: BudgetEntity?,
    categoryBudgets: List<BudgetEntity>,
    categories: Map<Long, CategoryEntity>,
    symbol: String,
    monthStartDay: Int,
    onEditMonthly: () -> Unit,
    onEditCategory: (Long) -> Unit,
    onAddCategory: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // 周期说明
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🐾", fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "预算周期：$periodLabel（每月${monthStartDay}日起算）",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(10.dp))

        // 月预算
        SectionCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("月预算", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    if (monthlyBudget == null) {
                        Text(
                            "未设置",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "已用 ${MoneyFormatter.format(periodExpense, symbol)} / ${MoneyFormatter.format(monthlyBudget.amount, symbol)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                TextButton(onClick = onEditMonthly) {
                    Text(if (monthlyBudget == null) "设置" else "修改")
                }
            }
            monthlyBudget?.let { budget ->
                Spacer(Modifier.height(10.dp))
                val fraction = periodExpense.toFloat() / budget.amount.coerceAtLeast(1).toFloat()
                val over = periodExpense > budget.amount
                ProgressBar(
                    fraction = fraction,
                    color = if (over) ExpenseRed else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                if (over) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CatFace(
                            mood = CatMood.SAD,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "喵呜… 已超支 ${MoneyFormatter.format(periodExpense - budget.amount, symbol)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ExpenseRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = "剩余 ${MoneyFormatter.format(budget.amount - periodExpense, symbol)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // 分类预算
        SectionCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分类预算",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAddCategory) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("添加")
                }
            }
            if (categoryBudgets.isEmpty()) {
                EmptyState(text = "还没有分类预算，点击「添加」设置")
            } else {
                categoryBudgets.sortedByDescending { it.amount }.forEach { budget ->
                    val category = categories[budget.categoryId]
                    if (category != null) {
                        val spent = periodByCategory[budget.categoryId] ?: 0L
                        val over = spent > budget.amount
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onEditCategory(budget.categoryId!!) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryAvatar(
                                icon = category.icon,
                                color = Color(category.color),
                                size = 36.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${MoneyFormatter.format(spent, symbol)} / ${MoneyFormatter.format(budget.amount, symbol)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (over) ExpenseRed
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(Modifier.height(5.dp))
                                ProgressBar(
                                    fraction = spent.toFloat() / budget.amount.coerceAtLeast(1).toFloat(),
                                    color = if (over) ExpenseRed else Color(category.color)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** 简易进度条。 */
@Composable
fun ProgressBar(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val f = fraction.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            Modifier
                .fillMaxWidth(f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

@Composable
private fun BudgetEditDialog(
    target: BudgetEditTarget,
    symbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onClear: () -> Unit
) {
    var input by remember { mutableStateOf(MoneyFormatter.formatPlain(target.currentAmountFen).replace(",", "")) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(target.title) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { value ->
                    if (value.length <= 10 && value.all { it.isDigit() || it == '.' }) input = value
                },
                label = { Text("金额") },
                prefix = { Text(symbol) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                MoneyFormatter.parseToFen(input)?.let { onConfirm(it) }
            }) { Text("确定") }
        },
        dismissButton = {
            Row {
                if (target.categoryId != null && target.currentAmountFen > 0) {
                    TextButton(onClick = onClear) {
                        Text("清除预算", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("取消") }
            }
        }
    )
}

@Composable
private fun CategoryBudgetPicker(
    categories: List<CategoryEntity>,
    onPick: (CategoryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择分类") },
        text = {
            Column(
                Modifier
                    .height(320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                categories.distinctBy { it.id }.forEach { category ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(category) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryAvatar(
                            icon = category.icon,
                            color = Color(category.color),
                            size = 34.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
