package com.miaomiao.jizhang.feature.records

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import com.miaomiao.jizhang.core.ui.components.CategoryAvatar
import com.miaomiao.jizhang.core.ui.components.EmptyState
import com.miaomiao.jizhang.core.ui.components.MoneyText

/** 账单列表：按日分组、搜索、类型筛选、点击编辑、复制再记。 */
@Composable
fun RecordsScreen(
    onEditTransaction: (Long) -> Unit,
    onCopyTransaction: (Long) -> Unit
) {
    val vm: RecordsViewModel = hiltViewModel()
    val grouped by vm.grouped.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val currency by vm.currencySymbol.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.statusBarsPadding().height(10.dp))
            Text(
                text = "账单",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = vm.keyword,
                onValueChange = vm::updateKeyword,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索备注…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(10.dp))
            FilterChips(
                selected = vm.typeFilter,
                onSelect = vm::updateTypeFilter
            )
            Spacer(Modifier.height(6.dp))
        }

        if (grouped.isEmpty()) {
            EmptyState(
                text = "这里空空如也，去记一笔吧",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(grouped, key = { it.date.toString() }) { group ->
                    Column {
                        DayHeader(group)
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            group.transactions.forEach { transaction ->
                                RecordItem(
                                    transaction = transaction,
                                    categoryName = categories[transaction.categoryId]?.name ?: "已删除分类",
                                    categoryIcon = categories[transaction.categoryId]?.icon ?: "📦",
                                    categoryColor = Color(categories[transaction.categoryId]?.color ?: 0xFFADB5BD),
                                    accountName = accounts[transaction.accountId]?.name,
                                    symbol = currency,
                                    onClick = { onEditTransaction(transaction.id) },
                                    onCopy = { onCopyTransaction(transaction.id) }
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun FilterChips(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip("全部", active = selected == null) { onSelect(null) }
        FilterChip("支出", active = selected == TxType.EXPENSE) { onSelect(TxType.EXPENSE) }
        FilterChip("收入", active = selected == TxType.INCOME) { onSelect(TxType.INCOME) }
    }
}

@Composable
private fun FilterChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (active) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun DayHeader(group: DayGroup) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = DateUtils.formatDayHeader(group.transactions.first().dateTime),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (group.expense > 0) {
                Text(
                    text = "支 ${com.miaomiao.jizhang.core.common.MoneyFormatter.format(group.expense)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (group.expense > 0 && group.income > 0) {
                Spacer(Modifier.width(8.dp))
            }
            if (group.income > 0) {
                Text(
                    text = "收 ${com.miaomiao.jizhang.core.common.MoneyFormatter.format(group.income)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = com.miaomiao.jizhang.core.ui.theme.IncomeGreen
                )
            }
        }
    }
}

@Composable
private fun RecordItem(
    transaction: TransactionEntity,
    categoryName: String,
    categoryIcon: String,
    categoryColor: Color,
    accountName: String?,
    symbol: String,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryAvatar(icon = categoryIcon, color = categoryColor, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = buildString {
                    append(DateUtils.formatTime(transaction.dateTime))
                    accountName?.let { append(" · $it") }
                    if (transaction.note.isNotBlank()) append(" · ${transaction.note}")
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        MoneyText(
            amountFen = transaction.amount,
            isExpense = transaction.type == TxType.EXPENSE,
            symbol = symbol,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
            Icon(
                imageVector = Icons.Rounded.ContentCopy,
                contentDescription = "复制再记",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
