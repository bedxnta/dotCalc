package com.bedanta.dotcalc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.bedanta.dotcalc.logic.DecimalSeparator

@Composable
fun Keypad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onParenthesis: () -> Unit,
    onDecimal: () -> Unit,
    onEquals: () -> Unit,
    hapticsEnabled: Boolean = true,
    reduceAnimations: Boolean = false,
    decimalSeparator: DecimalSeparator = DecimalSeparator.DOT,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ... (Rows 1-4 remain same)
        // Row 1: AC, ( ), DEL, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlyphButton(text = "AC", onClick = onClear, modifier = Modifier.weight(1f), contentColor = MaterialTheme.colorScheme.tertiary, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "( )", onClick = onParenthesis, modifier = Modifier.weight(1f), contentColor = MaterialTheme.colorScheme.secondary, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "DEL", onClick = onBackspace, modifier = Modifier.weight(1f), contentColor = MaterialTheme.colorScheme.secondary, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "÷", onClick = { onOperator("÷") }, modifier = Modifier.weight(1f), contentColor = MaterialTheme.colorScheme.secondary, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlyphButton(text = "7", onClick = { onDigit("7") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "8", onClick = { onDigit("8") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "9", onClick = { onDigit("9") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "×", onClick = { onOperator("×") }, modifier = Modifier.weight(1f), contentColor = MaterialTheme.colorScheme.secondary, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlyphButton(text = "4", onClick = { onDigit("4") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "5", onClick = { onDigit("5") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "6", onClick = { onDigit("6") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "−", onClick = { onOperator("−") }, modifier = Modifier.weight(1f), contentColor = MaterialTheme.colorScheme.secondary, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlyphButton(text = "1", onClick = { onDigit("1") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "2", onClick = { onDigit("2") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "3", onClick = { onDigit("3") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "+", onClick = { onOperator("+") }, modifier = Modifier.weight(1f), contentColor = MaterialTheme.colorScheme.secondary, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
        }

        // Row 5: 0, 00, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlyphButton(text = "0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "00", onClick = { onDigit("00") }, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = if (decimalSeparator == DecimalSeparator.COMMA) "," else ".", onClick = onDecimal, modifier = Modifier.weight(1f), isNumber = true, enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
            GlyphButton(text = "=", onClick = onEquals, modifier = Modifier.weight(1f), contentColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f), enabledHaptics = hapticsEnabled, reduceAnimations = reduceAnimations)
        }
    }
}
