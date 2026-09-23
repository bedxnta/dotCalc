package com.bedanta.dotcalc.ui.base

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bedanta.dotcalc.logic.BaseCalculatorEngine
import com.bedanta.dotcalc.logic.NumBase
import com.bedanta.dotcalc.ui.CalculatorViewModel
import com.bedanta.dotcalc.ui.CalculatorUiState
import com.bedanta.dotcalc.ui.theme.NDotFontFamily
import com.bedanta.dotcalc.ui.theme.NType82FontFamily
import java.math.BigInteger

@Composable
fun BaseCalculatorScreen(uiState: CalculatorUiState, viewModel: CalculatorViewModel) {
    var engine by remember { mutableStateOf(BaseCalculatorEngine()) }
    var currentBase by remember { mutableStateOf(NumBase.DEC) }
    var bitWidth by remember { mutableStateOf(32) }
    var signed by remember { mutableStateOf(false) }

    engine.base = currentBase
    engine.bitWidth = bitWidth
    engine.signed = signed

    val reduceAnimations = uiState.settings.reduceAnimations

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // BASE selector
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(NumBase.BIN, NumBase.OCT, NumBase.DEC, NumBase.HEX).forEach { b ->
                val selected = b == currentBase
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .border(1.dp, if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), RectangleShape)
                        .background(if (selected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background)
                        .clickable { currentBase = b; engine.base = b }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(text = b.name, fontWeight = FontWeight.Bold, fontFamily = NDotFontFamily)
                    }
                }
            }
        }

        // Bit width selector
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(8, 16, 32, 64).forEach { w ->
                val sel = w == bitWidth
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .border(1.dp, if (sel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), RectangleShape)
                        .background(if (sel) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background)
                        .clickable { bitWidth = w; engine.bitWidth = w }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "${w} BIT", fontFamily = NType82FontFamily, fontSize = 12.sp)
                }
            }
        }

        // Compact conversion display
        val valueMap = if (uiState.baseInput.isBlank()) {
            mapOf(NumBase.DEC to "", NumBase.HEX to "", NumBase.OCT to "", NumBase.BIN to "")
        } else {
            try {
                val expr = uiState.baseInput.replace("−", "-").replace("×", "*").replace("÷", "/").trim()
                if (expr.isEmpty()) {
                    mapOf(NumBase.DEC to "", NumBase.HEX to "", NumBase.OCT to "", NumBase.BIN to "")
                } else {
                    val divisionIndex = expr.indexOf('/')
                    if (divisionIndex >= 0) {
                        val left = expr.substring(0, divisionIndex).trim()
                        val right = expr.substring(divisionIndex + 1).trim()
                        if (left.isEmpty() || right.isEmpty()) {
                            mapOf(NumBase.DEC to "", NumBase.HEX to "", NumBase.OCT to "", NumBase.BIN to "")
                        } else {
                            val a = engine.evaluate(left).value
                            val b = engine.evaluate(right).value
                            if (a == null || b == null || b == BigInteger.ZERO) {
                                mapOf(NumBase.DEC to "", NumBase.HEX to "", NumBase.OCT to "", NumBase.BIN to "")
                            } else {
                                val q = a.divide(b)
                                val r = a.remainder(b)
                                val baseVals = engine.toBases(q).toMutableMap()
                                val qText = when (currentBase) {
                                    NumBase.BIN -> q.toString(2)
                                    NumBase.OCT -> q.toString(8)
                                    NumBase.DEC -> q.toString()
                                    NumBase.HEX -> q.toString(16).uppercase()
                                }
                                val rText = when (currentBase) {
                                    NumBase.BIN -> r.toString(2)
                                    NumBase.OCT -> r.toString(8)
                                    NumBase.DEC -> r.toString()
                                    NumBase.HEX -> r.toString(16).uppercase()
                                }
                                baseVals[currentBase] = "Quo: $qText  Rem: $rText"
                                baseVals
                            }
                        }
                    } else {
                        val res = engine.evaluate(expr)
                        if (res.value == null) mapOf(NumBase.DEC to "", NumBase.HEX to "", NumBase.OCT to "", NumBase.BIN to "")
                        else engine.toBases(res.value)
                    }
                }
            } catch (e: Exception) {
                mapOf(NumBase.DEC to "", NumBase.HEX to "", NumBase.OCT to "", NumBase.BIN to "")
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, MaterialTheme.colorScheme.onBackground)
                .padding(8.dp)
                .then(
                    if (reduceAnimations) Modifier 
                    else Modifier.animateContentSize(tween(250))
                )
        ) {
            listOf(NumBase.DEC, NumBase.HEX, NumBase.OCT, NumBase.BIN).forEach { b ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = b.name, fontFamily = NType82FontFamily, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.weight(5f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        val valueText = valueMap[b] ?: ""
                        AnimatedContent(
                            targetState = valueText,
                            transitionSpec = {
                                if (reduceAnimations) {
                                    fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
                                } else {
                                    fadeIn(tween(150, delayMillis = 50)).togetherWith(fadeOut(tween(100)))
                                }
                            },
                            label = "base_value"
                        ) { text ->
                            Text(
                                text = text,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = NType82FontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val indicatorColor by animateColorAsState(
                            targetValue = if (currentBase == b) MaterialTheme.colorScheme.primary else Color.Transparent,
                            animationSpec = tween(if (reduceAnimations) 0 else 200),
                            label = "base_indicator"
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(indicatorColor)
                        )
                    }
                }
            }
        }

        // Input display
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "INPUT",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.secondary, fontFamily = NType82FontFamily)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = uiState.baseInput,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = NType82FontFamily,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Physical keypad layout
        val fixedDigitLayout = listOf(
            listOf("A", "B", "C", "D"),
            listOf("E", "F", "0", "1"),
            listOf("2", "3", "4", "5"),
            listOf("6", "7", "8", "9")
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            fixedDigitLayout.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { label ->
                        val enabled = when (currentBase) {
                            NumBase.BIN -> label in listOf("0", "1")
                            NumBase.OCT -> label in listOf("0","1","2","3","4","5","6","7")
                            NumBase.DEC -> label in listOf("0","1","2","3","4","5","6","7","8","9")
                            NumBase.HEX -> label in listOf("0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F")
                        }
                        com.bedanta.dotcalc.ui.components.GlyphButton(
                            text = label,
                            onClick = { if (enabled) viewModel.onBaseDigit(label) },
                            modifier = Modifier.weight(1f),
                            isNumber = true,
                            enabledHaptics = uiState.settings.hapticsEnabled,
                            reduceAnimations = reduceAnimations,
                            buttonHeight = 46.dp,
                            contentColor = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Operator keypad
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("+", "−", "×", "÷").forEach { op ->
                    val internal = when (op) {
                        "−" -> "-"
                        "×" -> "*"
                        "÷" -> "/"
                        else -> op
                    }
                    com.bedanta.dotcalc.ui.components.GlyphButton(
                        text = op,
                        onClick = { viewModel.onBaseOperator(internal) },
                        modifier = Modifier.weight(1f),
                        enabledHaptics = uiState.settings.hapticsEnabled,
                        reduceAnimations = reduceAnimations,
                        buttonHeight = 42.dp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("AND","OR","XOR","NOT","<<", ">>").forEach { op ->
                    com.bedanta.dotcalc.ui.components.GlyphButton(
                        text = op,
                        onClick = { viewModel.onBaseOperator(" $op ") },
                        modifier = Modifier.weight(1f),
                        enabledHaptics = uiState.settings.hapticsEnabled,
                        reduceAnimations = reduceAnimations,
                        buttonHeight = 42.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Controls row: AC, DEL, SGN/USGN
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = "AC",
                onClick = { viewModel.onBaseClear() },
                modifier = Modifier.weight(1.2f),
                contentColor = MaterialTheme.colorScheme.tertiary,
                enabledHaptics = uiState.settings.hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 48.dp
            )
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = "DEL",
                onClick = { viewModel.onBaseBackspace() },
                modifier = Modifier.weight(1.2f),
                enabledHaptics = uiState.settings.hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 48.dp
            )
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = if (signed) "SGN" else "USGN",
                onClick = { signed = !signed; engine.signed = signed },
                modifier = Modifier.weight(1.6f),
                enabledHaptics = uiState.settings.hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 48.dp
            )
        }
    }
}
