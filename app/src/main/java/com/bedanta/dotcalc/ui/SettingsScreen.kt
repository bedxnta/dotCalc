package com.bedanta.dotcalc.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bedanta.dotcalc.logic.*
import com.bedanta.dotcalc.ui.theme.NDotFontFamily
import com.bedanta.dotcalc.ui.theme.NType82FontFamily

@Composable
fun SettingsScreen(
    settings: CalculatorSettings,
    onSettingsChange: (CalculatorSettings) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = NDotFontFamily,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "BACK",
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(8.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = NDotFontFamily)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "APPEARANCE") {
                SettingOptionRow(
                    label = "ACCENT MODE",
                    options = listOf("RED", "WHITE"),
                    selected = settings.accentMode.name,
                    onSelect = { onSettingsChange(settings.copy(accentMode = AccentMode.valueOf(it))) }
                )
                SettingOptionRow(
                    label = "APP NAME (LAUNCHER)",
                    options = listOf("DOTCALC", "CALCULATOR"),
                    selected = settings.launcherName.name,
                    onSelect = { onSettingsChange(settings.copy(launcherName = AppLauncherName.valueOf(it))) }
                )
            }

            SettingsSection(title = "INPUT & DISPLAY") {
                SettingToggleRow(
                    label = "HAPTIC FEEDBACK",
                    enabled = settings.hapticsEnabled,
                    onToggle = { onSettingsChange(settings.copy(hapticsEnabled = it)) }
                )
                SettingToggleRow(
                    label = "REDUCE ANIMATIONS",
                    enabled = settings.reduceAnimations,
                    onToggle = { onSettingsChange(settings.copy(reduceAnimations = it)) }
                )
            }

            SettingsSection(title = "NUMBER FORMAT") {
                SettingToggleRow(
                    label = "NUMBER GROUPING",
                    enabled = settings.groupingEnabled,
                    onToggle = { onSettingsChange(settings.copy(groupingEnabled = it)) }
                )
                
                if (settings.groupingEnabled) {
                    SettingOptionRow(
                        label = "GROUPING SYSTEM",
                        options = listOf("INTERNATIONAL", "INDIAN"),
                        selected = settings.groupingSeparator.name,
                        onSelect = { onSettingsChange(settings.copy(groupingSeparator = GroupingSeparator.valueOf(it))) }
                    )
                }

                SettingOptionRow(
                    label = "DECIMAL SEPARATOR",
                    options = listOf("DOT (.)", "COMMA (,)"),
                    selected = if (settings.decimalSeparator == DecimalSeparator.DOT) "DOT (.)" else "COMMA (,)",
                    onSelect = { onSettingsChange(settings.copy(decimalSeparator = if (it == "DOT (.)") DecimalSeparator.DOT else DecimalSeparator.COMMA)) }
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = "DOTCALC V1.1",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.outline,
                    fontFamily = NDotFontFamily,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
            .animateContentSize(tween(250))
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontFamily = NDotFontFamily,
                letterSpacing = 1.5.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun SettingOptionRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = NDotFontFamily,
                fontWeight = FontWeight.Normal
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = selected == option
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background,
                    animationSpec = tween(200),
                    label = "option_bg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    animationSpec = tween(200),
                    label = "option_border"
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .border(
                            0.5.dp,
                            borderColor,
                            RectangleShape
                        )
                        .background(backgroundColor)
                        .clickable { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary,
                            fontFamily = NType82FontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onToggle(!enabled) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = NDotFontFamily,
                fontWeight = FontWeight.Normal
            )
        )
        
        val horizontalBias by animateFloatAsState(
            targetValue = if (enabled) 1f else -1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
            label = "toggle_slide"
        )
        val toggleBgColor by animateColorAsState(if (enabled) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background)
        val thumbColor by animateColorAsState(if (enabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

        Box(
            modifier = Modifier
                .width(44.dp)
                .height(24.dp)
                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
                .background(toggleBgColor)
                .padding(2.dp),
            contentAlignment = BiasAlignment(horizontalBias = horizontalBias, verticalBias = 0f)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(thumbColor)
            )
        }
    }
}
