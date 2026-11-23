package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EthioHardwareColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = GraphiteVoid,
    primaryContainer = ChampagneGoldContainer,
    onPrimaryContainer = ChampagneGoldBright,
    secondary = ElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = ElectricBlueGlow,
    tertiary = StatusLiveGreen,
    onTertiary = GraphiteVoid,
    background = GraphiteVoid,
    onBackground = TextPrimary,
    surface = GraphitePanel,
    onSurface = TextPrimary,
    surfaceVariant = GraphiteElevated,
    onSurfaceVariant = TextSecondary,
    outline = GraphiteBorder,
    outlineVariant = ChassisDivider,
    error = StatusAlertRed,
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
