package com.bedanta.dotcalc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CursorControls(
    onLeft: () -> Unit,
    onRight: () -> Unit,
    hapticsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(138.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlyphButton(
                text = "←",
                onClick = onLeft,
                modifier = Modifier.weight(1f),
                isSmall = true,
                contentColor = MaterialTheme.colorScheme.secondary,
                enabledHaptics = hapticsEnabled
            )
            GlyphButton(
                text = "→",
                onClick = onRight,
                modifier = Modifier.weight(1f),
                isSmall = true,
                contentColor = MaterialTheme.colorScheme.secondary,
                enabledHaptics = hapticsEnabled
            )
        }
    }
}
