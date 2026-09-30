package com.miaomiao.jizhang.feature.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.ui.components.CatFace
import com.miaomiao.jizhang.core.ui.components.SectionCard
import com.miaomiao.jizhang.core.ui.components.moodFor
import com.miaomiao.jizhang.feature.cat.CatViewModel
import androidx.compose.material.icons.rounded.Repeat

/** 我的：猫咪 + 管理入口。 */
@Composable
fun ProfileScreen(
    onOpenCat: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenRecurring: () -> Unit
) {
    val catVm: CatViewModel = hiltViewModel()
    val catState by catVm.catState.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.statusBarsPadding().height(10.dp))
        Text(
            text = "我的",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(14.dp))

        // 猫咪大卡
        SectionCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenCat)
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
                    modifier = Modifier.size(84.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "喵喵的小窝",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "🐟 ${catState.fishCount} · 🔥 ${catState.streakDays} 天 · 📒 ${catState.totalCount} 笔",
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
        Spacer(Modifier.height(14.dp))

        // 管理入口
        SectionCard(Modifier.fillMaxWidth()) {
            EntryRow(Icons.Rounded.Category, "分类管理", "自定义支出/收入分类") { onOpenCategories() }
            EntryDivider()
            EntryRow(Icons.Rounded.Wallet, "账户管理", "现金、微信、支付宝、银行卡") { onOpenAccounts() }
            EntryDivider()
            EntryRow(Icons.Rounded.Repeat, "周期账单", "每天/每周/每月自动记账") { onOpenRecurring() }
            EntryDivider()
            EntryRow(Icons.Rounded.Pets, "猫咪系统", "小鱼干、成就、动画开关") { onOpenCat() }
            EntryDivider()
            EntryRow(Icons.Rounded.Settings, "设置", "主题、货币、数据备份") { onOpenSettings() }
        }
        Spacer(Modifier.height(20.dp))

        Text(
            text = "喵喵记 v1.0.0",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun EntryRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
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

@Composable
private fun EntryDivider() {
    Spacer(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}
