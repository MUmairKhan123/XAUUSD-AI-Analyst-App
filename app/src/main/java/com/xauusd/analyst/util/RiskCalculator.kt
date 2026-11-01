package com.xauusd.analyst.util

import com.xauusd.analyst.data.AppSettings
import com.xauusd.analyst.data.money
import com.xauusd.analyst.data.num
import kotlin.math.abs
import kotlin.math.floor

object RiskCalculator {

    fun riskAmount(balance: Double, riskPercent: Double): Double = balance * riskPercent / 100.0

    sealed class LotResult {
        /** [lots] is the maximum lot size that keeps the loss at SL within the planned risk. */
        data class Ok(
            val lots: Double,
            val lossPerLotAtSl: Double,
            val notes: List<String>
        ) : LotResult()

        data class Missing(val items: List<String>) : LotResult()
    }

    /**
     * Lot size = planned risk / (SL distance / tickSize * tickValue).
     * Nothing is assumed: every broker value must be supplied by the user from the
     * XM MT5 "Specification" screen. Missing values => [LotResult.Missing].
     */
    fun lotSize(s: AppSettings, slDistance: Double?): LotResult {
        val missing = mutableListOf<String>()
        val contract = s.contractSize.num()?.takeIf { it > 0 }
        val tickSize = s.tickSize.num()?.takeIf { it > 0 }
        val tickValue = s.tickValue.num()?.takeIf { it > 0 }
        if (contract == null) missing += "Contract size (from XM MT5 > XAUUSD > Specification)"
        if (tickSize == null) missing += "Tick size (from Specification)"
        if (tickValue == null) missing += "Tick value in your account currency (from Specification)"
        if (s.accountCurrency.isBlank()) missing += "Account currency"
        if (slDistance == null || slDistance <= 0) missing += "Stop-loss distance in price units (entry to SL)"
        if (missing.isNotEmpty()) return LotResult.Missing(missing)

        val risk = riskAmount(s.balance, s.riskPercent)
        val ticks = slDistance!! / tickSize!!
        val lossPerLot = ticks * tickValue!!
        var lots = risk / lossPerLot
        val notes = mutableListOf<String>()

        if (s.accountCurrency.trim().equals("USD", ignoreCase = true)) {
            val expected = contract!! * tickSize
            if (abs(expected - tickValue) / tickValue > 0.05) {
                notes += "Tick value (${money(tickValue)}) does not match contract size x tick size " +
                    "(${money(expected)}). Re-check the numbers in the XM Specification."
            }
        }
        s.lotStep.num()?.takeIf { it > 0 }?.let { step ->
            lots = floor(lots / step + 1e-9) * step
        } ?: run { notes += "Lot step not entered: round the result DOWN to XM's lot step." }

        s.minLot.num()?.takeIf { it > 0 }?.let { min ->
            if (lots < min) notes += "Minimum lot ($min) would risk MORE than your planned " +
                "${money(risk)} ${s.accountCurrency} at this SL distance. Skip or use a tighter valid SL."
        }
        return LotResult.Ok(lots, lossPerLot, notes)
    }

    private val numberRegex = Regex("""\d+(?:\.\d+)?""")

    private fun numbers(text: String): List<Double> =
        numberRegex.findAll(text.replace(",", "")).mapNotNull { it.value.toDoubleOrNull() }.toList()

    /** Conservative SL distance: the farthest point of the entry zone from the SL. */
    fun slDistance(entryText: String, slText: String): Double? {
        val sl = numbers(slText).firstOrNull() ?: return null
        val entries = numbers(entryText)
        if (entries.isEmpty()) return null
        return entries.maxOf { abs(it - sl) }.takeIf { it > 0 }
    }
}
