package com.bedanta.dotcalc.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bedanta.dotcalc.logic.HistoryEntry
import com.bedanta.dotcalc.ui.theme.NDotFontFamily
import com.bedanta.dotcalc.ui.theme.NType82FontFamily

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryPanel(
    history: List<HistoryEntry>,
    onItemClick: (HistoryEntry) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.85f)
            .background(MaterialTheme.colorScheme.background)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f), RectangleShape)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HISTORY",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NDotFontFamily
                )
            )
            Row {
                Text(
                    text = "CLEAR",
                    modifier = Modifier
                        .clickable { onClearAll() }
                        .padding(8.dp),
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.tertiary, fontFamily = NDotFontFamily)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CLOSE",
                    modifier = Modifier
                        .clickable { onClose() }
                        .padding(8.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = NDotFontFamily)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(6.dp))

        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "NO HISTORY",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline, fontFamily = NType82FontFamily)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(history.reversed()) { entry ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                placementSpec = tween(250)
                            )
                            .clickable { onItemClick(entry) }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = entry.expression,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 14.sp,
                                fontFamily = NType82FontFamily
                            ),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "= ${entry.result}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, fontFamily = NType82FontFamily),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 0.2.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}
