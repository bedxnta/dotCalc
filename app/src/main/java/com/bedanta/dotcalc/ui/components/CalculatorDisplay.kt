package com.bedanta.dotcalc.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bedanta.dotcalc.logic.CalculatorSettings
import com.bedanta.dotcalc.logic.ExpressionEvaluator
import com.bedanta.dotcalc.logic.MathValue
import com.bedanta.dotcalc.logic.SettingsManager
import com.bedanta.dotcalc.ui.theme.NType82FontFamily
import java.math.BigDecimal

@Composable
fun CalculatorDisplay(
    expression: String,
    liveResult: MathValue,
    isError: Boolean = false,
    errorMessage: String? = null,
    settings: CalculatorSettings = CalculatorSettings(),
    onBackspace: () -> Unit = {},
    onHistoryToggle: () -> Unit,
    onResultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val evaluator = remember<ExpressionEvaluator> { ExpressionEvaluator() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        val isValidPreview = !isError && expression.isNotEmpty() && liveResult !is MathValue.Undefined && !liveResult.numericValue.isNaN() && !liveResult.numericValue.isInfinite()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 6.dp)
                .then(
                    if (settings.reduceAnimations) Modifier 
                    else Modifier.animateContentSize(tween(180, easing = LinearOutSlowInEasing))
                ),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = if (isError) (errorMessage ?: "Error: Invalid expression") else SettingsManager.formatExpression(expression, settings),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = if (isError) 18.sp else 32.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.End,
                        fontFamily = NType82FontFamily,
                        lineHeight = if (isError) 22.sp else 38.sp
                    ),
                    color = if (isError) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground,
                    maxLines = if (isError) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                    modifier = if (isError) Modifier.widthIn(max = 260.dp).fillMaxWidth() else Modifier
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.Bottom),
                contentAlignment = Alignment.BottomEnd
            ) {
                AnimatedContent(
                    targetState = liveResult to isValidPreview,
                    transitionSpec = {
                        if (settings.reduceAnimations) {
                            fadeIn(tween(0)).togetherWith(fadeOut(tween(0)))
                        } else {
                            fadeIn(tween(150)).togetherWith(fadeOut(tween(100)))
                        }
                    },
                    label = "preview_transition"
                ) { (result, valid) ->
                    when {
                        valid && result is MathValue.Exact && result.isPureRational() -> {
                            Row(
                                modifier = Modifier.clickable { onResultClick() },
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "= ",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = NType82FontFamily
                                    )
                                )
                                ExactResultRenderer(result, settings)
                            }
                        }
                        valid -> {
                            val previewText = evaluator.formatResult(result, settings, stripZeros = false)
                            Text(
                                text = "≈ $previewText",
                                modifier = Modifier
                                    .clickable { onResultClick() }
                                    .alpha(1f),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.End,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NType82FontFamily
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        else -> {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExactResultRenderer(value: MathValue.Exact, settings: CalculatorSettings = CalculatorSettings()) {
    if (value.isPureRational() && value.rational.d != 1L) {
        val n = value.rational.n
        val d = value.rational.d
        val nStr = SettingsManager.formatDecimal(BigDecimal.valueOf(n), settings)
        val dStr = SettingsManager.formatDecimal(BigDecimal.valueOf(d), settings)
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = nStr,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = NType82FontFamily
                )
            )
            Box(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = if (nStr.length > dStr.length) nStr else dStr,
                    fontSize = 18.sp,
                    color = Color.Transparent
                )
            }
            Text(
                text = dStr,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = NType82FontFamily
                )
            )
        }
    } else {
        Text(
            text = value.toFormattedExactString(settings),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                fontFamily = NType82FontFamily
            )
        )
    }
}
