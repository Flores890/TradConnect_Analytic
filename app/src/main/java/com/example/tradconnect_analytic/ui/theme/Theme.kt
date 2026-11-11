package com.example.tradconnect_analytic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF78B8A9),
    onPrimary = Color(0xFF10231F),
    primaryContainer = Color(0xFF294B43),
    onPrimaryContainer = Color(0xFFD0E9E2),
    secondary = Color(0xFFAAA0CC),
    onSecondary = Color(0xFF29233D),
    secondaryContainer = Color(0xFF403852),
    onSecondaryContainer = Color(0xFFE4DDF2),
    tertiary = Color(0xFFD3AA70),
    onTertiary = Color(0xFF362714),
    tertiaryContainer = Color(0xFF53412A),
    onTertiaryContainer = Color(0xFFF1DFC5),
    error = Color(0xFFE49A91),
    onError = Color(0xFF341512),
    errorContainer = Color(0xFF5B2823),
    onErrorContainer = Color(0xFFF5D4D0),
    background = Color(0xFF151719),
    onBackground = Color(0xFFE8E9E8),
    surface = Color(0xFF202326),
    onSurface = Color(0xFFE8E9E8),
    surfaceVariant = Color(0xFF2B3033),
    onSurfaceVariant = Color(0xFFC1C7C6),
    outline = Color(0xFF89928F),
    outlineVariant = Color(0xFF414846)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF287F70),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9EEE8),
    onPrimaryContainer = Color(0xFF173D35),
    secondary = Color(0xFF536F82),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1EAF0),
    onSecondaryContainer = Color(0xFF263D4B),
    tertiary = Color(0xFF9A713A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF3E8D8),
    onTertiaryContainer = Color(0xFF594324),
    error = Color(0xFFB14C43),
    onError = Color.White,
    errorContainer = Color(0xFFF6E1DE),
    onErrorContainer = Color(0xFF652B26),
    background = Color(0xFFF4F6F7),
    onBackground = Color(0xFF1D2529),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1D2529),
    surfaceVariant = Color(0xFFE8ECEE),
    onSurfaceVariant = Color(0xFF4D5A60),
    outline = Color(0xFF7C898E),
    outlineVariant = Color(0xFFD5DCDF)
)

@Composable
fun TradConnect_AnalyticTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
