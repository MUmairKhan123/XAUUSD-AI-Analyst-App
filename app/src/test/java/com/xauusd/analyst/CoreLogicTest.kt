package com.xauusd.analyst

import com.xauusd.analyst.data.AppSettings
import com.xauusd.analyst.data.Decision
import com.xauusd.analyst.data.Timeframe
import com.xauusd.analyst.util.AnalysisParser
import com.xauusd.analyst.util.PromptBuilder
import com.xauusd.analyst.util.RiskCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreLogicTest {
    @Test fun riskAmountDefault() {
        assertEquals(2.0, RiskCalculator.riskAmount(100.0, 2.0), 1e-9)
    }

    @Test fun lotSizeNeedsVerifiedSpecs() {
        val r = RiskCalculator.lotSize(AppSettings(), 3.0)
        assertTrue(r is RiskCalculator.LotResult.Missing)
    }

    @Test fun lotSizeFromSpecs() {
        val s = AppSettings(contractSize = "100", tickSize = "0.01", tickValue = "1", lotStep = "0.01")
        val r = RiskCalculator.lotSize(s, 2.0) as RiskCalculator.LotResult.Ok
        // $2 risk, 200 ticks * $1 = $200 per lot -> 0.01 lot
        assertEquals(0.01, r.lots, 1e-9)
    }

    @Test fun parserReadsSummaryBlock() {
        val t = "8. Final decision: SELL\nblah\nDECISION: SELL\nENTRY: 2650.5 - 2652\nSL: 2655\nTP1: 2645\nTP2: 2640\nTP3: 2635\nRR: 1:2.5\nINVALIDATION: 2655.5"
        val p = AnalysisParser.parse(t)
        assertEquals(Decision.SELL, p.decision)
        assertEquals("2650.5 - 2652", p.entry)
        assertEquals("2655", p.sl)
        assertEquals("2635", p.tp3)
    }

    @Test fun parserUnknownDecisionIsNull() {
        assertNull(AnalysisParser.parse("no decision here").decision)
    }

    @Test fun slDistanceUsesFarthestEntryPoint() {
        assertEquals(5.0, RiskCalculator.slDistance("2650 - 2652", "2655")!!, 1e-9)
    }

    @Test fun promptKeepsM1AndListsMissing() {
        val p = PromptBuilder.build(AppSettings(), setOf(Timeframe.H4), "")
        assertTrue(p.contains("M1 - Precise entry trigger") || p.contains("M1 -"))
        assertTrue(p.contains("MISSING"))
        assertTrue(!p.contains("M3"))
    }
}
