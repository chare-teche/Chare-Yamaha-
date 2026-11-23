package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EthioHardwareColorScheme = darkColorScheme(
    primary = LcdHeaderMagenta,
    onPrimary = Color.White,
    primaryContainer = LcdHeaderMagentaDark,
    onPrimaryContainer = Color.White,
    secondary = LcdAmberBright,
    onSecondary = ChassisVoid,
    secondaryContainer = LcdAmberHighlight,
    onSecondaryContainer = Color.White,
    tertiary = LedBlue,
    onTertiary = ChassisVoid,
    background = ChassisVoid,
    onBackground = SilkscreenWhite,
    surface = ChassisPanel,
    onSurface = SilkscreenWhite,
    surfaceVariant = ChassisSurface,
    onSurfaceVariant = SilkscreenMuted,
    outline = ChassisBorder,
    outlineVariant = ChassisDivider,
    error = LedCrimson,
    onError = Color.White
)

@Composable
fun EthioYamahaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EthioHardwareColorScheme,
        typography = EthioTypography,
        content = content
    )
}
