package com.xauusd.analyst.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore(context: Context) {
    private val p = context.getSharedPreferences("xau_settings", Context.MODE_PRIVATE)
    private val _flow = MutableStateFlow(load())
    val flow: StateFlow<AppSettings> = _flow.asStateFlow()

    private fun d(key: String, def: Double) = p.getString(key, null)?.toDoubleOrNull() ?: def
    private fun s(key: String, def: String) = p.getString(key, def) ?: def

    private fun load(): AppSettings {
        val def = AppSettings()
        return AppSettings(
            balance = d("balance", def.balance),
            riskPercent = d("risk", def.riskPercent),
            maxPositions = p.getInt("maxPos", def.maxPositions),
            dailyLossLimit = d("dailyLoss", def.dailyLossLimit),
            maxSpread = d("maxSpread", def.maxSpread),
            spreadWarning = p.getBoolean("spreadWarn", def.spreadWarning),
            newsWarning = p.getBoolean("newsWarn", def.newsWarning),
            accountCurrency = s("currency", def.accountCurrency),
            contractSize = s("contract", ""),
            tickSize = s("tickSize", ""),
            tickValue = s("tickValue", ""),
            minLot = s("minLot", ""),
            lotStep = s("lotStep", "")
        )
    }

    fun update(v: AppSettings) {
        p.edit()
            .putString("balance", v.balance.toString())
            .putString("risk", v.riskPercent.toString())
            .putInt("maxPos", v.maxPositions)
            .putString("dailyLoss", v.dailyLossLimit.toString())
            .putString("maxSpread", v.maxSpread.toString())
            .putBoolean("spreadWarn", v.spreadWarning)
            .putBoolean("newsWarn", v.newsWarning)
            .putString("currency", v.accountCurrency)
            .putString("contract", v.contractSize)
            .putString("tickSize", v.tickSize)
            .putString("tickValue", v.tickValue)
            .putString("minLot", v.minLot)
            .putString("lotStep", v.lotStep)
            .apply()
        _flow.value = v
    }
}
