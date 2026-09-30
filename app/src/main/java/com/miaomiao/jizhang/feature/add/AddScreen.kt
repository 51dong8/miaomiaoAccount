package com.miaomiao.jizhang.feature.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miaomiao.jizhang.core.common.RecurringFrequency
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import com.miaomiao.jizhang.core.ui.components.CatFace
import com.miaomiao.jizhang.core.ui.components.CatMood
import com.miaomiao.jizhang.core.ui.components.GradientButton
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** 记一笔（新增 / 编辑）。 */
@Composable
fun AddScreen(
    onBack: () -> Unit
) {
    val vm: AddViewModel = hiltViewModel()
    val activeAccounts by vm.activeAccounts.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            // 顶栏
            AddTopBar(
                title = if (vm.editingId != null) "编辑账单" else "记一笔",
                onBack = onBack,
                onDelete = if (vm.editingId != null) {
                    { showDeleteDialog = true }
                } else {
                    null
                }
            )

            // 可滚动编辑区
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                TypeSwitch(isExpense = vm.isExpense, onChange = vm::switchType)

                Spacer(Modifier.height(18.dp))
                AmountDisplay(
                    amountInput = vm.amountInput,
                    date = vm.selectedDate,
                    onDateClick = { showDatePicker = true }
                )

                Spacer(Modifier.height(20.dp))
                Text("分类", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                CategoryGrid(
                    categories = vm.currentCategories,
                    selected = vm.selectedCategory,
                    onSelect = vm::selectCategory
                )

                Spacer(Modifier.height(18.dp))
                Text("账户", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                AccountRow(
                    accounts = activeAccounts,
                    selected = vm.selectedAccount,
                    onSelect = vm::selectAccount
                )

                Spacer(Modifier.height(18.dp))
                OutlinedTextField(
                    value = vm.note,
                    onValueChange = vm::updateNote,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注（选填）") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // 周期（仅新增时显示）
                if (vm.editingId == null) {
                    Spacer(Modifier.height(18.dp))
                    Text("周期", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    FrequencySelector(
                        frequency = vm.frequency,
                        weeklyDay = vm.weeklyDay,
                        monthlyDay = vm.monthlyDay,
                        onFrequency = vm::updateFrequency,
                        onWeeklyDay = vm::updateWeeklyDay,
                        onMonthlyDay = vm::updateMonthlyDay
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // 键盘 + 保存
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                NumberPad(onKey = vm::onKeyPress)
                Spacer(Modifier.height(10.dp))
                GradientButton(
                    text = if (vm.editingId != null) "保存修改" else "保存",
                    onClick = { vm.save(onSaved = onBack) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = vm.canSave && !vm.isSaving
                )
                Spacer(Modifier.height(8.dp))
                Spacer(Modifier.navigationBarsPadding().height(4.dp))
            }
        }

        // 保存成功动画
        if (vm.showSaveSuccess) {
            vm.lastCatState?.let { state ->
                SaveSuccessOverlay(
                    catState = state,
                    onFinished = { vm.consumeSuccess(onFinished = onBack) }
                )
            }
        }
    }

    // 日期选择
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = vm.selectedDate
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        vm.setDate(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    // 删除确认
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除这笔账单？") },
            text = { Text("删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    vm.delete(onDeleted = onBack)
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun AddTopBar(
    title: String,
    onBack: () -> Unit,
    onDelete: (() -> Unit)?
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
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun TypeSwitch(
    isExpense: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        SwitchItem("支出", active = isExpense, Modifier.weight(1f)) { onChange(true) }
        SwitchItem("收入", active = !isExpense, Modifier.weight(1f)) { onChange(false) }
    }
}

@Composable
private fun SwitchItem(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(38.dp)
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

@Composable
private fun AmountDisplay(
    amountInput: String,
    date: LocalDate,
    onDateClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "¥",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = if (amountInput.isEmpty()) "0.00" else amountInput,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Surface(
            onClick = onDateClick,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${date.monthValue}月${date.dayOfMonth}日",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<CategoryEntity>,
    selected: CategoryEntity?,
    onSelect: (CategoryEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.chunked(5).forEach { rowCategories ->
            Row(Modifier.fillMaxWidth()) {
                rowCategories.forEach { category ->
                    CategoryItem(
                        category = category,
                        selected = selected?.id == category.id,
                        onClick = { onSelect(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(5 - rowCategories.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: CategoryEntity,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    if (selected) Color(category.color).copy(alpha = 0.22f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .then(
                    if (selected) {
                        Modifier.border(2.dp, Color(category.color), CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.icon, fontSize = 22.sp)
        }
        Text(
            text = category.name,
            fontSize = 12.sp,
            maxLines = 1,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun AccountRow(
    accounts: List<AccountEntity>,
    selected: AccountEntity?,
    onSelect: (AccountEntity) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        accounts.forEach { account ->
            val active = selected?.id == account.id
            Surface(
                onClick = { onSelect(account) },
                shape = RoundedCornerShape(50),
                color = if (active) Color(account.color).copy(alpha = 0.18f)
                else MaterialTheme.colorScheme.surfaceVariant,
                border = if (active) {
                    androidx.compose.foundation.BorderStroke(1.5.dp, Color(account.color))
                } else {
                    null
                }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = account.icon, fontSize = 15.sp)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/** 保存成功全屏动画：猫咪 + 小鱼干。 */
@Composable
private fun FrequencySelector(
    frequency: String,
    weeklyDay: Int,
    monthlyDay: Int,
    onFrequency: (String) -> Unit,
    onWeeklyDay: (Int) -> Unit,
    onMonthlyDay: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            FreqChip("仅一次", frequency == RecurringFrequency.NONE, Modifier.weight(1f)) {
                onFrequency(RecurringFrequency.NONE)
            }
            FreqChip("每天", frequency == RecurringFrequency.DAILY, Modifier.weight(1f)) {
                onFrequency(RecurringFrequency.DAILY)
            }
            FreqChip("每周", frequency == RecurringFrequency.WEEKLY, Modifier.weight(1f)) {
                onFrequency(RecurringFrequency.WEEKLY)
            }
            FreqChip("每月", frequency == RecurringFrequency.MONTHLY, Modifier.weight(1f)) {
                onFrequency(RecurringFrequency.MONTHLY)
            }
        }

        if (frequency == RecurringFrequency.WEEKLY) {
            val names = arrayOf("一", "二", "三", "四", "五", "六", "日")
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..7).forEach { d ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (d == weeklyDay) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onWeeklyDay(d) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = names[d - 1],
                            fontSize = 12.sp,
                            color = if (d == weeklyDay) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (d == weeklyDay) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        if (frequency == RecurringFrequency.MONTHLY) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..31).forEach { d ->
                    Box(
                        Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (d == monthlyDay) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onMonthlyDay(d) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$d",
                            fontSize = 12.sp,
                            color = if (d == monthlyDay) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (d == monthlyDay) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreqChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (active) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/** 保存成功全屏动画：猫咪 + 小鱼干，带明显跳过按钮。 */
@Composable
private fun SaveSuccessOverlay(
    catState: CatStateEntity,
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.7f)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CatFace(
                    mood = if (catState.streakDays >= 7) CatMood.PROUD else CatMood.HAPPY,
                    modifier = Modifier.size(150.dp),
                    animate = true
                )
                Text(
                    text = "记账成功！小鱼干 +1 🐟",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (catState.streakDays >= 7) {
                        "已连续记账 ${catState.streakDays} 天，太棒啦！"
                    } else {
                        "连续记账 ${catState.streakDays} 天"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable(onClick = onFinished)
                        .padding(horizontal = 28.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "知道了，跳过",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
