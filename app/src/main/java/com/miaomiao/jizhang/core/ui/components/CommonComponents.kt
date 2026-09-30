package com.miaomiao.jizhang.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miaomiao.jizhang.core.common.MoneyFormatter
import com.miaomiao.jizhang.core.ui.theme.IncomeGreen
import com.miaomiao.jizhang.core.ui.theme.OrangeDeep
import com.miaomiao.jizhang.core.ui.theme.OrangePrimary

/** 圆角卡片容器。 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .padding(16.dp),
        content = content
    )
}

/** 空状态：猫咪 + 提示文案。 */
@Composable
fun EmptyState(
    text: String,
    modifier: Modifier = Modifier,
    mood: CatMood = CatMood.SLEEPY,
    catSize: Dp = 96.dp
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CatFace(mood = mood, modifier = Modifier.size(catSize))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** 圆形彩色底 + emoji 的分类/账户图标。 */
@Composable
fun CategoryAvatar(
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = (size.value * 0.44f).sp)
    }
}

/** 渐变主按钮。 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(18.dp)
    val contentAlpha = if (enabled) 1f else 0.5f
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(listOf(OrangePrimary, OrangeDeep))
            )
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = contentAlpha),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** 金额文本：支出红 / 收入绿。 */
@Composable
fun MoneyText(
    amountFen: Long,
    isExpense: Boolean,
    modifier: Modifier = Modifier,
    symbol: String = "¥",
    style: TextStyle = MaterialTheme.typography.titleMedium,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val color = if (isExpense) MaterialTheme.colorScheme.error else IncomeGreen
    val sign = if (isExpense) "-" else "+"
    Text(
        text = sign + MoneyFormatter.format(amountFen, symbol),
        modifier = modifier,
        color = color,
        style = style,
        fontWeight = fontWeight
    )
}

/** 横向统计小卡。 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Text(text = it, fontSize = 14.sp)
                Box(Modifier.size(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
