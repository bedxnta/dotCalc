package com.bedanta.dotcalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.bedanta.dotcalc.logic.AccentMode

private fun getGlyphColorScheme(accentMode: AccentMode) = darkColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = LightGray,
    onSecondary = Black,
    tertiary = if (accentMode == AccentMode.RED) RedAccent else White,
    background = Black,
    onBackground = White,
    surface = NearBlack,
    onSurface = White,
    outline = MediumGray
)

@Composable
fun DotCalcTheme(
    accentMode: AccentMode = AccentMode.RED,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = getGlyphColorScheme(accentMode),
        typography = Typography,
        content = content
    )
}
