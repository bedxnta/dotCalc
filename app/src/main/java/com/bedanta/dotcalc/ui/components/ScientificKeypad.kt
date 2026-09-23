package com.bedanta.dotcalc.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bedanta.dotcalc.logic.AngleMode

@Composable
fun ScientificKeypad(
    isInverse: Boolean,
    angleMode: AngleMode,
    onFunctionClick: (String) -> Unit,
    onInverseToggle: () -> Unit,
    onAngleModeToggle: () -> Unit,
    hapticsEnabled: Boolean = true,
    reduceAnimations: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Row 1: INV, DEG/RAD, sin, cos, tan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GlyphButton(text = "INV", onClick = onInverseToggle, modifier = Modifier.weight(1f), isSmall = true, contentColor = if (isInverse) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = angleMode.name, onClick = onAngleModeToggle, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            
            ScientificButton(if (isInverse) "sin⁻¹" else "sin", onFunctionClick, hapticsEnabled, reduceAnimations)
            ScientificButton(if (isInverse) "cos⁻¹" else "cos", onFunctionClick, hapticsEnabled, reduceAnimations)
            ScientificButton(if (isInverse) "tan⁻¹" else "tan", onFunctionClick, hapticsEnabled, reduceAnimations)
        }

        // Row 2: √, x², xʸ, ln, log
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GlyphButton(text = "√", onClick = { onFunctionClick("√") }, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            ScientificButton(if (isInverse) "eˣ" else "x²", onFunctionClick, hapticsEnabled, reduceAnimations)
            ScientificButton(if (isInverse) "10ˣ" else "xʸ", onFunctionClick, hapticsEnabled, reduceAnimations)
            GlyphButton(text = "ln", onClick = { onFunctionClick("ln") }, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "log", onClick = { onFunctionClick("log") }, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
        }

        // Row 3: π, e, !, 1/x, %
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            GlyphButton(text = "π", onClick = { onFunctionClick("π") }, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "e", onClick = { onFunctionClick("e") }, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "!", onClick = { onFunctionClick("!") }, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "1/x", onClick = { onFunctionClick("1/x") }, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "%", onClick = { onFunctionClick("%") }, modifier = Modifier.weight(1f), isSmall = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
        }
    }
}

@Composable
private fun RowScope.ScientificButton(label: String, onClick: (String) -> Unit, hapticsEnabled: Boolean, reduceAnimations: Boolean) {
    if (reduceAnimations) {
        GlyphButton(
            text = label,
            onClick = { onClick(label) },
            modifier = Modifier.weight(1f),
            isSmall = true,
            enabledHaptics = hapticsEnabled,
            reduceAnimations = true
        )
    } else {
        AnimatedContent(
            targetState = label,
            transitionSpec = {
                fadeIn(tween(150)).togetherWith(fadeOut(tween(100)))
            },
            modifier = Modifier.weight(1f),
            label = "scientific_label"
        ) { text ->
            GlyphButton(
                text = text,
                onClick = { onClick(text) },
                isSmall = true,
                enabledHaptics = hapticsEnabled
            )
        }
    }
}
