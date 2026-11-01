package com.xauusd.analyst.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.xauusd.analyst.MainViewModel
import com.xauusd.analyst.data.AnalysisEntity
import com.xauusd.analyst.data.ImagePaths
import com.xauusd.analyst.data.Timeframe
import com.xauusd.analyst.data.decisionOf
import java.io.File

@Composable
fun HistoryScreen(vm: MainViewModel, onOpen: (Long) -> Unit) {
    val list by vm.history.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<AnalysisEntity?>(null) }
    var confirmClear by remember { mutableStateOf(false) }

    when (val data = list) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Gold)
        }
        else -> if (data.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                EmptyState("No saved analyses", "Saved ChatGPT analyses appear here so you can review past decisions.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlineButton("Clear all history", Modifier.fillMaxWidth(), color = SellRed) { confirmClear = true }
                }
                items(data, key = { it.id }) { e ->
                    val imgs = ImagePaths.decode(e.imagePaths)
                    XCard(modifier = Modifier.clickable { onOpen(e.id) }, borderColor = decisionColor(decisionOf(e.decision)).copy(alpha = 0.6f)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            DecisionBadge(decisionOf(e.decision))
                            Muted(formatDate(e.createdAt))
                            IconButton(onClick = { toDelete = e }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SellRed)
                            }
                        }
                        LevelRow("Entry", e.entry)
                        LevelRow("SL", e.stopLoss)
                        LevelRow("TP1 / TP2 / TP3", listOf(e.tp1, e.tp2, e.tp3).joinToString(" / ") { it.ifBlank { "-" } })
                        if (imgs.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Timeframe.values().forEach { tf ->
                                    imgs[tf]?.let { p ->
                                        AsyncImage(
                                            model = File(p), contentDescription = tf.label,
                                            modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    toDelete?.let { e ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            containerColor = NavyCard,
            title = { Text("Delete this analysis?", color = TextPrimary) },
            text = { Muted("It and its saved screenshots will be removed from this phone.") },
            confirmButton = { TextButton(onClick = { vm.delete(e); toDelete = null }) { Text("Delete", color = SellRed) } },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel", color = Gold) } }
        )
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            containerColor = NavyCard,
            title = { Text("Clear all history?", color = TextPrimary) },
            text = { Muted("Every saved analysis and its screenshots will be deleted. This cannot be undone.") },
            confirmButton = { TextButton(onClick = { vm.clearAll(); confirmClear = false }) { Text("Clear all", color = SellRed) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel", color = Gold) } }
        )
    }
}
