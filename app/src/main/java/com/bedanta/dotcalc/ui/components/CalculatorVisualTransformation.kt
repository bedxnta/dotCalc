package com.bedanta.dotcalc.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.bedanta.dotcalc.logic.CalculatorSettings
import com.bedanta.dotcalc.logic.DecimalSeparator
import com.bedanta.dotcalc.logic.SettingsManager

class CalculatorVisualTransformation(
    private val settings: CalculatorSettings,
    private val isBaseMode: Boolean = false
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val transformedText = SettingsManager.formatExpression(originalText, settings, isBaseMode)
        
        val groupingSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) '.' else ','

        // Build offset mapping
        // We track where each character of the original text ends up in the transformed text.
        val mapping = IntArray(originalText.length + 1)
        var tIdx = 0
        for (oIdx in originalText.indices) {
            val oChar = originalText[oIdx]
            
            // Skip grouping separators in the transformed text that don't exist in original
            while (tIdx < transformedText.length && 
                   transformedText[tIdx] == groupingSep && 
                   oChar != groupingSep) {
                tIdx++
            }
            
            mapping[oIdx] = tIdx
            
            if (tIdx < transformedText.length) {
                tIdx++
            }
        }
        mapping[originalText.length] = transformedText.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return mapping[offset.coerceIn(0, originalText.length)]
            }

            override fun transformedToOriginal(offset: Int): Int {
                // Find the first original index that maps to or after this transformed offset
                for (i in 0..originalText.length) {
                    if (mapping[i] >= offset) return i
                }
                return originalText.length
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}
