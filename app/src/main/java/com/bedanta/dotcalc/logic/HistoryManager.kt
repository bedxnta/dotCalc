package com.bedanta.dotcalc.logic

import android.content.Context
import android.content.SharedPreferences

data class HistoryEntry(val expression: String, val result: String)

class HistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dot_calc_prefs", Context.MODE_PRIVATE)

    fun saveHistory(history: List<HistoryEntry>) {
        val historyString = history.joinToString("|||") { "${it.expression}@@@${it.result}" }
        prefs.edit().putString("calculation_history", historyString).apply()
    }

    fun loadHistory(): List<HistoryEntry> {
        val historyString = prefs.getString("calculation_history", "") ?: ""
        if (historyString.isEmpty()) return emptyList()
        return historyString.split("|||").mapNotNull {
            val parts = it.split("@@@")
            if (parts.size == 2) HistoryEntry(parts[0], parts[1]) else null
        }
    }
}
