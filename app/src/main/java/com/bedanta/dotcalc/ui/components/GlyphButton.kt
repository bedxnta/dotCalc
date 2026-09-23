package com.bedanta.dotcalc.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bedanta.dotcalc.ui.theme.NDotFontFamily

@Composable
fun GlyphButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = Color.Transparent,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    isWide: Boolean = false,
    isNumber: Boolean = false,
    isSmall: Boolean = false,
    enabledHaptics: Boolean = true,
    reduceAnimations: Boolean = false,
    buttonHeight: androidx.compose.ui.unit.Dp = if (isSmall) 44.dp else 72.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && !reduceAnimations) 0.975f else 1f,
        animationSpec = if (isPressed) {
            tween(durationMillis = 40, easing = LinearEasing)
        } else {
            if (reduceAnimations) tween(0)
            else spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        },
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(durationMillis = if (reduceAnimations) 0 else 60),
        label = "alpha"
    )
    
    val finalBorderColor = if (isNumber) borderColor.copy(alpha = 0.8f) else borderColor.copy(alpha = 0.4f)
    val finalContainerColor = if (isNumber) Color.White.copy(alpha = 0.05f) else containerColor
    
    val textStyle = if (isSmall) {
        MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontFamily = NDotFontFamily)
    } else {
        MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp, fontFamily = NDotFontFamily)
    }

    Box(
        modifier = modifier
            .height(buttonHeight)
            .then(if (isWide) Modifier.width(160.dp) else Modifier)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(finalContainerColor)
            .border(0.5.dp, finalBorderColor, RectangleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (enabledHaptics) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    onClick()
                }
            )
            .padding(if (isSmall) 4.dp else 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = textStyle.copy(
                color = contentColor
            )
        )
    }
}
