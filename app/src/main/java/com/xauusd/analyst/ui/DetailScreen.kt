package com.xauusd.analyst.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.xauusd.analyst.MainViewModel
import com.xauusd.analyst.data.AnalysisEntity
import com.xauusd.analyst.data.ImagePaths
import com.xauusd.analyst.data.Timeframe
import com.xauusd.analyst.data.decisionOf
import com.xauusd.analyst.data.fmt
import com.xauusd.analyst.util.Sharing
import java.io.File

@Composable
fun DetailScreen(vm: MainViewModel, id: Long, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val state = produceState<Pair<Boolean, AnalysisEntity?>>(initialValue = false to null, id) {
        value = true to vm.get(id)
    }
    val (loaded, e) = state.value

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Gold) }
        return
    }
    if (e == null) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            EmptyState("Not found", "This analysis no longer exists.")
        }
        return
    }
    val imgs = ImagePaths.decode(e.imagePaths)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        XCard(borderColor = decisionColor(decisionOf(e.decision))) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                DecisionBadge(decisionOf(e.decision))
                Muted(formatDate(e.createdAt))
            }
            LevelRow("Entry zone", e.entry)
            LevelRow("Stop Loss", e.stopLoss)
            LevelRow("TP1", e.tp1)
            LevelRow("TP2", e.tp2)
            LevelRow("TP3", e.tp3)
            LevelRow("Risk-to-Reward", e.rr)
            LevelRow("Invalidation", e.invalidation)
            Muted("Account at the time: ${fmt(e.balance)} | risk ${fmt(e.riskPercent)}%")
        }

        if (imgs.isNotEmpty()) {
            XCard {
                Title("Screenshots used")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Timeframe.values().forEach { tf ->
                        imgs[tf]?.let { p ->
                            Muted(tf.label)
                            AsyncImage(
                                model = File(p), contentDescription = tf.label,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        } else {
            XCard { Muted("No screenshots were saved with this analysis.") }
        }

        XCard {
            Title("Full analysis")
            SelectionContainer { Body(e.analysisText.ifBlank { "(empty)" }) }
            OutlineButton("Copy analysis text", Modifier.fillMaxWidth()) {
                Sharing.copy(ctx, "Analysis", e.analysisText)
                vm.messages.tryEmit("Analysis copied")
            }
        }

        OutlineButton("Delete this analysis", Modifier.fillMaxWidth(), color = SellRed) {
            vm.delete(e)
            onBack()
        }
    }
}
