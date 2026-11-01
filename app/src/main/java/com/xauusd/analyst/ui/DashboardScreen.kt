package com.xauusd.analyst.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xauusd.analyst.MainViewModel
import com.xauusd.analyst.data.Timeframe
import com.xauusd.analyst.data.decisionOf
import com.xauusd.analyst.data.fmt
import com.xauusd.analyst.data.money
import com.xauusd.analyst.util.RiskCalculator

@Composable
fun DashboardScreen(vm: MainViewModel, onNav: (String) -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val shots by vm.shots.collectAsStateWithLifecycle()
    val latest by vm.latest.collectAsStateWithLifecycle()
    val risk = RiskCalculator.riskAmount(s.balance, s.riskPercent)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        XCard(borderColor = Gold) {
            Muted("XM  |  MT5  |  XAUUSD  |  Scalping  |  Manual only")
            Text(
                "${s.accountCurrency} ${money(s.balance)}",
                color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp
            )
            Row(Modifier.fillMaxWidth()) {
                Stat("Risk / trade", "${fmt(s.riskPercent)}%")
                Stat("Planned risk", "${s.accountCurrency} ${money(risk)}")
            }
            Row(Modifier.fillMaxWidth()) {
                Stat("Max positions", s.maxPositions.toString())
                Stat("Daily loss limit", "${s.accountCurrency} ${money(s.dailyLossLimit)}")
            }
            OutlineButton("Edit risk settings", Modifier.fillMaxWidth()) { onNav("settings") }
        }

        XCard {
            val n = shots.size
            Title("Today's workflow")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Body("1. Screenshots uploaded")
                Text("$n / 5", color = if (n == 5) BuyGreen else Gold, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { n / 5f },
                modifier = Modifier.fillMaxWidth(),
                color = Gold, trackColor = NavyLight
            )
            Muted(
                Timeframe.values().joinToString("  ") { (if (it in shots) "[OK] " else "[--] ") + it.label }
            )
            Body("2. Copy/share the prompt to ChatGPT   3. Paste the reply   4. Review & save")
            GoldButton(if (n == 5) "Open prompt" else "Upload charts") {
                onNav(if (n == 5) "prompt" else "upload")
            }
        }

        XCard {
            Title("Latest analysis")
            val l = latest
            if (l == null) {
                EmptyState("No analysis yet", "Upload your 5 charts, run them through ChatGPT and save the result here.")
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DecisionBadge(decisionOf(l.decision))
                    Muted(formatDate(l.createdAt))
                }
                LevelRow("Entry", l.entry)
                LevelRow("SL", l.stopLoss)
                LevelRow("TP1 / TP2 / TP3", listOf(l.tp1, l.tp2, l.tp3).joinToString(" / ") { it.ifBlank { "-" } })
                OutlineButton("Open full analysis", Modifier.fillMaxWidth()) { onNav("detail/${l.id}") }
            }
        }

        if (s.spreadWarning) {
            WarningBox("Spread: this app has no live data. Check the live XAUUSD spread in MT5 yourself. Your limit is ${money(s.maxSpread)} (price units). Skip the trade if it is wider.")
        }
        if (s.newsWarning) {
            WarningBox("High-impact news: this app has no news feed. Check an economic calendar yourself before trading (e.g. US CPI, NFP, FOMC).")
        }

        XCard {
            Title("What is automatic, what is manual")
            Body("Automatic: prompt generation, risk maths, local history.")
            Body("Manual: you attach the screenshots in ChatGPT, send them, and paste the answer back. This app is not connected to ChatGPT, XM or MT5, and it never places trades.")
            Muted("AI analysis can be wrong. No accuracy or profit is guaranteed. Gold trading is high risk.")
        }
    }
}
