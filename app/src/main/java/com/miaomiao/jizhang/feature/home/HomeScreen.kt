package com.miaomiao.jizhang.feature.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.common.MoneyFormatter
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import com.miaomiao.jizhang.core.ui.components.CatFace
import com.miaomiao.jizhang.core.ui.components.CatMood
import com.miaomiao.jizhang.core.ui.components.CategoryAvatar
import com.miaomiao.jizhang.core.ui.components.EmptyState
import com.miaomiao.jizhang.core.ui.components.MoneyText
import com.miaomiao.jizhang.core.ui.components.SectionCard
import com.miaomiao.jizhang.core.ui.components.moodFor
import com.miaomiao.jizhang.core.ui.theme.OrangeDeep
import com.miaomiao.jizhang.core.ui.theme.OrangePrimary
import java.time.LocalDate

/** 首页：今日概况 + 猫咪 + 最近账单。 */
@Composable
fun HomeScreen(
    onOpenAdd: () -> Unit,
    onOpenCat: () -> Unit,
    onEditTransaction: (Long) -> Unit
) {
    val vm: HomeViewModel = hiltViewModel()
    val summary by vm.todaySummary.collectAsStateWithLifecycle()
    val recent by vm.recentTransactions.collectAsStateWithLifecycle()
    val catState by vm.catState.collectAsStateWithLifecycle()
    val currency by vm.currencySymbol.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        HomeHeader()
        Spacer(Modifier.height(16.dp))

        SummaryCard(
            expense = summary.first,
            income = summary.second,
            symbol = currency,
            onOpenAdd = onOpenAdd
        )
        Spacer(Modifier.height(16.dp))

        CatCard(catState = catState, onClick = onOpenCat)
        Spacer(Modifier.height(16.dp))

        RecentSection(
            transactions = recent,
            categories = categories,
            accounts = accounts,
            symbol = currency,
            onEditTransaction = onEditTransaction,
            onOpenAdd = onOpenAdd
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HomeHeader() {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "喵喵记",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${LocalDate.now().monthValue}月${LocalDate.now().dayOfMonth}日 ${weekText(LocalDate.now())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(text = "🐱", fontSize = 26.sp)
    }
}

private fun weekText(date: LocalDate): String =
    arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")[(date.dayOfWeek.value + 6) % 7]

@Composable
private fun SummaryCard(
    expense: Long,
    income: Long,
    symbol: String,
    onOpenAdd: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(OrangePrimary, OrangeDeep)))
            .clickable(onClick = onOpenAdd)
            .padding(20.dp)
    ) {
        Text(
            text = "今日支出",
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            text = MoneyFormatter.format(expense, symbol),
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("今日收入", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                Text(
                    MoneyFormatter.format(income, symbol),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column(Modifier.weight(1f)) {
                Text("本月结余", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
                Text(
                    MoneyFormatter.format(income - expense, symbol),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "＋", color = Color.White, fontSize = 26.sp)
            }
        }
    }
}

@Composable
private fun CatCard(
    catState: CatStateEntity,
    onClick: () -> Unit
) {
    SectionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CatFace(
                mood = moodFor(
                    streakDays = catState.streakDays,
                    recordedToday = catState.lastRecordDate == DateUtils.todayStr(),
                    overBudget = false
                ),
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "我的小猫",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "🐟 小鱼干 ${catState.fishCount} · 🔥 连续 ${catState.streakDays} 天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentSection(
    transactions: List<TransactionEntity>,
    categories: Map<Long, com.miaomiao.jizhang.core.data.entity.CategoryEntity>,
    accounts: Map<Long, com.miaomiao.jizhang.core.data.entity.AccountEntity>,
    symbol: String,
    onEditTransaction: (Long) -> Unit,
    onOpenAdd: () -> Unit
) {
    SectionCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = "最近账单",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        if (transactions.isEmpty()) {
            EmptyState(text = "今天还没有账单，点击上方记一笔吧")
        } else {
            transactions.forEach { transaction ->
                val category = categories[transaction.categoryId]
                val account = accounts[transaction.accountId]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onEditTransaction(transaction.id) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryAvatar(
                        icon = category?.icon ?: "📦",
                        color = Color(category?.color ?: 0xFFADB5BD),
                        size = 40.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = category?.name ?: "已删除分类",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = buildString {
                                append(DateUtils.formatTime(transaction.dateTime))
                                account?.let { append(" · ${it.name}") }
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
                }
            }
        }
    }
}
