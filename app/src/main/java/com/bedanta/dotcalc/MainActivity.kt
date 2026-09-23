package com.bedanta.dotcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bedanta.dotcalc.ui.CalculatorScreen
import com.bedanta.dotcalc.ui.CalculatorViewModel
import com.bedanta.dotcalc.ui.theme.DotCalcTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestMaximumRefreshRate()
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            DotCalcTheme(accentMode = uiState.settings.accentMode) {
                CalculatorScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestMaximumRefreshRate()
    }

    private fun requestMaximumRefreshRate() {
        val modes = windowManager.defaultDisplay.supportedModes
        val maximumMode = modes.maxByOrNull { it.refreshRate } ?: return

        window.attributes = window.attributes.apply {
            preferredDisplayModeId = maximumMode.modeId
            preferredRefreshRate = maximumMode.refreshRate
        }
    }
}
