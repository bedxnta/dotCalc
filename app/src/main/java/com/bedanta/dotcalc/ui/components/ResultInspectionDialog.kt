package com.bedanta.dotcalc.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bedanta.dotcalc.logic.DetailedResult
import com.bedanta.dotcalc.ui.theme.NDotFontFamily
import com.bedanta.dotcalc.ui.theme.NType82FontFamily

@Composable
fun ResultInspectionDialog(
    result: DetailedResult,
    settings: com.bedanta.dotcalc.logic.CalculatorSettings = com.bedanta.dotcalc.logic.CalculatorSettings(),
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        var isVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { isVisible = true }
        
        val scale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0.95f,
            animationSpec = tween(durationMillis = if (settings.reduceAnimations) 0 else 250, easing = LinearOutSlowInEasing),
            label = "dialog_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(durationMillis = if (settings.reduceAnimations) 0 else 200),
            label = "dialog_alpha"
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
            shape = RectangleShape,
            color = Color.Black,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .border(0.5.dp, MaterialTheme.colorScheme.outline)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESULT",
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NType82FontFamily
                        )
                    )
                    Text(
                        text = "×",
                        modifier = Modifier.clickable { onDismiss() },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NDotFontFamily
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = result.expression,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontFamily = NType82FontFamily
                    )
                )

                Text(
                    text = "= ${result.decimal}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = NType82FontFamily
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                ResultItem("DECIMAL", result.decimal) {
                    clipboardManager.setText(AnnotatedString(result.decimal))
                }

                // Show EXACT FORM if available
                result.exactDisplay?.let {
                    FractionResultItem("EXACT FORM", it) {
                        clipboardManager.setText(AnnotatedString(it))
                    }
                }

                // Show FRACTION separately only if different from exactDisplay or if specific fraction logic is needed
                result.fraction?.let {
                    if (it != result.exactDisplay) {
                        FractionResultItem("FRACTION", it) {
                            clipboardManager.setText(AnnotatedString(it))
                        }
                    }
                }

                ResultItem("ENGINEERING", result.engineering) {
                    clipboardManager.setText(AnnotatedString(result.engineering))
                }

                ResultItem("SCIENTIFIC", result.scientific) {
                    clipboardManager.setText(AnnotatedString(result.scientific))
                }

                result.mixedFraction?.let {
                    FractionResultItem("MIXED FRACTION", it) {
                        clipboardManager.setText(AnnotatedString(it))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "CLOSE",
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NDotFontFamily
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ResultItem(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onCopy() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold,
                fontFamily = NType82FontFamily
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = NType82FontFamily
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "COPY",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp,
                    fontFamily = NDotFontFamily
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun FractionResultItem(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { onCopy() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold,
                fontFamily = NType82FontFamily
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.contains("/")) {
                    val parts = value.split(" ")
                    val fractionPart = if (parts.size > 1) parts[1] else parts[0]
                    val wholePart = if (parts.size > 1) parts[0] else null
                    
                    val fractionSplit = fractionPart.split("/")
                    val numerator = fractionSplit[0]
                    val denominator = fractionSplit[1]

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (wholePart != null) {
                            Text(
                                text = "$wholePart ",
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = NType82FontFamily)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = numerator,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontFamily = NType82FontFamily)
                            )
                            Box(
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                                    .height(1.dp)
                                    .background(Color.White)
                                    .padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = if (numerator.length > denominator.length) numerator else denominator,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontFamily = NType82FontFamily),
                                    color = Color.Transparent
                                )
                            }
                            Text(
                                text = denominator,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontFamily = NType82FontFamily)
                            )
                        }
                    }
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = NType82FontFamily)
                    )
                }
            }
            Text(
                text = "COPY",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 10.sp,
                    fontFamily = NDotFontFamily
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )
    }
}
