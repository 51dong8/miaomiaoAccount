package com.miaomiao.jizhang.feature.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.miaomiao.jizhang.core.common.MoneyFormatter
import com.miaomiao.jizhang.core.common.RecurringLogic
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.RecurringRuleEntity
import com.miaomiao.jizhang.core.ui.components.CategoryAvatar
import com.miaomiao.jizhang.core.ui.components.EmptyState

/** 周期账单管理页。 */
@Composable
fun RecurringManageScreen(
    onBack: () -> Unit
) {
    val vm: RecurringViewModel = hiltViewModel()
    val rules by vm.rules.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val symbol by vm.currencySymbol.collectAsStateWithLifecycle()

    var deleteTarget by remember { mutableStateOf<RecurringRuleEntity?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "周期账单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "记一笔时选择「每天/每周/每月」即可创建周期规则；每天打开应用或系统定时任务会自动补记到期账单。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(10.dp))

        if (rules.isEmpty()) {
            EmptyState(text = "还没有周期账单规则\n记一笔时选择周期即可创建")
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rules, key = { it.id }) { rule ->
                    val category = categories[rule.categoryId]
                    val account = accounts[rule.accountId]
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                fontWeight = FontWeight.Medium,
                                color = if (rule.active) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = buildString {
                                    append(RecurringLogic.describe(rule.frequency, rule.dayOfWeek, rule.dayOfMonth))
                                    account?.let { append(" · ${it.name}") }
                                    if (rule.note.isNotBlank()) append(" · ${rule.note}")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = (if (rule.type == TxType.EXPENSE) "-" else "+") +
                                MoneyFormatter.formatPlain(rule.amount),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (rule.type == TxType.EXPENSE) {
                                MaterialTheme.colorScheme.error
                            } else {
                                Color(0xFF27AE60)
                            }
                        )
                        Spacer(Modifier.width(6.dp))
                        Switch(
                            checked = rule.active,
                            onCheckedChange = { vm.setActive(rule, it) }
                        )
                        IconButton(onClick = { deleteTarget = rule }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    deleteTarget?.let { rule ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("删除这条周期规则？") },
            text = { Text("删除后不再自动记账，已生成的账单不受影响。") },
            confirmButton = {
                TextButton(onClick = {
                    vm.delete(rule)
                    deleteTarget = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            }
        )
    }
}
