package com.xauusd.analyst.util

import com.xauusd.analyst.data.AppSettings
import com.xauusd.analyst.data.Timeframe
import com.xauusd.analyst.data.fmt
import com.xauusd.analyst.data.money

object PromptBuilder {

    fun build(s: AppSettings, uploaded: Set<Timeframe>, notes: String): String = buildString {
        val cur = s.accountCurrency.ifBlank { "USD" }
        val risk = RiskCalculator.riskAmount(s.balance, s.riskPercent)

        appendLine("ROLE")
        appendLine("Act as a senior XAUUSD (Gold) analyst specialising in Smart Money Concepts (SMC) and multi-timeframe analysis. I trade MANUALLY on XM MT5. You do not execute trades and you must not promise outcomes.")
        appendLine()
        appendLine("ACCOUNT CONTEXT")
        appendLine("- Broker: XM | Platform: MT5 | Symbol: XAUUSD | Style: Scalping | Execution: manual only")
        appendLine("- Balance: $cur ${money(s.balance)} | Risk per trade: ${fmt(s.riskPercent)}% | Planned risk: $cur ${money(risk)}")
        appendLine("- Max simultaneous open positions: ${s.maxPositions} | Daily loss limit: $cur ${money(s.dailyLossLimit)}")
        appendLine("- Forbidden: martingale, grid trading, averaging down.")
        appendLine()
        appendLine("ATTACHED SCREENSHOTS (file names start with the timeframe)")
        Timeframe.values().forEachIndexed { i, tf ->
            val status = if (tf in uploaded) "ATTACHED" else "MISSING"
            appendLine("${i + 1}. ${tf.label} - ${tf.purpose}: $status")
        }
        val missing = Timeframe.values().filter { it !in uploaded }
        if (missing.isNotEmpty()) {
            appendLine("Missing: ${missing.joinToString { it.label }}. State what cannot be assessed without them. If the missing timeframe is needed for confirmation, the decision must be WAIT.")
        }
        appendLine()
        appendLine("TIMEFRAME ROLES")
        appendLine("- H4: major directional bias.")
        appendLine("- H1: key levels, swing points and liquidity.")
        appendLine("- M15: market structure and directional confirmation.")
        appendLine("- M5: setup identification.")
        appendLine("- M1: entry trigger confirmation.")
        appendLine()
        appendLine("ANALYSIS FRAMEWORK - assess each item from the screenshots")
        appendLine("1. Market trend and market structure (HH/HL/LH/LL).")
        appendLine("2. Smart Money Concepts (SMC) context.")
        appendLine("3. Fair Value Gaps (FVG) - mark relevant ones and whether they are mitigated.")
        appendLine("4. Order Blocks (OB) - bullish/bearish, with price zones.")
        appendLine("5. Break of Structure (BOS) and 6. Change of Character (CHoCH) - on which timeframe, and whether confirmed by a candle CLOSE.")
        appendLine("7. Buy-side liquidity (BSL) and 8. Sell-side liquidity (SSL).")
        appendLine("9. Equal highs / equal lows (EQH/EQL).")
        appendLine("10. Liquidity sweeps (has liquidity been taken? in which direction?).")
        appendLine("11. Premium and discount zones (relative to the current dealing range).")
        appendLine("12. Support and resistance.")
        appendLine("13. Displacement and retests.")
        appendLine("14. Candle-close confirmation (wicks alone are not confirmation).")
        appendLine("15. Multi-timeframe confluence (H4 -> H1 -> M15 -> M5 -> M1 alignment).")
        appendLine()
        appendLine("STRICT RULES")
        appendLine("- Use ONLY what is visible in the screenshots. Do not invent prices, candles, indicators or news. If a price scale or label is unreadable, say so.")
        appendLine("- You have no live market data and must not claim any. Do not claim to know current news.")
        appendLine("- If screenshots conflict, are unclear, or confirmation is missing, the decision MUST be WAIT. Never force a trade.")
        appendLine("- No guarantees, no accuracy percentages, no 'sure win' language. Confidence is qualitative only (Low / Medium / High) with reasons.")
        appendLine("- Do not recommend martingale, grid, averaging down, or exceeding ${s.maxPositions} open positions.")
        appendLine("- Do not give a lot size. I calculate it myself from verified XM contract specifications.")
        appendLine("- Calculate risk-to-reward honestly for TP1, TP2 and TP3 and flag it if it is poor.")
        appendLine("- Remind me to check the live spread and high-impact economic news myself before any entry.")
        appendLine()
        appendLine("REQUIRED OUTPUT FORMAT (use these headings in this order)")
        appendLine("1. Overall market bias")
        appendLine("2. H4 and H1 trend")
        appendLine("3. M15 market structure")
        appendLine("4. M5 setup")
        appendLine("5. M1 entry confirmation")
        appendLine("6. Liquidity levels")
        appendLine("7. Relevant FVG and Order Blocks")
        appendLine("8. Final decision: BUY / SELL / WAIT")
        appendLine("9. Entry zone")
        appendLine("10. Stop Loss (SL)")
        appendLine("11. Take Profit 1 (TP1)")
        appendLine("12. Take Profit 2 (TP2)")
        appendLine("13. Take Profit 3 (TP3)")
        appendLine("14. Risk-to-Reward ratio")
        appendLine("15. Setup invalidation level")
        appendLine("16. Conditions required before entry")
        appendLine("17. Reasons to avoid the trade")
        appendLine("18. Qualitative confidence assessment")
        appendLine()
        appendLine("FINISH with this exact plain-text block (no markdown, one key per line; use N/A when WAIT):")
        appendLine("DECISION: BUY or SELL or WAIT")
        appendLine("ENTRY: <price or zone>")
        appendLine("SL: <price>")
        appendLine("TP1: <price>")
        appendLine("TP2: <price>")
        appendLine("TP3: <price>")
        appendLine("RR: <e.g. 1:2.5>")
        appendLine("INVALIDATION: <price>")
        if (notes.isNotBlank()) {
            appendLine()
            appendLine("EXTRA INFO TYPED BY ME (unverified, treat with caution)")
            appendLine(notes.trim())
        }
    }.trimEnd()
}
