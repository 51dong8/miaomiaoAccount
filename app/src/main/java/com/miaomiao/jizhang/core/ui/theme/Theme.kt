package com.miaomiao.jizhang.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.miaomiao.jizhang.core.data.repository.SettingsRepository

private val LightColors = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    primaryContainer = OrangeSoft,
    onPrimaryContainer = Color(0xFF5C2E00),
    secondary = Color(0xFFB9774A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE8D6),
    onSecondaryContainer = Color(0xFF4A2A12),
    background = CreamBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceWarm,
    onSurfaceVariant = TextSecondary,
    outline = DividerWarm,
    outlineVariant = DividerWarm,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF5C0E08)
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF4A2400),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = Color(0xFFE8B98D),
    onSecondary = Color(0xFF4A2A12),
    secondaryContainer = Color(0xFF6A4423),
    onSecondaryContainer = Color(0xFFFFE8D6),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkDivider,
    outlineVariant = DarkDivider,
    error = Color(0xFFFF8A80),
    onError = Color(0xFF4A0A05),
    errorContainer = Color(0xFF6B1410),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun MiaoMiaoTheme(
    themeMode: String = SettingsRepository.THEME_SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        SettingsRepository.THEME_LIGHT -> false
        SettingsRepository.THEME_DARK -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
