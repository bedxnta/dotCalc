package com.bedanta.dotcalc.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.bedanta.dotcalc.ui.theme.NDotFontFamily
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import com.bedanta.dotcalc.logic.*
import com.bedanta.dotcalc.ui.components.*
import com.bedanta.dotcalc.ui.base.BaseCalculatorScreen

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = uiState.currentScreen,
            transitionSpec = {
                if (uiState.settings.reduceAnimations) {
                    fadeIn(tween(150)).togetherWith(fadeOut(tween(150)))
                } else {
                    if (targetState == CurrentScreen.SETTINGS) {
                        (fadeIn(animationSpec = tween(180)) +
                                slideInVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it / 12 })
                            .togetherWith(fadeOut(animationSpec = tween(150)))
                    } else {
                        (fadeIn(animationSpec = tween(180)))
                            .togetherWith(fadeOut(animationSpec = tween(150)) +
                                    slideOutVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) { it / 12 })
                    }
                }
            },
            label = "screen_navigation"
        ) { screen ->
            if (screen == CurrentScreen.SETTINGS) {
                SettingsScreen(
                    settings = uiState.settings,
                    onSettingsChange = { viewModel.updateSettings(it) },
                    onBack = { viewModel.toggleSettings() }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    AnimatedContent(
                        targetState = uiState.appMode,
                        transitionSpec = {
                            if (uiState.settings.reduceAnimations) {
                                fadeIn(tween(150)).togetherWith(fadeOut(tween(150)))
                            } else {
                                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                                (fadeIn(animationSpec = tween(180)) + 
                                 slideInHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { direction * it / 12 })
                                    .togetherWith(fadeOut(animationSpec = tween(150)) + 
                                     slideOutHorizontally(animationSpec = tween(250, easing = FastOutSlowInEasing)) { -direction * it / 12 })
                            }
                        },
                        label = "mode_switch"
                    ) { mode ->
                        when (mode) {
                            AppMode.BASIC -> BasicCalculatorContent(uiState, viewModel)
                            AppMode.CONVERT -> UnitConversionScreen(
                                uiState = uiState,
                                onCategorySelected = { viewModel.setConversionCategory(it) },
                                onDigit = { viewModel.onConversionDigit(it) },
                                onDecimal = { viewModel.onConversionDecimal() },
                                onBackspace = { viewModel.onConversionBackspace() },
                                onClear = { viewModel.onConversionClear() },
                                onToggleSign = { viewModel.onConversionToggleSign() },
                                onOpenPicker = { viewModel.openConversionPicker(it) },
                                onDismissPicker = { viewModel.dismissConversionPicker() },
                                onUnitSelected = { viewModel.setConversionUnit(it) },
                                onSwap = { viewModel.swapConversionUnits() },
                                onHistoryClicked = { viewModel.applyConversionHistory(it) },
                                onClearHistory = { viewModel.clearHistory() },
                                onSetActiveSide = { viewModel.setConversionActiveSide(it) }
                            )
                            AppMode.BASE -> BaseCalculatorScreen(uiState = uiState, viewModel = viewModel)
                        }
                    }

                    // Top Navigation Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.TopStart),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HistoryButton(
                                onClick = { if (uiState.appMode == AppMode.BASIC) viewModel.toggleHistory() },
                                enabled = uiState.appMode == AppMode.BASIC
                            )
                            SettingsButton(
                                onClick = { viewModel.toggleSettings() }
                            )
                        }

                        ModeSelectorButton(
                            currentMode = uiState.appMode,
                            expanded = uiState.modeSelectorExpanded,
                            settings = uiState.settings,
                            onToggle = { viewModel.toggleModeSelector() },
                            onSelect = { viewModel.selectAppMode(it) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }

                    uiState.detailedResult?.let { result ->
                        ResultInspectionDialog(
                            result = result,
                            settings = uiState.settings,
                            onDismiss = { viewModel.dismissDetailedResult() }
                        )
                    }

                    HistoryDrawerOverlay(
                        show = uiState.showHistory,
                        history = uiState.history,
                        settings = uiState.settings,
                        onToggle = { viewModel.toggleHistory() },
                        onItemClick = { viewModel.onHistoryItemClick(it) },
                        onClearAll = { viewModel.clearHistory() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier = modifier
            .width(HeaderButtonWidth)
            .height(HeaderButtonHeight)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
            .background(MaterialTheme.colorScheme.background)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .alpha(if (enabled) 1f else 0.45f),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "HISTORY",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = NDotFontFamily,
                textAlign = TextAlign.Start
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(HeaderButtonHeight)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
            .background(MaterialTheme.colorScheme.background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BasicCalculatorContent(
    uiState: CalculatorUiState,
    viewModel: CalculatorViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CalculatorDisplay(
            expression = uiState.expression,
            liveResult = uiState.liveResult,
            isError = uiState.isError,
            errorMessage = uiState.errorMessage,
            settings = uiState.settings,
            onBackspace = { viewModel.onBackspace() },
            onHistoryToggle = { viewModel.toggleHistory() },
            onResultClick = { viewModel.onResultClick() },
            modifier = Modifier.weight(1.5f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(3.5f)
                .padding(bottom = 24.dp)
                .then(
                    if (uiState.settings.reduceAnimations) Modifier 
                    else Modifier.animateContentSize(tween(300, easing = LinearOutSlowInEasing))
                ),
            verticalArrangement = Arrangement.Bottom
        ) {
            ScientificKeypad(
                isInverse = uiState.isInverse,
                angleMode = uiState.angleMode,
                onFunctionClick = { viewModel.onFunction(it) },
                onInverseToggle = { viewModel.toggleInverse() },
                onAngleModeToggle = { viewModel.toggleAngleMode() },
                hapticsEnabled = uiState.settings.hapticsEnabled,
                reduceAnimations = uiState.settings.reduceAnimations
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Keypad(
                    onDigit = { viewModel.onDigit(it) },
                    onOperator = { viewModel.onOperator(it) },
                    onClear = { viewModel.onClear() },
                    onBackspace = { viewModel.onBackspace() },
                    onParenthesis = { viewModel.onParenthesis() },
                    onDecimal = { viewModel.onDecimal() },
                    onEquals = { viewModel.onEquals() },
                    hapticsEnabled = uiState.settings.hapticsEnabled,
                    reduceAnimations = uiState.settings.reduceAnimations,
                    decimalSeparator = uiState.settings.decimalSeparator,
                    modifier = Modifier.fillMaxWidth()
                )

                HistoryDrawerOverlay(
                    show = uiState.showHistory,
                    history = uiState.history,
                    settings = uiState.settings,
                    onToggle = { viewModel.toggleHistory() },
                    onItemClick = { viewModel.onHistoryItemClick(it) },
                    onClearAll = { viewModel.clearHistory() }
                )
            }
        }
    }
}

@Composable
fun BoxScope.HistoryDrawerOverlay(
    show: Boolean,
    history: List<HistoryEntry>,
    settings: CalculatorSettings,
    onToggle: () -> Unit,
    onItemClick: (HistoryEntry) -> Unit,
    onClearAll: () -> Unit
) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(tween(if (settings.reduceAnimations) 200 else 250)),
        exit = fadeOut(tween(if (settings.reduceAnimations) 150 else 200)),
        modifier = Modifier.matchParentSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() }
        )
    }

    AnimatedVisibility(
        visible = show,
        enter = if (settings.reduceAnimations) fadeIn(tween(250)) else (slideInHorizontally(tween(300, easing = LinearOutSlowInEasing)) { -it } + fadeIn(tween(300))),
        exit = if (settings.reduceAnimations) fadeOut(tween(200)) else (slideOutHorizontally(tween(250, easing = FastOutLinearInEasing)) { -it } + fadeOut(tween(250))),
        modifier = Modifier.matchParentSize()
    ) {
        HistoryPanel(
            history = history,
            onItemClick = onItemClick,
            onClearAll = onClearAll,
            onClose = onToggle,
            modifier = Modifier.fillMaxSize()
        )
    }
}
