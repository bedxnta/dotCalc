package com.bedanta.dotcalc.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bedanta.dotcalc.logic.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ConversionSelectorSide { FROM, TO }

data class ConversionHistoryEntry(
    val fromValue: String,
    val fromUnit: String,
    val toValue: String,
    val toUnit: String
)

data class CalculatorUiState(
    val expression: String = "",
    val liveResult: MathValue = MathValue.Exact(),
    val history: List<HistoryEntry> = emptyList(),
    val isScientific: Boolean = false,
    val isInverse: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
    val showHistory: Boolean = false,
    val detailedResult: DetailedResult? = null,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val previewLocked: Boolean = false,
    val appMode: AppMode = AppMode.BASIC,
    val modeSelectorExpanded: Boolean = false,
    val conversionCategory: UnitCategory = UnitCategory.LENGTH,
    val conversionFromUnitId: String = "m",
    val conversionToUnitId: String = "km",
    val conversionFromValue: String = "",
    val conversionToValue: String = "",
    val conversionError: String? = null,
    val conversionPickerOpen: Boolean = false,
    val conversionPickerSide: ConversionSelectorSide? = null,
    val conversionActiveSide: ConversionSelectorSide = ConversionSelectorSide.FROM,
    val conversionHistory: List<ConversionHistoryEntry> = emptyList(),
    val settings: CalculatorSettings = CalculatorSettings(),
    val showSettings: Boolean = false,
    val currentScreen: CurrentScreen = CurrentScreen.CALCULATOR,
    val baseInput: String = ""
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val evaluator = ExpressionEvaluator()
    private val conversionEngine = UnitConversionEngine()
    private val historyManager = HistoryManager(application)
    private val settingsManager = SettingsManager(application)

    init {
        _uiState.update { 
            it.copy(
                history = historyManager.loadHistory(),
                settings = settingsManager.loadSettings()
            ) 
        }
        syncConversionFromInput()
    }

    fun toggleModeSelector() {
        _uiState.update { it.copy(modeSelectorExpanded = !it.modeSelectorExpanded) }
    }

    fun selectAppMode(mode: AppMode) {
        _uiState.update { it.copy(appMode = mode, modeSelectorExpanded = false) }
        if (mode == AppMode.CONVERT) syncConversionFromInput()
    }

    fun setConversionCategory(category: UnitCategory) {
        val units = conversionEngine.unitsFor(category)
        val from = units.firstOrNull { it.id == _uiState.value.conversionFromUnitId } ?: units.first()
        val to = units.firstOrNull { it.id == _uiState.value.conversionToUnitId } ?: units.getOrNull(1) ?: from
        _uiState.update {
            it.copy(
                conversionCategory = category,
                conversionFromUnitId = from.id,
                conversionToUnitId = to.id,
                conversionError = null,
                conversionPickerOpen = false,
                conversionPickerSide = null
            )
        }
        syncConversionFromInput()
    }

    fun setConversionActiveSide(side: ConversionSelectorSide) {
        _uiState.update { it.copy(conversionActiveSide = side) }
    }

    fun openConversionPicker(side: ConversionSelectorSide) {
        _uiState.update { current ->
            if (side == ConversionSelectorSide.TO) {
                current.copy(conversionPickerOpen = true, conversionPickerSide = side)
            } else {
                current.copy(conversionPickerOpen = true, conversionPickerSide = side, conversionActiveSide = side)
            }
        }
    }

    fun dismissConversionPicker() {
        _uiState.update { it.copy(conversionPickerOpen = false, conversionPickerSide = null) }
    }

    fun setConversionUnit(unitId: String) {
        val state = _uiState.value
        val units = conversionEngine.unitsFor(state.conversionCategory)
        val unit = units.firstOrNull { it.id == unitId } ?: return
        if (state.conversionPickerSide == ConversionSelectorSide.FROM) {
            _uiState.update { it.copy(conversionFromUnitId = unit.id, conversionPickerOpen = false, conversionPickerSide = null, conversionError = null) }
            syncConversionFromInput()
        } else {
            _uiState.update { it.copy(conversionToUnitId = unit.id, conversionPickerOpen = false, conversionPickerSide = null, conversionError = null) }
            syncConversionFromInput()
        }
    }

    fun onConversionDigit(digit: String) {
        val state = _uiState.value
        val side = state.conversionActiveSide
        if (side == ConversionSelectorSide.FROM) {
            val next = state.conversionFromValue + digit
            _uiState.update { it.copy(conversionFromValue = next, conversionError = null) }
            syncConversionFromInput()
        } else {
            val next = state.conversionToValue + digit
            _uiState.update { it.copy(conversionToValue = next, conversionError = null) }
            syncConversionToInput()
        }
    }

    fun onConversionBackspace() {
        val state = _uiState.value
        val side = state.conversionActiveSide
        val current = if (side == ConversionSelectorSide.FROM) state.conversionFromValue else state.conversionToValue
        if (current.isEmpty()) return
        val next = current.dropLast(1)
        if (side == ConversionSelectorSide.FROM) {
            _uiState.update { it.copy(conversionFromValue = next, conversionError = null) }
            syncConversionFromInput()
        } else {
            _uiState.update { it.copy(conversionToValue = next, conversionError = null) }
            syncConversionToInput()
        }
    }

    fun onConversionDecimal() {
        val state = _uiState.value
        val current = if (state.conversionActiveSide == ConversionSelectorSide.FROM) state.conversionFromValue else state.conversionToValue
        if (!current.contains(".")) {
            onConversionDigit(".")
        }
    }

    fun onConversionClear() {
        _uiState.update { it.copy(conversionFromValue = "", conversionToValue = "", conversionError = null) }
    }

    fun onConversionToggleSign() {
        val state = _uiState.value
        val side = state.conversionActiveSide
        val current = if (side == ConversionSelectorSide.FROM) state.conversionFromValue else state.conversionToValue
        val next = when {
            current.isBlank() -> "-"
            current == "-" -> ""
            current.startsWith("-") -> current.removePrefix("-")
            else -> "-$current"
        }
        if (side == ConversionSelectorSide.FROM) {
            _uiState.update { it.copy(conversionFromValue = next, conversionError = null) }
            syncConversionFromInput()
        } else {
            _uiState.update { it.copy(conversionToValue = next, conversionError = null) }
            syncConversionToInput()
        }
    }

    fun swapConversionUnits() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                conversionFromUnitId = state.conversionToUnitId,
                conversionToUnitId = state.conversionFromUnitId,
                conversionFromValue = state.conversionToValue,
                conversionToValue = state.conversionFromValue,
                conversionError = null
            )
        }
        syncConversionFromInput()
    }

    fun onBaseDigit(digit: String) {
        val state = _uiState.value
        _uiState.update { it.copy(baseInput = state.baseInput + digit) }
    }

    fun onBaseOperator(op: String) {
        onBaseDigit(op)
    }

    fun onBaseBackspace() {
        val state = _uiState.value
        if (state.baseInput.isNotEmpty()) {
            _uiState.update { it.copy(baseInput = state.baseInput.dropLast(1)) }
        }
    }

    fun onBaseClear() {
        _uiState.update { it.copy(baseInput = "") }
    }

    private fun syncConversionFromInput() {
        val state = _uiState.value
        val fromDef = conversionEngine.unitsFor(state.conversionCategory).firstOrNull { it.id == state.conversionFromUnitId }
            ?: conversionEngine.unitsFor(state.conversionCategory).first()
        val toDef = conversionEngine.unitsFor(state.conversionCategory).firstOrNull { it.id == state.conversionToUnitId }
            ?: conversionEngine.unitsFor(state.conversionCategory).getOrNull(1) ?: fromDef
        val parsed = conversionEngine.parseInput(state.conversionFromValue, state.settings)
        if (state.conversionFromValue.isBlank() || state.conversionFromValue == "-") {
            _uiState.update { it.copy(conversionToValue = "", conversionError = null) }
            return
        }
        if (parsed == null) {
            _uiState.update { it.copy(conversionError = "Invalid input") }
            return
        }
        val result = conversionEngine.convert(parsed, fromDef, toDef)
        if (result.error != null || result.value == null) {
            _uiState.update { it.copy(conversionToValue = "", conversionError = result.error ?: "Invalid conversion") }
            return
        }
        _uiState.update {
            it.copy(
                conversionToValue = conversionEngine.formatNumber(result.value),
                conversionError = null
            )
        }
        addConversionHistoryIfNeeded(fromDef, toDef, parsed, result.value)
    }

    private fun syncConversionToInput() {
        val state = _uiState.value
        val fromDef = conversionEngine.unitsFor(state.conversionCategory).firstOrNull { it.id == state.conversionFromUnitId }
            ?: conversionEngine.unitsFor(state.conversionCategory).first()
        val toDef = conversionEngine.unitsFor(state.conversionCategory).firstOrNull { it.id == state.conversionToUnitId }
            ?: conversionEngine.unitsFor(state.conversionCategory).getOrNull(1) ?: fromDef
        val parsed = conversionEngine.parseInput(state.conversionToValue, state.settings)
        if (state.conversionToValue.isBlank() || state.conversionToValue == "-") {
            _uiState.update { it.copy(conversionFromValue = "", conversionError = null) }
            return
        }
        if (parsed == null) {
            _uiState.update { it.copy(conversionError = "Invalid input") }
            return
        }
        val reverse = conversionEngine.convert(parsed, toDef, fromDef)
        if (reverse.error != null || reverse.value == null) {
            _uiState.update { it.copy(conversionFromValue = "", conversionError = reverse.error ?: "Invalid conversion") }
            return
        }
        _uiState.update {
            it.copy(
                conversionFromValue = conversionEngine.formatNumber(reverse.value),
                conversionError = null
            )
        }
        addConversionHistoryIfNeeded(fromDef, toDef, reverse.value, parsed)
    }

    private fun addConversionHistoryIfNeeded(fromDef: UnitDefinition, toDef: UnitDefinition, fromValue: Double, toValue: Double) {
        val state = _uiState.value
        val entry = ConversionHistoryEntry(
            fromValue = conversionEngine.formatNumber(fromValue),
            fromUnit = fromDef.symbol,
            toValue = conversionEngine.formatNumber(toValue),
            toUnit = toDef.symbol
        )
        val existing = state.conversionHistory
        val updated = (listOf(entry) + existing).distinctBy { it.fromValue + it.fromUnit + it.toValue + it.toUnit }.take(8)
        _uiState.update { it.copy(conversionHistory = updated) }
    }

    fun clearConversionHistory() {
        _uiState.update { it.copy(conversionHistory = emptyList()) }
    }

    fun applyConversionHistory(entry: ConversionHistoryEntry) {
        val category = _uiState.value.conversionCategory
        val units = conversionEngine.unitsFor(category)
        val fromUnit = units.firstOrNull { it.symbol == entry.fromUnit || it.label.contains(entry.fromUnit, ignoreCase = true) } ?: units.first()
        val toUnit = units.firstOrNull { it.symbol == entry.toUnit || it.label.contains(entry.toUnit, ignoreCase = true) } ?: units.getOrNull(1) ?: fromUnit
        _uiState.update {
            it.copy(
                conversionFromUnitId = fromUnit.id,
                conversionToUnitId = toUnit.id,
                conversionFromValue = entry.fromValue,
                conversionToValue = entry.toValue,
                conversionError = null
            )
        }
    }

    private fun appendText(text: String) {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                expression = state.expression + text,
                isError = false,
                errorMessage = null,
                previewLocked = false
            )
        }
        updateLiveResult()
    }

    fun onDigit(digit: String) {
        appendText(digit)
    }

    fun onOperator(op: String) {
        val current = _uiState.value.expression
        if (current.isEmpty() && op == "−") {
            appendText("−")
            return
        }
        if (current.isNotEmpty()) {
            appendText(op)
        }
    }

    fun onFunction(func: String) {
        val label = when (func) {
            "x²" -> "^2"
            "xʸ" -> "^"
            "eˣ" -> "e^"
            "10ˣ" -> "10^"
            "1/x" -> "1÷"
            "√" -> "√("
            "sin", "cos", "tan", "sin⁻¹", "cos⁻¹", "tan⁻¹", "ln", "log" -> "$func("
            else -> func
        }
        appendText(label)
    }

    fun onParenthesis() {
        val current = _uiState.value.expression
        val last = current.lastOrNull()
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }
        val toAdd = if (openCount > closeCount && (last?.isDigit() == true || last == ')' || last == 'π' || last == 'e' || last == '%')) {
            ")"
        } else {
            "("
        }
        appendText(toAdd)
    }

    fun onDecimal() {
        val state = _uiState.value
        val current = state.expression
        val lastPart = current.split("+", "−", "×", "÷", "(", ")", "^", "√").last()
        if (!lastPart.contains(".")) {
            appendText(".")
        }
    }

    fun onClear() {
        _uiState.update { it.copy(expression = "", liveResult = MathValue.Exact(), isError = false, errorMessage = null, previewLocked = false) }
    }

    fun onBackspace() {
        val state = _uiState.value
        if (state.expression.isEmpty()) return
        _uiState.update {
            it.copy(expression = state.expression.dropLast(1), isError = false, errorMessage = null, previewLocked = false)
        }
        updateLiveResult()
    }

    fun onEquals() {
        val state = _uiState.value
        if (state.expression.isEmpty()) return

        val result = evaluator.evaluate(state.expression, state.angleMode, state.appMode, state.settings)
        if (result is MathValue.Undefined || result.numericValue.isNaN() || result.numericValue.isInfinite()) {
            val errorMessage = evaluator.getErrorMessage(state.expression, state.angleMode, state.appMode, state.settings)
            _uiState.update { it.copy(isError = true, errorMessage = errorMessage, liveResult = MathValue.Undefined, previewLocked = true) }
            return
        }

        val formatted = evaluator.formatResult(result, state.settings)
        if (formatted == "Undefined" || formatted.startsWith("Error:")) {
            val errorMessage = evaluator.getErrorMessage(state.expression, state.angleMode, state.appMode, state.settings)
            _uiState.update { it.copy(isError = true, errorMessage = errorMessage, liveResult = MathValue.Undefined, previewLocked = true) }
            return
        }
        val newEntry = HistoryEntry(state.expression, formatted)
        val newHistory = (state.history + newEntry).takeLast(50)
        historyManager.saveHistory(newHistory)

        _uiState.update { it.copy(
            expression = formatted,
            liveResult = MathValue.Undefined,
            history = newHistory,
            isError = false,
            errorMessage = null,
            previewLocked = true
        ) }
    }

    fun onResultClick() {
        val state = _uiState.value
        val valueToInspect = if (!state.liveResult.numericValue.isNaN()) {
             state.liveResult
        } else if (state.expression.isNotEmpty()) {
             evaluator.evaluate(state.expression, state.angleMode, state.appMode)
        } else return

        if (!valueToInspect.numericValue.isNaN()) {
            val detailed = evaluator.getDetailedResult(state.expression, valueToInspect, state.settings)
            _uiState.update { it.copy(detailedResult = detailed) }
        }
    }

    fun onHistoryItemClick(entry: HistoryEntry) {
        _uiState.update { it.copy(expression = entry.expression, showHistory = false, isError = false, errorMessage = null, previewLocked = false) }
        updateLiveResult()
    }

    fun dismissDetailedResult() {
        _uiState.update { it.copy(detailedResult = null) }
    }

    fun toggleScientific() {
        _uiState.update { it.copy(isScientific = !it.isScientific) }
    }

    fun toggleInverse() {
        _uiState.update { it.copy(isInverse = !it.isInverse) }
    }

    fun toggleAngleMode() {
        _uiState.update { it.copy(angleMode = if (it.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG, isError = false, errorMessage = null) }
        updateLiveResult()
    }

    fun toggleHistory() {
        _uiState.update { it.copy(showHistory = !it.showHistory, showSettings = false) }
    }

    fun toggleSettings() {
        _uiState.update { it.copy(currentScreen = if (it.currentScreen == CurrentScreen.CALCULATOR) CurrentScreen.SETTINGS else CurrentScreen.CALCULATOR, showHistory = false) }
    }

    fun updateSettings(settings: CalculatorSettings) {
        settingsManager.saveSettings(settings)
        _uiState.update { it.copy(settings = settings) }
        updateLiveResult()
    }

    fun clearHistory() {
        historyManager.saveHistory(emptyList())
        _uiState.update { it.copy(history = emptyList()) }
    }

    private fun updateLiveResult() {
        val state = _uiState.value
        if (state.previewLocked) {
            _uiState.update { it.copy(liveResult = MathValue.Undefined) }
            return
        }
        val expr = state.expression
        if (expr.isEmpty()) {
            _uiState.update { it.copy(liveResult = MathValue.Exact()) }
            return
        }
        val result = evaluator.evaluate(expr, state.angleMode, state.appMode, state.settings)
        val liveValue = if (result is MathValue.Undefined || result.numericValue.isNaN() || result.numericValue.isInfinite()) {
            MathValue.Undefined
        } else {
            result
        }
        _uiState.update { it.copy(liveResult = liveValue) }
    }
    private fun MathValue.isZero(): Boolean = this is MathValue.Exact && this.isZero()
}
