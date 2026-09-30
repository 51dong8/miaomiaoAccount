package com.miaomiao.jizhang.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.min

/** 猫咪表情。 */
enum class CatMood {
    /** 困（今天还没记账） */
    SLEEPY,

    /** 开心（默认） */
    HAPPY,

    /** 得意（连续记账 ≥ 7 天） */
    PROUD,

    /** 难过（预算超支） */
    SAD
}

/** 根据连续天数与是否已记账推断表情。 */
fun moodFor(streakDays: Int, recordedToday: Boolean, overBudget: Boolean): CatMood = when {
    overBudget -> CatMood.SAD
    !recordedToday && streakDays < 2 -> CatMood.SLEEPY
    streakDays >= 7 -> CatMood.PROUD
    else -> CatMood.HAPPY
}

/**
 * 喵喵记的 IP 猫咪：Canvas 手绘猫脸。
 * @param animate 是否播放呼吸动画（默认 false，列表项建议关闭）
 */
@Composable
fun CatFace(
    mood: CatMood = CatMood.HAPPY,
    modifier: Modifier = Modifier,
    animate: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "catBreath")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    Canvas(
        modifier = modifier.scale(if (animate) scale else 1f)
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.56f
        val r = min(w, h) * 0.34f

        val faceColor = Color(0xFFFFB878)
        val earInnerColor = Color(0xFFFF8FA3)
        val lineColor = Color(0xFF8A5A2B)
        val blushColor = Color(0x55FF8A8A)

        // ---- 耳朵 ----
        val leftEar = Path().apply {
            moveTo(cx - r * 0.95f, cy - r * 0.70f)
            lineTo(cx - r * 0.55f, cy - r * 1.12f)
            lineTo(cx - r * 0.10f, cy - r * 0.72f)
            close()
        }
        val rightEar = Path().apply {
            moveTo(cx + r * 0.95f, cy - r * 0.70f)
            lineTo(cx + r * 0.55f, cy - r * 1.12f)
            lineTo(cx + r * 0.10f, cy - r * 0.72f)
            close()
        }
        drawPath(leftEar, faceColor)
        drawPath(rightEar, faceColor)

        val leftEarInner = Path().apply {
            moveTo(cx - r * 0.72f, cy - r * 0.72f)
            lineTo(cx - r * 0.55f, cy - r * 0.96f)
            lineTo(cx - r * 0.32f, cy - r * 0.74f)
            close()
        }
        val rightEarInner = Path().apply {
            moveTo(cx + r * 0.72f, cy - r * 0.72f)
            lineTo(cx + r * 0.55f, cy - r * 0.96f)
            lineTo(cx + r * 0.32f, cy - r * 0.74f)
            close()
        }
        drawPath(leftEarInner, earInnerColor)
        drawPath(rightEarInner, earInnerColor)

        // ---- 头 ----
        drawCircle(faceColor, radius = r, center = Offset(cx, cy))
        // 头顶两撮毛
        val hair = Path().apply {
            moveTo(cx - r * 0.12f, cy - r * 0.97f)
            quadraticBezierTo(cx - r * 0.18f, cy - r * 1.18f, cx - r * 0.02f, cy - r * 1.16f)
            quadraticBezierTo(cx + r * 0.08f, cy - r * 1.20f, cx + r * 0.12f, cy - r * 0.97f)
            close()
        }
        drawPath(hair, Color(0xFFE8A465))

        // ---- 眼睛 ----
        val eyeY = cy - r * 0.12f
        val eyeDx = r * 0.30f
        val eyeR = r * 0.075f
        when (mood) {
            CatMood.SLEEPY -> {
                // 闭眼（向下的弧线）
                val arcStroke = Stroke(width = r * 0.055f, cap = StrokeCap.Round)
                drawArc(
                    color = lineColor,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - eyeDx - eyeR, eyeY - eyeR),
                    size = Size(eyeR * 2f, eyeR * 2f),
                    style = arcStroke
                )
                drawArc(
                    color = lineColor,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx + eyeDx - eyeR, eyeY - eyeR),
                    size = Size(eyeR * 2f, eyeR * 2f),
                    style = arcStroke
                )
            }
            CatMood.HAPPY -> {
                // 弯弯的笑眼
                val arcStroke = Stroke(width = r * 0.07f, cap = StrokeCap.Round)
                drawArc(
                    color = lineColor,
                    startAngle = 20f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx - eyeDx - eyeR, eyeY - eyeR),
                    size = Size(eyeR * 2f, eyeR * 2f),
                    style = arcStroke
                )
                drawArc(
                    color = lineColor,
                    startAngle = 20f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(cx + eyeDx - eyeR, eyeY - eyeR),
                    size = Size(eyeR * 2f, eyeR * 2f),
                    style = arcStroke
                )
            }
            CatMood.PROUD -> {
                // 圆眼 + 高光（得意）
                drawCircle(lineColor, radius = eyeR, center = Offset(cx - eyeDx, eyeY))
                drawCircle(lineColor, radius = eyeR, center = Offset(cx + eyeDx, eyeY))
                drawCircle(Color.White, radius = eyeR * 0.32f, center = Offset(cx - eyeDx - eyeR * 0.3f, eyeY - eyeR * 0.3f))
                drawCircle(Color.White, radius = eyeR * 0.32f, center = Offset(cx + eyeDx - eyeR * 0.3f, eyeY - eyeR * 0.3f))
            }
            CatMood.SAD -> {
                // 下垂眼（半圆朝下）
                drawArc(
                    color = lineColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - eyeDx - eyeR, eyeY - eyeR),
                    size = Size(eyeR * 2f, eyeR * 2f),
                    style = Stroke(width = r * 0.07f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = lineColor,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx + eyeDx - eyeR, eyeY - eyeR),
                    size = Size(eyeR * 2f, eyeR * 2f),
                    style = Stroke(width = r * 0.07f, cap = StrokeCap.Round)
                )
            }
        }

        // ---- 鼻子 ----
        val noseY = cy + r * 0.06f
        val nose = Path().apply {
            moveTo(cx - r * 0.06f, noseY)
            lineTo(cx, noseY - r * 0.09f)
            lineTo(cx + r * 0.06f, noseY)
            lineTo(cx, noseY + r * 0.05f)
            close()
        }
        drawPath(nose, Color(0xFFF08383))

        // ---- 嘴 ----
        val mouthStroke = Stroke(width = r * 0.045f, cap = StrokeCap.Round)
        when (mood) {
            CatMood.HAPPY -> {
                val mouth = Path().apply {
                    moveTo(cx - r * 0.18f, noseY + r * 0.08f)
                    quadraticBezierTo(cx, noseY + r * 0.38f, cx + r * 0.18f, noseY + r * 0.08f)
                }
                drawPath(mouth, lineColor, style = mouthStroke)
            }
            CatMood.PROUD -> {
                val mouthL = Path().apply {
                    moveTo(cx - r * 0.22f, noseY + r * 0.08f)
                    quadraticBezierTo(cx - r * 0.16f, noseY + r * 0.30f, cx, noseY + r * 0.20f)
                }
                val mouthR = Path().apply {
                    moveTo(cx + r * 0.22f, noseY + r * 0.08f)
                    quadraticBezierTo(cx + r * 0.16f, noseY + r * 0.30f, cx, noseY + r * 0.20f)
                }
                drawPath(mouthL, lineColor, style = mouthStroke)
                drawPath(mouthR, lineColor, style = mouthStroke)
            }
            CatMood.SLEEPY -> {
                drawCircle(lineColor, radius = r * 0.035f, center = Offset(cx, noseY + r * 0.12f))
            }
            CatMood.SAD -> {
                val mouth = Path().apply {
                    moveTo(cx - r * 0.18f, noseY + r * 0.28f)
                    quadraticBezierTo(cx, noseY + r * 0.06f, cx + r * 0.18f, noseY + r * 0.28f)
                }
                drawPath(mouth, lineColor, style = mouthStroke)
            }
        }

        // ---- 胡须 ----
        val whiskerStroke = Stroke(width = r * 0.03f, cap = StrokeCap.Round)
        val wY = cy + r * 0.12f
        val leftW1 = Path().apply {
            moveTo(cx - r * 0.62f, wY - r * 0.12f)
            lineTo(cx - r * 1.02f, wY - r * 0.22f)
        }
        val leftW2 = Path().apply {
            moveTo(cx - r * 0.62f, wY + r * 0.02f)
            lineTo(cx - r * 1.02f, wY + r * 0.10f)
        }
        val rightW1 = Path().apply {
            moveTo(cx + r * 0.62f, wY - r * 0.12f)
            lineTo(cx + r * 1.02f, wY - r * 0.22f)
        }
        val rightW2 = Path().apply {
            moveTo(cx + r * 0.62f, wY + r * 0.02f)
            lineTo(cx + r * 1.02f, wY + r * 0.10f)
        }
        drawPath(leftW1, lineColor.copy(alpha = 0.45f), style = whiskerStroke)
        drawPath(leftW2, lineColor.copy(alpha = 0.45f), style = whiskerStroke)
        drawPath(rightW1, lineColor.copy(alpha = 0.45f), style = whiskerStroke)
        drawPath(rightW2, lineColor.copy(alpha = 0.45f), style = whiskerStroke)

        // ---- 腮红 ----
        drawCircle(blushColor, radius = r * 0.12f, center = Offset(cx - r * 0.56f, cy + r * 0.26f))
        drawCircle(blushColor, radius = r * 0.12f, center = Offset(cx + r * 0.56f, cy + r * 0.26f))
    }
}
