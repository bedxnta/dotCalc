package com.bedanta.dotcalc.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bedanta.dotcalc.logic.*
import com.bedanta.dotcalc.ui.theme.NDotFontFamily
import com.bedanta.dotcalc.ui.theme.NType82FontFamily
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

val HeaderButtonWidth = 110.dp
val HeaderButtonHeight = 36.dp

@Composable
fun ModeSelectorButton(
    currentMode: AppMode,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    settings: CalculatorSettings = CalculatorSettings(),
    onToggle: () -> Unit,
    onSelect: (AppMode) -> Unit
) {
    Box(modifier = modifier.width(HeaderButtonWidth)) {
        Box(
            modifier = Modifier
                .width(HeaderButtonWidth)
                .height(HeaderButtonHeight)
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
                .background(MaterialTheme.colorScheme.background)
                .clickable { onToggle() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when (currentMode) {
                    AppMode.BASIC -> "BASIC ▾"
                    AppMode.CONVERT -> "CONVERT ▾"
                    AppMode.BASE -> "BASE ▾"
                },
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = NDotFontFamily,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = if (settings.reduceAnimations) fadeIn(tween(150)) else (fadeIn(tween(150)) + expandVertically(animationSpec = tween(200, easing = LinearOutSlowInEasing))),
            exit = if (settings.reduceAnimations) fadeOut(tween(150)) else (fadeOut(tween(120)) + shrinkVertically(animationSpec = tween(180, easing = FastOutLinearInEasing))),
            modifier = Modifier
                .padding(top = HeaderButtonHeight + 6.dp)
                .width(HeaderButtonWidth)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    listOf(AppMode.BASIC, AppMode.CONVERT, AppMode.BASE).forEach { mode ->
                        val selected = mode == currentMode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(mode) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(if (selected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (mode) {
                                    AppMode.BASIC -> "BASIC"
                                    AppMode.CONVERT -> "CONVERT"
                                    AppMode.BASE -> "BASE"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontFamily = NDotFontFamily,
                                    textAlign = TextAlign.Start
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnitConversionScreen(
    uiState: CalculatorUiState,
    onCategorySelected: (UnitCategory) -> Unit,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onToggleSign: () -> Unit,
    onOpenPicker: (ConversionSelectorSide) -> Unit,
    onDismissPicker: () -> Unit,
    onUnitSelected: (String) -> Unit,
    onSwap: () -> Unit,
    onHistoryClicked: (ConversionHistoryEntry) -> Unit,
    onClearHistory: () -> Unit,
    onSetActiveSide: (ConversionSelectorSide) -> Unit
) {
    val engine = remember { UnitConversionEngine() }
    val categories = remember { engine.categories() }
    val categoryScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 16.dp)
    ) {
        Text(
            text = "CONVERT",
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = NDotFontFamily,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(categoryScroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val label = category.name.replace('_', ' ')
                val selected = category == uiState.conversionCategory
                Box(
                    modifier = Modifier
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
                        .background(if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background)
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            fontFamily = NType82FontFamily,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        val fromDef = engine.unitsFor(uiState.conversionCategory).firstOrNull { it.id == uiState.conversionFromUnitId } ?: engine.unitsFor(uiState.conversionCategory).first()
        val toDef = engine.unitsFor(uiState.conversionCategory).firstOrNull { it.id == uiState.conversionToUnitId } ?: engine.unitsFor(uiState.conversionCategory).getOrNull(1) ?: fromDef

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
                .padding(12.dp)
                .then(
                    if (uiState.settings.reduceAnimations) Modifier 
                    else Modifier.animateContentSize(tween(250))
                )
        ) {
            ConversionFieldCard(
                title = "FROM",
                value = SettingsManager.formatExpression(uiState.conversionFromValue, uiState.settings),
                unitLabel = fromDef.label,
                unitSymbol = fromDef.symbol,
                isActive = uiState.conversionActiveSide == ConversionSelectorSide.FROM,
                onFieldTap = { onSetActiveSide(ConversionSelectorSide.FROM) },
                onUnitClick = { onOpenPicker(ConversionSelectorSide.FROM) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (uiState.conversionActiveSide == ConversionSelectorSide.TO) 180f else 0f,
                    animationSpec = if (uiState.settings.reduceAnimations) tween(0) else spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "swap_rotation"
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .graphicsLayer { rotationZ = rotation }
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onSwap() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⇅", fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                }
            }

            ConversionFieldCard(
                title = "TO",
                value = SettingsManager.formatExpression(uiState.conversionToValue, uiState.settings),
                unitLabel = toDef.label,
                unitSymbol = toDef.symbol,
                isActive = uiState.conversionActiveSide == ConversionSelectorSide.TO,
                onFieldTap = { onSetActiveSide(ConversionSelectorSide.TO) },
                onUnitClick = { onOpenPicker(ConversionSelectorSide.TO) }
            )

            if (uiState.conversionError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = uiState.conversionError,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = NType82FontFamily)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        UnitConversionKeypad(
            hapticsEnabled = uiState.settings.hapticsEnabled,
            reduceAnimations = uiState.settings.reduceAnimations,
            decimalSeparator = uiState.settings.decimalSeparator,
            onClear = onClear,
            onDelete = onBackspace,
            onToggleSign = onToggleSign,
            onDigit = onDigit,
            onDecimal = onDecimal
        )
    }

    if (uiState.conversionPickerOpen) {
        Dialog(onDismissRequest = onDismissPicker) {
            val focusManager = LocalFocusManager.current
            var query by remember { mutableStateOf("") }
            val units = engine.unitsFor(uiState.conversionCategory).filter {
                val text = "$it ${it.aliases.joinToString()}".lowercase()
                query.isBlank() || text.contains(query.lowercase())
            }
            Column(
                modifier = Modifier
                    .width(330.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
                    .padding(12.dp)
            ) {
                Text(
                    text = if (uiState.conversionPickerSide == ConversionSelectorSide.FROM) "SELECT FROM UNIT" else "SELECT TO UNIT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = NType82FontFamily,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
                        .padding(8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = NType82FontFamily
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxHeight(0.7f)) {
                    items(units) { unit ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    focusManager.clearFocus()
                                    onUnitSelected(unit.id)
                                }
                                .padding(vertical = 8.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = "${unit.label} (${unit.symbol})",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontFamily = NType82FontFamily
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnitConversionKeypad(
    hapticsEnabled: Boolean = true,
    reduceAnimations: Boolean = false,
    decimalSeparator: com.bedanta.dotcalc.logic.DecimalSeparator = com.bedanta.dotcalc.logic.DecimalSeparator.DOT,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onToggleSign: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = "AC",
                onClick = onClear,
                modifier = Modifier.weight(1f),
                contentColor = MaterialTheme.colorScheme.tertiary,
                enabledHaptics = hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 58.dp
            )
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = "DEL",
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                contentColor = MaterialTheme.colorScheme.secondary,
                enabledHaptics = hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 58.dp
            )
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = "±",
                onClick = onToggleSign,
                modifier = Modifier.weight(1f),
                contentColor = MaterialTheme.colorScheme.secondary,
                enabledHaptics = hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 58.dp
            )
        }

        listOf(listOf("7", "8", "9"), listOf("4", "5", "6"), listOf("1", "2", "3")).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { digit ->
                    com.bedanta.dotcalc.ui.components.GlyphButton(
                        text = digit,
                        onClick = { onDigit(digit) },
                        modifier = Modifier.weight(1f),
                        isNumber = true,
                        enabledHaptics = hapticsEnabled,
                        reduceAnimations = reduceAnimations,
                        buttonHeight = 58.dp
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = "0",
                onClick = { onDigit("0") },
                modifier = Modifier.weight(1f),
                isNumber = true,
                enabledHaptics = hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 58.dp
            )
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = "00",
                onClick = { onDigit("00") },
                modifier = Modifier.weight(1f),
                isNumber = true,
                enabledHaptics = hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 58.dp
            )
            com.bedanta.dotcalc.ui.components.GlyphButton(
                text = if (decimalSeparator == com.bedanta.dotcalc.logic.DecimalSeparator.COMMA) "," else ".",
                onClick = onDecimal,
                modifier = Modifier.weight(1f),
                isNumber = true,
                enabledHaptics = hapticsEnabled,
                reduceAnimations = reduceAnimations,
                buttonHeight = 58.dp
            )
        }
    }
}

@Composable
private fun ConversionFieldCard(
    title: String,
    value: String,
    unitLabel: String,
    unitSymbol: String,
    isActive: Boolean,
    onFieldTap: () -> Unit,
    onUnitClick: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = NType82FontFamily,
                letterSpacing = 1.sp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = unitLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = NType82FontFamily
                )
            )
            Box(
                modifier = Modifier
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
                    .clickable { onUnitClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = unitSymbol,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = NType82FontFamily
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .alpha(if (isActive) 1f else 0.8f)
                .border(
                    1.dp,
                    if (isActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                    RectangleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onFieldTap() },
                        onLongPress = { clipboard.setText(AnnotatedString(value)) }
                    )
                }
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    fontFamily = NType82FontFamily,
                    fontWeight = FontWeight.Light
                ),
                maxLines = 1
            )
        }
    }
}
