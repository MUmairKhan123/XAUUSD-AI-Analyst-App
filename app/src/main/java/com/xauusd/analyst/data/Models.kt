package com.xauusd.analyst.data

import java.util.Locale

enum class Timeframe(val label: String, val title: String, val purpose: String) {
    H4("H4", "Major market trend", "Major directional bias"),
    H1("H1", "Key levels & liquidity", "Key levels, swing points, BSL/SSL, EQH/EQL"),
    M15("M15", "Market structure", "Market structure, BOS/CHoCH, directional confirmation"),
    M5("M5", "SMC setup", "Setup identification: FVG and Order Blocks"),
    M1("M1", "Entry confirmation", "Precise entry trigger with candle-close confirmation")
}

enum class Decision { BUY, SELL, WAIT }

fun decisionOf(s: String?): Decision =
    runCatching { Decision.valueOf((s ?: "").trim().uppercase()) }.getOrDefault(Decision.WAIT)

/** All user-editable settings. Contract specs are intentionally EMPTY by default (never invented). */
data class AppSettings(
    val balance: Double = 100.0,
    val riskPercent: Double = 2.0,
    val maxPositions: Int = 2,
    val dailyLossLimit: Double = 6.0,
    val maxSpread: Double = 0.40,
    val spreadWarning: Boolean = true,
    val newsWarning: Boolean = true,
    val accountCurrency: String = "USD",
    val contractSize: String = "",
    val tickSize: String = "",
    val tickValue: String = "",
    val minLot: String = "",
    val lotStep: String = ""
)

/** Parses "12.5" or "12,5". Returns null when blank/invalid. */
fun String.num(): Double? = trim().replace(",", ".").toDoubleOrNull()

fun money(x: Double): String = String.format(Locale.US, "%.2f", x)

fun fmt(x: Double): String =
    if (x % 1.0 == 0.0) x.toInt().toString() else String.format(Locale.US, "%.2f", x)

object ImagePaths {
    fun encode(map: Map<Timeframe, String>): String =
        map.entries.joinToString(";") { "${it.key.name}=${it.value}" }

    fun decode(s: String): Map<Timeframe, String> =
        s.split(";").mapNotNull { part ->
            val i = part.indexOf('=')
            if (i <= 0) return@mapNotNull null
            val tf = Timeframe.values().firstOrNull { it.name == part.substring(0, i) }
                ?: return@mapNotNull null
            tf to part.substring(i + 1)
        }.toMap()
}
