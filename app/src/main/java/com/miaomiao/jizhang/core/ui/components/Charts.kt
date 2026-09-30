package com.miaomiao.jizhang.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** 环形图数据切片。 */
data class ChartSlice(
    val label: String,
    val value: Float,
    val color: Color
)

/**
 * 环形图（甜甜圈）。中间可叠加文本。
 */
@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    modifier: Modifier = Modifier,
    centerText: String? = null,
    centerSubText: String? = null,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            if (total <= 0f) {
                // 空数据：画占位环
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = size.minDimension * 0.13f, cap = StrokeCap.Butt)
                )
                return@Canvas
            }
            val strokeWidth = size.minDimension * 0.13f
            val gap = 2.5f
            var startAngle = -90f
            slices.forEach { slice ->
                val sweep = slice.value / total * 360f
                val effectiveSweep = if (sweep > 0.5f) sweep - gap else sweep
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = effectiveSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep
            }
        }
        if (centerText != null || centerSubText != null) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                centerText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                centerSubText?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 趋势折线图：渐变面积 + 折线 + 数据点。
 * [points] 为空或不足 2 个点时返回空画布。
 */
@Composable
fun TrendLineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillAlpha: Float = 0.22f
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val padX = w * 0.02f
        val padTop = h * 0.08f
        val padBottom = h * 0.06f

        val max = points.maxOrNull() ?: return@Canvas
        val min = points.minOrNull() ?: return@Canvas
        val range = (max - min).coerceAtLeast(0.01f)

        val stepX = (w - padX * 2) / (points.size - 1)
        val ys = points.map { v ->
            padTop + (1f - (v - min) / range) * (h - padTop - padBottom)
        }

        // 渐变填充
        val linePath = Path().apply {
            points.forEachIndexed { i, _ ->
                val x = padX + i * stepX
                if (i == 0) moveTo(x, ys[i]) else lineTo(x, ys[i])
            }
        }
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(padX + (points.size - 1) * stepX, h)
            lineTo(padX, h)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = fillAlpha), lineColor.copy(alpha = 0f))
            )
        )

        // 折线
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
        )

        // 数据点
        points.forEachIndexed { i, _ ->
            drawCircle(
                color = lineColor,
                radius = if (i == points.lastIndex) 7f else 4.5f,
                center = androidx.compose.ui.geometry.Offset(padX + i * stepX, ys[i])
            )
            if (i == points.lastIndex) {
                drawCircle(
                    color = Color.White,
                    radius = 2.6f,
                    center = androidx.compose.ui.geometry.Offset(padX + i * stepX, ys[i])
                )
            }
        }
    }
}
