# Implementation Plan - Full Number Formatting System Audit and Fix

Perform a comprehensive audit of the number formatting pipeline to ensure mathematical precision is strictly separated from visual representation, and fix all identified bugs consistently across all modes.

## User Review Required

> [!IMPORTANT]
> - **Answer Preview:** Now matches the final result's formatting exactly, including grouping and decimal separators.
> - **Trailing Zeros:** Typing `1.0` or `1.00` now preserves the zeros in the display and preview.
> - **Base Mode Guard:** BASE mode is now strictly isolated from global formatting settings (no grouping/decimal separators in Hex/Bin/Oct/Dec).
> - **Indian Grouping:** Correctly implements the `2,2,3` grouping standard.

## Proposed Changes

### Core Logic

#### [MODIFY] [SettingsManager.kt](file:///home/bd/AndroidStudioProjects/dotCalc/app/src/main/java/com/bedanta/dotcalc/logic/SettingsManager.kt)
- **`formatDecimal`**:
    - Added `stripZeros` flag (default `true` for results, `false` for expression editing).
    - Fixed negative number grouping (groups digits, then prepends sign).
    - Hardened Indian grouping logic for very large numbers.
- **`formatExpression`**:
    - Uses a robust regex to identify numbers within an expression.
    - Preserves trailing decimal separators during active input.
- **`normalizeInput`**:
    - Correctly handles both Dot and Comma roles for grouping vs decimal separation.

#### [MODIFY] [ExpressionEvaluator.kt](file:///home/bd/AndroidStudioProjects/dotCalc/app/src/main/java/com/bedanta/dotcalc/logic/ExpressionEvaluator.kt)
- **`formatResult`**: Unified to use `SettingsManager.formatDecimal` with appropriate flags.

#### [MODIFY] [UnitConversionEngine.kt](file:///home/bd/AndroidStudioProjects/dotCalc/app/src/main/java/com/bedanta/dotcalc/logic/UnitConversionEngine.kt)
- **`formatNumber`**: Simplified to return a locale-neutral raw string, allowing the UI layer to handle localization via the central formatter.

### UI Layer

#### [MODIFY] [CalculatorDisplay.kt](file:///home/bd/AndroidStudioProjects/dotCalc/app/src/main/java/com/bedanta/dotcalc/ui/components/CalculatorDisplay.kt)
- **Expression Rendering**: Uses `SettingsManager.formatExpression` to localize numbers as they are typed.
- **Preview Rendering**: Uses the unified `formatResult` logic, ensuring perfect parity with the final answer.
- **Blinking Cursor**: Integrated correctly into the formatted text row.

#### [MODIFY] [UnitConversionScreen.kt](file:///home/bd/AndroidStudioProjects/dotCalc/app/src/main/java/com/bedanta/dotcalc/ui/UnitConversionScreen.kt)
- Updated `ConversionFieldCard` to use the central `formatExpression` logic for both "FROM" and "TO" fields.

#### [MODIFY] [CalculatorViewModel.kt](file:///home/bd/AndroidStudioProjects/dotCalc/app/src/main/java/com/bedanta/dotcalc/ui/CalculatorViewModel.kt)
- **`onDecimal`**: Improved to detect existing separators in the current token regardless of the selected locale (Dot or Comma).

### Base Mode Isolation

#### [MODIFY] [BaseCalculatorScreen.kt](file:///home/bd/AndroidStudioProjects/dotCalc/app/src/main/java/com/bedanta/dotcalc/ui/base/BaseCalculatorScreen.kt)
- Verified that all number displays explicitly bypass localized formatting.

## Verification Plan

### Automated Tests
- **`SettingsManagerTest.kt`**: Expanded to cover International vs Indian grouping across Dot and Comma decimal modes, including negative numbers and trailing zeros.

### Manual Verification
1.  **Grouping in Preview:** Enter `1234567 + 1`. Verify preview shows `1,234,568`.
2.  **Trailing Zeros:** Type `1.0`. Verify display shows `1.0`. Verify preview shows `1.0` (or `1,0`).
3.  **Base Mode:** Switch to BASE. Enter `1234567`. Verify NO commas appear.
4.  **Role Swap:** Set Decimal to COMMA. Enter `1.234,56`. Verify parsing and display are correct.
5.  **Conversions:** Convert `1234567 m` to `km`. Verify both fields use correct grouping and separators.
