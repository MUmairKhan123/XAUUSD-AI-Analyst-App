package com.xauusd.analyst.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xauusd.analyst.MainViewModel
import com.xauusd.analyst.data.Timeframe
import com.xauusd.analyst.data.money
import com.xauusd.analyst.util.PromptBuilder
import com.xauusd.analyst.util.Sharing

@Composable
fun PromptScreen(vm: MainViewModel, onNext: () -> Unit) {
    val ctx = LocalContext.current
    val s by vm.settings.collectAsStateWithLifecycle()
    val shots by vm.shots.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val prompt = remember(s, shots.keys.toSet(), notes) { PromptBuilder.build(s, shots.keys.toSet(), notes) }
    val orderedPaths = Timeframe.values().mapNotNull { shots[it] }

    var c1 by rememberSaveable { mutableStateOf(false) }
    var c2 by rememberSaveable { mutableStateOf(false) }
    var c3 by rememberSaveable { mutableStateOf(false) }
    var c4 by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (shots.size < 5) {
            WarningBox("${shots.size}/5 screenshots uploaded. The prompt tells ChatGPT which are missing and to answer WAIT if confirmation is missing.")
        }

        XCard(borderColor = Gold) {
            Title("1. Send to ChatGPT")
            GoldButton("Copy prompt") {
                Sharing.copy(ctx, "XAUUSD prompt", prompt)
                vm.messages.tryEmit("Prompt copied")
            }
            OutlineButton("Share prompt + ${orderedPaths.size} screenshot(s)", Modifier.fillMaxWidth(), enabled = orderedPaths.isNotEmpty()) {
                Sharing.copy(ctx, "XAUUSD prompt", prompt)
                Sharing.share(ctx, prompt, orderedPaths, "Share to ChatGPT")
                vm.messages.tryEmit("Prompt also copied. If ChatGPT only receives the images, paste the prompt.")
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton("Share images only", Modifier.weight(1f), enabled = orderedPaths.isNotEmpty()) {
                    Sharing.share(ctx, null, orderedPaths, "Share screenshots")
                }
                OutlineButton("Share text only", Modifier.weight(1f)) {
                    Sharing.share(ctx, prompt, emptyList(), "Share prompt")
                }
            }
            OutlineButton("Open ChatGPT", Modifier.fillMaxWidth()) {
                val app = Sharing.openChatGpt(ctx)
                vm.messages.tryEmit(if (app) "Opening ChatGPT app" else "ChatGPT app not found - opening chatgpt.com")
            }
        }

        XCard {
            Title("If sharing does not attach all 5 images")
            Muted("Some devices/apps accept only one image or ignore text when sharing. This is a limit of Android and ChatGPT, not a bug here. Do it manually:")
            Body("1. Tap \"Copy prompt\".")
            Body("2. Open ChatGPT and start a new chat.")
            Body("3. Tap + (attach) > Photos and select all 5 screenshots (names start with H4, H1, M15, M5, M1).")
            Body("4. Paste the prompt and send.")
            Body("5. Copy ChatGPT's full answer, come back here and open the Result tab.")
        }

        XCard {
            Title("2. Before you trade (manual checks)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(c1, { c1 = it }, colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Navy))
                Body("Live spread checked in MT5 (my limit: ${money(s.maxSpread)})")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(c2, { c2 = it }, colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Navy))
                Body("No high-impact news soon (checked a calendar myself)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(c3, { c3 = it }, colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Navy))
                Body("Open positions below my max (${s.maxPositions})")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(c4, { c4 = it }, colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Navy))
                Body("Daily loss limit (${s.accountCurrency} ${money(s.dailyLossLimit)}) not reached")
            }
            if (c1 && c2 && c3 && c4) Chip("CHECKLIST COMPLETE", BuyGreen) else Chip("CHECKLIST INCOMPLETE", WaitGrey)
            Muted("No martingale. No grid. No averaging down.")
        }

        XCard {
            Title("Optional notes for ChatGPT")
            XField("e.g. spread I see now, session, news I know about", notes, { vm.notes.value = it }, singleLine = false, minLines = 2)
            Muted("What you type here is included as UNVERIFIED info.")
        }

        XCard {
            Title("Generated prompt")
            SelectionContainer { Body(prompt) }
        }

        GoldButton("I have ChatGPT's answer - go to Result", onClick = onNext)
    }
}
