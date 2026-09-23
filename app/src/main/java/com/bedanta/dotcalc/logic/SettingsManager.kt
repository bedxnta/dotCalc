package com.bedanta.dotcalc.logic

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import java.math.BigDecimal
import java.util.Locale

enum class AccentMode { RED, WHITE }
enum class GroupingSeparator { INDIAN, INTERNATIONAL }
enum class DecimalSeparator { DOT, COMMA }
enum class CurrentScreen { CALCULATOR, SETTINGS }
enum class AppLauncherName { DOTCALC, CALCULATOR }

data class CalculatorSettings(
    val accentMode: AccentMode = AccentMode.RED,
    val hapticsEnabled: Boolean = true,
    val groupingEnabled: Boolean = true,
    val groupingSeparator: GroupingSeparator = GroupingSeparator.INTERNATIONAL,
    val decimalSeparator: DecimalSeparator = DecimalSeparator.DOT,
    val reduceAnimations: Boolean = false,
    val launcherName: AppLauncherName = AppLauncherName.DOTCALC
)

class SettingsManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dot_calc_settings", Context.MODE_PRIVATE)

    fun saveSettings(settings: CalculatorSettings) {
        val oldLauncherName = loadSettings().launcherName
        
        prefs.edit().apply {
            putString("accent_mode", settings.accentMode.name)
            putBoolean("haptics_enabled", settings.hapticsEnabled)
            putBoolean("grouping_enabled", settings.groupingEnabled)
            putString("grouping_separator", settings.groupingSeparator.name)
            putString("decimal_separator", settings.decimalSeparator.name)
            putBoolean("reduce_animations", settings.reduceAnimations)
            putString("launcher_name", settings.launcherName.name)
            apply()
        }

        if (oldLauncherName != settings.launcherName) {
            updateLauncherName(settings.launcherName)
        }
    }

    fun loadSettings(): CalculatorSettings {
        return CalculatorSettings(
            accentMode = AccentMode.valueOf(prefs.getString("accent_mode", AccentMode.RED.name) ?: AccentMode.RED.name),
            hapticsEnabled = prefs.getBoolean("haptics_enabled", true),
            groupingEnabled = prefs.getBoolean("grouping_enabled", true),
            groupingSeparator = GroupingSeparator.valueOf(prefs.getString("grouping_separator", GroupingSeparator.INTERNATIONAL.name) ?: GroupingSeparator.INTERNATIONAL.name),
            decimalSeparator = DecimalSeparator.valueOf(prefs.getString("decimal_separator", DecimalSeparator.DOT.name) ?: DecimalSeparator.DOT.name),
            reduceAnimations = prefs.getBoolean("reduce_animations", false),
            launcherName = AppLauncherName.valueOf(prefs.getString("launcher_name", AppLauncherName.DOTCALC.name) ?: AppLauncherName.DOTCALC.name)
        )
    }

    private fun updateLauncherName(name: AppLauncherName) {
        val pm = context.packageManager
        val packageName = context.packageName
        
        val dotCalcAlias = ComponentName(packageName, "$packageName.LauncherDotCalc")
        val calculatorAlias = ComponentName(packageName, "$packageName.LauncherCalculator")
        
        if (name == AppLauncherName.DOTCALC) {
            pm.setComponentEnabledSetting(dotCalcAlias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(calculatorAlias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        } else {
            pm.setComponentEnabledSetting(dotCalcAlias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
            pm.setComponentEnabledSetting(calculatorAlias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
        }
    }

    companion object {
        fun formatDecimal(bd: BigDecimal, settings: CalculatorSettings, stripZeros: Boolean = true, isBaseMode: Boolean = false): String {
            if (isBaseMode) return bd.toPlainString()

            val plain = if (stripZeros) bd.stripTrailingZeros().toPlainString() else bd.toPlainString()
            val decimalPart = if (plain.contains(".")) plain.substringAfter(".") else ""
            val wholePart = if (plain.contains(".")) plain.substringBefore(".") else plain
            val sign = if (bd.signum() == -1) "−" else ""
            val absoluteWholePart = if (wholePart.startsWith("-")) wholePart.substring(1) else wholePart

            val groupedWhole = if (!settings.groupingEnabled) {
                absoluteWholePart
            } else {
                when (settings.groupingSeparator) {
                    GroupingSeparator.INTERNATIONAL -> {
                        absoluteWholePart.reversed().chunked(3).joinToString(",").reversed()
                    }
                    GroupingSeparator.INDIAN -> {
                        if (absoluteWholePart.length <= 3) absoluteWholePart
                        else {
                            val lastThree = absoluteWholePart.substring(absoluteWholePart.length - 3)
                            val rest = absoluteWholePart.substring(0, absoluteWholePart.length - 3)
                            val restGrouped = rest.reversed().chunked(2).joinToString(",").reversed()
                            "$restGrouped,$lastThree"
                        }
                    }
                }
            }

            val decimalSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "," else "."
            val groupingSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "." else ","

            var finalResult = sign + groupedWhole.replace(",", groupingSep)
            if (decimalPart.isNotEmpty() || (plain.endsWith(".") && !stripZeros)) {
                finalResult += decimalSep + decimalPart
            }
            return finalResult
        }

        fun formatExpression(expr: String, settings: CalculatorSettings, isBaseMode: Boolean = false): String {
            if (isBaseMode || expr.isEmpty()) return expr
            
            val decimalSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "," else "."
            
            // Regex to find numbers (including scientific notation and decimals)
            val numberRegex = Regex("\\d+\\.\\d+|\\d+\\.|\\.\\d+|\\d+")
            
            return numberRegex.replace(expr) { matchResult ->
                val numStr = matchResult.value
                try {
                    val bd = BigDecimal(numStr)
                    // For expressions, we do NOT strip zeros to preserve user input
                    val formatted = formatDecimal(bd, settings, stripZeros = false, isBaseMode = isBaseMode)
                    
                    // Edge case: if the match is just ".", formatted might be "0" or something.
                    // But our regex doesn't match just ".".
                    
                    // Handle trailing dot preservation
                    if (numStr.endsWith(".") && !formatted.contains(decimalSep)) {
                        formatted + decimalSep
                    } else if (numStr.startsWith(".") && !formatted.contains(decimalSep)) {
                        decimalSep + formatted
                    } else {
                        formatted
                    }
                } catch (e: Exception) {
                    numStr
                }
            }
        }

        fun formatScientific(value: Double, settings: CalculatorSettings): String {
            val s = "%.10e".format(Locale.US, value).replace("e+0", "e").replace("e+", "e")
            val decimalSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "," else "."
            return s.replace(".", decimalSep)
        }

        fun formatEngineering(value: Double, settings: CalculatorSettings): String {
            if (value == 0.0 || value.isNaN() || value.isInfinite()) return "0"
            val exp = (kotlin.math.floor(kotlin.math.log10(kotlin.math.abs(value)) / 3.0) * 3.0).toInt()
            val mantissa = value / Math.pow(10.0, exp.toDouble())
            val s = "%.3f".format(Locale.US, mantissa)
            val decimalSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "," else "."
            return "${s.replace(".", decimalSep)} × 10^$exp"
        }

        fun normalizeInput(expr: String, settings: CalculatorSettings): String {
            val decimalSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "," else "."
            val groupingSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "." else ","
            // Remove spaces and grouping separators, then normalize decimal separator to dot
            return expr.replace(" ", "")
                .replace(groupingSep, "")
                .replace(decimalSep, ".")
        }
    }
}
