package com.xauusd.analyst.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xauusd.analyst.MainViewModel
import com.xauusd.analyst.data.AppSettings
import com.xauusd.analyst.data.fmt
import com.xauusd.analyst.data.money
import com.xauusd.analyst.data.num
import com.xauusd.analyst.util.RiskCalculator

@Composable
fun SettingsScreen(vm: MainViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()

    var balance by rememberSaveable { mutableStateOf(fmt(s.balance)) }
    var risk by rememberSaveable { mutableStateOf(fmt(s.riskPercent)) }
    var maxPos by rememberSaveable { mutableStateOf(s.maxPositions.toString()) }
    var dailyLoss by rememberSaveable { mutableStateOf(fmt(s.dailyLossLimit)) }
    var maxSpread by rememberSaveable { mutableStateOf(fmt(s.maxSpread)) }
    var spreadWarn by rememberSaveable { mutableStateOf(s.spreadWarning) }
    var newsWarn by rememberSaveable { mutableStateOf(s.newsWarning) }
    var currency by rememberSaveable { mutableStateOf(s.accountCurrency) }
    var contract by rememberSaveable { mutableStateOf(s.contractSize) }
    var tickSize by rememberSaveable { mutableStateOf(s.tickSize) }
    var tickValue by rememberSaveable { mutableStateOf(s.tickValue) }
    var minLot by rememberSaveable { mutableStateOf(s.minLot) }
    var lotStep by rememberSaveable { mutableStateOf(s.lotStep) }
    var slDistance by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun draft(): AppSettings = s.copy(
        balance = balance.num() ?: s.balance,
        riskPercent = risk.num() ?: s.riskPercent,
        maxPositions = maxPos.trim().toIntOrNull() ?: s.maxPositions,
        dailyLossLimit = dailyLoss.num() ?: s.dailyLossLimit,
        maxSpread = maxSpread.num() ?: s.maxSpread,
        spreadWarning = spreadWarn,
        newsWarning = newsWarn,
        accountCurrency = currency.trim(),
        contractSize = contract.trim(),
        tickSize = tickSize.trim(),
        tickValue = tickValue.trim(),
        minLot = minLot.trim(),
        lotStep = lotStep.trim()
    )

    fun save() {
        val b = balance.num(); val r = risk.num(); val m = maxPos.trim().toIntOrNull()
        val dl = dailyLoss.num(); val sp = maxSpread.num()
        error = when {
            b == null || b <= 0 -> "Balance must be a number above 0."
            r == null || r <= 0 || r > 100 -> "Risk % must be between 0 and 100."
            m == null || m < 1 || m > 20 -> "Max positions must be a whole number from 1 to 20."
            dl == null || dl < 0 -> "Daily loss limit must be 0 or more."
            sp == null || sp < 0 -> "Max spread must be 0 or more."
            else -> null
        }
        if (error == null) vm.saveSettings(draft())
    }

    val d = draft()
    val riskAmt = RiskCalculator.riskAmount(d.balance, d.riskPercent)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        XCard(borderColor = Gold) {
            Title("Account & risk")
            Muted("Broker XM | Platform MT5 | Symbol XAUUSD | Scalping | Manual execution only")
            XField("Account balance", balance, { balance = it }, number = true)
            XField("Risk per trade (%)", risk, { risk = it }, number = true)
            Text(
                "Planned risk: ${d.accountCurrency} ${money(riskAmt)}",
                color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp
            )
            if (d.riskPercent > 5) WarningBox("Risk above 5% per trade is very aggressive for a small account.")
            XField("Max simultaneous open positions", maxPos, { maxPos = it })
            XField("Daily loss limit (account currency)", dailyLoss, { dailyLoss = it }, number = true)
            XField("Max acceptable spread (price units, e.g. 0.40)", maxSpread, { maxSpread = it }, number = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Body("Spread warning")
                Switch(spreadWarn, { spreadWarn = it }, colors = SwitchDefaults.colors(checkedTrackColor = Gold, checkedThumbColor = Navy))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Body("High-impact news warning")
                Switch(newsWarn, { newsWarn = it }, colors = SwitchDefaults.colors(checkedTrackColor = Gold, checkedThumbColor = Navy))
            }
            error?.let { WarningBox(it, SellRed) }
            GoldButton("Save settings") { save() }
        }

        XCard {
            Title("Fixed safety rules")
            Body("- No martingale\n- No grid trading\n- No automatic averaging down\n- Manual execution only; the app never trades or logs in to XM/MT5")
        }

        XCard {
            Title("Lot-size calculator")
            Muted("Nothing is assumed. Enter the values exactly as shown in MT5: right-click XAUUSD in Market Watch > Specification.")
            XField("Account currency (e.g. USD)", currency, { currency = it })
            XField("Contract size", contract, { contract = it }, number = true)
            XField("Tick size", tickSize, { tickSize = it }, number = true)
            XField("Tick value (per 1 lot, account currency)", tickValue, { tickValue = it }, number = true)
            XField("Minimum lot (optional)", minLot, { minLot = it }, number = true)
            XField("Lot step (optional)", lotStep, { lotStep = it }, number = true)
            XField("Stop-loss distance (price units, entry to SL)", slDistance, { slDistance = it }, number = true)
            when (val r = RiskCalculator.lotSize(d, slDistance.num())) {
                is RiskCalculator.LotResult.Missing ->
                    WarningBox("Cannot calculate a lot size yet. Still needed:\n- " + r.items.joinToString("\n- "))
                is RiskCalculator.LotResult.Ok -> {
                    Text(
                        "Max lot: ${String.format(java.util.Locale.US, "%.3f", r.lots)}",
                        color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp
                    )
                    Muted("Loss at SL per 1.00 lot: ${d.accountCurrency} ${money(r.lossPerLotAtSl)}. Risk budget: ${d.accountCurrency} ${money(riskAmt)}.")
                    r.notes.forEach { WarningBox(it) }
                    Muted("Always confirm the final lot in MT5 before sending the order.")
                }
            }
            GoldButton("Save settings") { save() }
        }

        XCard {
            Title("About / limitations")
            Body("Not connected to ChatGPT, XM or MT5. No live prices, no news feed, no trade execution. You move screenshots and text between this app and ChatGPT yourself. AI output can be wrong; nothing here is financial advice or a guarantee of profit.")
        }
    }
}
