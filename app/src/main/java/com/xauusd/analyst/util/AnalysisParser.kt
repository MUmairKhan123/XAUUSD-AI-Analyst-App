package com.xauusd.analyst.util

import com.xauusd.analyst.data.Decision

data class Parsed(
    val decision: Decision?,
    val entry: String,
    val sl: String,
    val tp1: String,
    val tp2: String,
    val tp3: String,
    val rr: String,
    val invalidation: String
)

/** Best-effort extraction from ChatGPT's reply. The user can always edit the result. */
object AnalysisParser {

    private fun field(text: String, key: String): String {
        val r = Regex(
            "(?im)^[\\s>*#\\-\u2022]*\\**\\s*" + key + "(?:\\s*\\([^)]*\\))?\\s*\\**\\s*[:=]\\s*\\**\\s*(.+?)\\s*\\**\\s*$"
        )
        // Prefer the LAST match: the machine-readable summary block is at the end.
        val m = r.findAll(text).lastOrNull() ?: return ""
        return m.groupValues[1].trim().trim('*').trim()
    }

    fun parse(text: String): Parsed {
        val strict = Regex(
            "(?im)^[\\s>*#\\-\u2022]*\\**\\s*(?:FINAL\\s+)?DECISION\\s*\\**\\s*[:=\\-]\\s*\\**\\s*\\[?\\s*(BUY|SELL|WAIT)"
        ).findAll(text).lastOrNull()
        val loose = Regex("(?i)final\\s+decision\\W{0,30}(BUY|SELL|WAIT)").findAll(text).lastOrNull()
        val d = (strict ?: loose)?.groupValues?.get(1)?.uppercase()?.let { Decision.valueOf(it) }
        return Parsed(
            decision = d,
            entry = field(text, "ENTRY(?:\\s+ZONE)?"),
            sl = field(text, "(?:SL|STOP\\s*LOSS)"),
            tp1 = field(text, "(?:TP1|TAKE\\s*PROFIT\\s*1)"),
            tp2 = field(text, "(?:TP2|TAKE\\s*PROFIT\\s*2)"),
            tp3 = field(text, "(?:TP3|TAKE\\s*PROFIT\\s*3)"),
            rr = field(text, "(?:RR|RISK[- ]TO[- ]REWARD(?:\\s+RATIO)?)"),
            invalidation = field(text, "(?:INVALIDATION|SETUP\\s+INVALIDATION)(?:\\s+LEVEL)?")
        )
    }
}
