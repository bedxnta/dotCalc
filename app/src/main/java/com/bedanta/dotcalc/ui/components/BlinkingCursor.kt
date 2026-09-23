package com.bedanta.dotcalc.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CursorStyle {
    LINE,  // Standard vertical bar
    BLOCK  // Solid square (Programmer style)
}

@Composable
fun BlinkingCursor(
    height: Dp = 28.dp,
    width: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.tertiary,
    style: CursorStyle = CursorStyle.LINE,
    reduceAnimations: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (reduceAnimations) 1f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (reduceAnimations) 1000 else 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val actualWidth = if (style == CursorStyle.BLOCK) 10.dp else width
    val actualHeight = if (style == CursorStyle.BLOCK) 18.dp else height

    Box(
        modifier = modifier
            .height(actualHeight)
            .width(actualWidth)
            .background(color.copy(alpha = alpha))
    )
}
