package com.miaomiao.jizhang.core.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miaomiao.jizhang.core.ui.theme.OrangeDeep
import com.miaomiao.jizhang.core.ui.theme.OrangePrimary
import com.miaomiao.jizhang.feature.home.HomeScreen
import com.miaomiao.jizhang.feature.profile.ProfileScreen
import com.miaomiao.jizhang.feature.records.RecordsScreen
import com.miaomiao.jizhang.feature.stats.StatsScreen

/** 主框架：4 个 Tab + 中央记账 FAB。 */
@Composable
fun MainScaffold(
    onOpenAdd: () -> Unit,
    onOpenCat: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCategories: () -> Unit,
    onOpenAccounts: () -> Unit,
    onEditTransaction: (Long) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MainBottomBar(
                selected = selectedTab,
                onSelect = { selectedTab = it },
                onAddClick = onOpenAdd
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    onOpenAdd = onOpenAdd,
                    onOpenCat = onOpenCat,
                    onEditTransaction = onEditTransaction
                )
                1 -> RecordsScreen(onEditTransaction = onEditTransaction)
                2 -> StatsScreen()
                else -> ProfileScreen(
                    onOpenCat = onOpenCat,
                    onOpenSettings = onOpenSettings,
                    onOpenCategories = onOpenCategories,
                    onOpenAccounts = onOpenAccounts
                )
            }
        }
    }
}

private val TabItems = listOf(
    Triple(0, Icons.Rounded.Home, "首页"),
    Triple(1, Icons.Rounded.ReceiptLong, "账单"),
    Triple(2, Icons.Rounded.PieChart, "统计"),
    Triple(3, Icons.Rounded.Person, "我的")
)

@Composable
private fun MainBottomBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth()) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabItems.take(2).forEach { (index, icon, label) ->
                    BottomBarItem(
                        index = index,
                        icon = icon,
                        label = label,
                        selected = selected,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f)
                    )
                }
                // 中间留给 FAB 的空间
                Spacer(Modifier.weight(1f))
                TabItems.drop(2).forEach { (index, icon, label) ->
                    BottomBarItem(
                        index = index,
                        icon = icon,
                        label = label,
                        selected = selected,
                        onSelect = onSelect,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        // 中央凸出 FAB
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-26).dp)
        ) {
            FloatingActionButton(
                onClick = onAddClick,
                shape = CircleShape,
                containerColor = OrangePrimary,
                contentColor = Color.White,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "记一笔",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }
        }
        // FAB 底部小高光
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 36.dp)
                .size(18.dp, 6.dp)
                .background(OrangeDeep.copy(alpha = 0.35f), RoundedCornerShape(50))
        )
    }
}

@Composable
private fun BottomBarItem(
    index: Int,
    icon: ImageVector,
    label: String,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val active = selected == index
    val color = if (active) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onSelect(index) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
