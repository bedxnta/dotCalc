# Walkthrough - Formatting Audit and Settings Redesign

I have completed a comprehensive audit and overhaul of the number formatting system, transitioning to a dedicated full-screen experience and implementing robust, locale-aware logic.

## Changes Made

### Core Architecture & Logic
- **Consolidated Formatter**: Centralized all number formatting logic into `SettingsManager` to ensure consistency across Basic, Convert, and Base modes.
- **Intelligent Input & Parsing**:
    - The calculator now uses a "Normalize-Parse-Calculate-Format" pipeline.
    - User input is automatically normalized (e.g., `1.234,56` -> `1234.56`) before evaluation.
    - The keypad's decimal button label (`.` or `,`) updates dynamically based on settings.
- **Answer Preview Sync**: Fixed the bug where the answer preview ignored the decimal comma setting. The preview now uses the exact same logic as the final result.
- **Base Mode Protection**: Verified that `BASE` mode explicitly ignores grouping and decimal settings to maintain standard programmer representations (e.g., `ABCDEF` remains unformatted).

### Settings Implementation
- **Dedicated Settings Screen**: Replaced the settings modal with a full-screen UI that maintains the GLYPH aesthetic and preserves calculator state during navigation.
- **Accent Mode (RED/WHITE)**: Replaced hardcoded colors with theme-aware `tertiary` references. The monochrome white look now applies globally to cursors, indicators, and buttons.
- **Advanced Grouping**:
    - **Grouping Toggle**: Master control to enable/disable digit grouping.
    - **Indian Grouping**: Implemented the correct `2,2,3` grouping standard (e.g., `12,34,567.89`).
    - **Role Swap**: Automatically swaps separator roles (e.g., Comma decimal uses Dot grouping: `1.234.567,89`).
- **Haptic Feedback**: Global toggle for app-generated tactile feedback.

### Visual Improvements
- **Blinking Cursor**: Added a custom-rendered blinking cursor to input fields for better focus visibility. The cursor color now dynamically follows the selected **Accent Mode** (Red or White), ensuring it remains consistent with the rest of the UI.
- **Tap-to-Position Cursor**: Users can now tap anywhere within the input fields to move the cursor. This is implemented using `TextLayoutResult` to map screen coordinates to mathematical character indices.
- **Stability Fixes**: Resolved a crash (`IllegalArgumentException`) that occurred when the text layout was temporarily out of sync with the input text during rapid editing or setting changes.
- **Layout Stability**: Fixed layout shifts caused by long inputs or changing separators.
- **Consistent Typography**: Applied `NDotFontFamily` to all header elements, drawer titles, and Settings headers.
- **Accessibility & Motion**:
    - **Reduce Animations**: Added a global setting to disable or minimize scaling and movement effects, providing a faster and more stable experience.
    - **Fluid Settings**: Integrated animated toggles and color transitions in the Settings screen that respect your motion preferences.
- **Launcher Customization**:
    - **App Name Selection**: Added a new feature in Settings allowing users to choose between **dotCalc** and **Calculator** as the name shown in the Android launcher. This uses advanced activity aliasing to update the system branding without requiring a re-install.
    - **Custom App Icon**: Integrated a brand-new adaptive icon (`ic_dcalc`) with support for adaptive shapes, standard/round launcher styles, and Android 13+ themed (monochrome) icons.

## Verification Results

### Logic & Formatting
- **Decimal Comma**: Verified `15 ÷ 4` displays as `3,75` in both preview and final result when Comma mode is active.
- **Indian Grouping**: Verified `1234567` displays as `12,34,567`.
- **Negative Values**: Verified `-1234567.89` groups correctly as `−1,234,567.89`.
- **Monochrome Mode**: Verified that indicators and cursors turn white in "WHITE" accent mode.

### System Stability
- All settings are persistent across app restarts via `SharedPreferences`.
- Mutual exclusion between Settings and History drawers ensures a focused UI.
- Ran `assembleDebug` successfully.
