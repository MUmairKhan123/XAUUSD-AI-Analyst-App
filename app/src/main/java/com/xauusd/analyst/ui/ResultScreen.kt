package com.xauusd.analyst.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xauusd.analyst.MainViewModel
import com.xauusd.analyst.data.Decision
import com.xauusd.analyst.data.money
import com.xauusd.analyst.util.RiskCalculator

@Composable
fun ResultScreen(vm: MainViewModel, onSaved: () -> Unit, onSettings: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val d by vm.draft.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val shots by vm.shots.collectAsStateWithLifecycle()
    val risk = RiskCalculator.riskAmount(s.balance, s.riskPercent)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        XCard(borderColor = Gold) {
            Title("Paste ChatGPT's analysis")
            Muted("ChatGPT is not connected to this app. Copy its full reply in ChatGPT, then paste it here.")
            GoldButton("Paste from clipboard") {
                val t = clipboard.getText()?.text.orEmpty()
                if (t.isBlank()) vm.messages.tryEmit("Clipboard is empty") else vm.detect(t)
            }
            XField("Analysis text", d.text, { t -> vm.updateDraft { it.copy(text = t) } }, singleLine = false, minLines = 8)
            OutlineButton("Detect decision & levels from text", Modifier.fillMaxWidth(), enabled = d.text.isNotBlank()) {
                vm.detect(d.text)
            }
        }

        if (d.text.isBlank()) {
            XCard { EmptyState("Nothing to review yet", "Paste ChatGPT's reply above. The app will try to read the decision, entry, SL and TP levels - you can correct them.") }
        } else {
            XCard(borderColor = decisionColor(d.decision)) {
                Title("Review")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Decision.values().forEach { opt ->
                        val selected = d.decision == opt
                        val c = decisionColor(opt)
                        OutlineButton(
                            opt.name, Modifier.weight(1f), color = if (selected) c else TextMuted
                        ) { vm.updateDraft { it.copy(decision = opt) } }
                    }
                }
                if (d.decision == null) WarningBox("Decision not detected. Choose BUY / SELL / WAIT. If you are unsure, choose WAIT.")
                XField("Entry zone", d.entry, { t -> vm.updateDraft { it.copy(entry = t) } })
                XField("Stop Loss (SL)", d.sl, { t -> vm.updateDraft { it.copy(sl = t) } })
                XField("TP1", d.tp1, { t -> vm.updateDraft { it.copy(tp1 = t) } })
                XField("TP2", d.tp2, { t -> vm.updateDraft { it.copy(tp2 = t) } })
                XField("TP3", d.tp3, { t -> vm.updateDraft { it.copy(tp3 = t) } })
                XField("Risk-to-Reward", d.rr, { t -> vm.updateDraft { it.copy(rr = t) } })
                XField("Invalidation level", d.invalidation, { t -> vm.updateDraft { it.copy(invalidation = t) } })
            }

            val trade = d.decision == Decision.BUY || d.decision == Decision.SELL
            if (trade && shots.size < 5) {
                WarningBox("Only ${shots.size}/5 screenshots are loaded in the app. A BUY/SELL without all five timeframes should be treated as WAIT.", SellRed)
            }
            if (trade && (d.sl.isBlank() || d.entry.isBlank())) {
                WarningBox("Entry or SL is missing. Do not trade without a defined stop loss.", SellRed)
            }

            XCard {
                Title("Risk & lot size")
                Row(Modifier.fillMaxWidth()) {
                    Stat("Planned risk", "${s.accountCurrency} ${money(risk)}")
                    Stat("Max open", s.maxPositions.toString())
                }
                if (!trade) {
                    Muted("No trade is proposed, so no lot size is needed.")
                } else {
                    val dist = RiskCalculator.slDistance(d.entry, d.sl)
                    if (dist != null) Body("SL distance read from the text: ${money(dist)} (price units, farthest point of entry zone). Verify against your chart.")
                    when (val r = RiskCalculator.lotSize(s, dist)) {
                        is RiskCalculator.LotResult.Ok -> {
                            Text(
                                "Max lot for the planned risk: ${String.format(java.util.Locale.US, "%.3f", r.lots)}",
                                color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp
                            )
                            Muted("Loss per 1.00 lot at this SL: ${s.accountCurrency} ${money(r.lossPerLotAtSl)}. Calculated only from the contract values you entered in Settings.")
                            r.notes.forEach { WarningBox(it) }
                        }
                        is RiskCalculator.LotResult.Missing -> {
                            WarningBox("Lot size is NOT calculated, because required information is missing:\n- " + r.items.joinToString("\n- "))
                            OutlineButton("Open Settings", Modifier.fillMaxWidth(), onClick = onSettings)
                        }
                    }
                }
            }

            GoldButton("Save to history") { vm.saveDraft { onSaved() } }
        }
    }
}
