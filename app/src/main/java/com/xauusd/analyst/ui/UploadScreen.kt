package com.xauusd.analyst.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.xauusd.analyst.MainViewModel
import com.xauusd.analyst.data.Timeframe
import java.io.File

@Composable
fun UploadScreen(vm: MainViewModel, onNext: () -> Unit) {
    val shots by vm.shots.collectAsStateWithLifecycle()
    var target by rememberSaveable { mutableStateOf<String?>(null) }

    val single = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val tf = target?.let { name -> Timeframe.values().firstOrNull { it.name == name } }
        if (uri != null && tf != null) vm.setImage(tf, uri)
        target = null
    }
    val multi = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
        if (uris.isNotEmpty()) vm.setImages(uris)
    }
    val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        XCard(borderColor = Gold) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Title("XAUUSD charts")
                Text("${shots.size} / 5 uploaded", color = if (shots.size == 5) BuyGreen else Gold, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(progress = { shots.size / 5f }, modifier = Modifier.fillMaxWidth(), color = Gold, trackColor = NavyLight)
            Muted("Take MT5 screenshots of XAUUSD on H4, H1, M15, M5 and M1 (M1, not M3). Make sure the price scale and candles are readable.")
            OutlineButton("Select several at once", Modifier.fillMaxWidth()) { multi.launch(imageOnly) }
            Muted("Several-at-once fills the EMPTY slots in order H4 > H1 > M15 > M5 > M1. Check each card afterwards.")
        }

        Timeframe.values().forEach { tf ->
            val path = shots[tf]
            XCard(borderColor = if (path != null) BuyGreen.copy(alpha = 0.6f) else Outline) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(tf.label, color = Gold, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Muted("${tf.title}")
                    }
                    if (path != null) Chip("UPLOADED", BuyGreen) else Chip("MISSING", SellRed)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Navy),
                    contentAlignment = Alignment.Center
                ) {
                    if (path != null) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "${tf.label} screenshot",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Muted("No ${tf.label} screenshot yet")
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (path == null) {
                        GoldButton("Select ${tf.label}", Modifier.weight(1f)) {
                            target = tf.name; single.launch(imageOnly)
                        }
                    } else {
                        OutlineButton("Replace", Modifier.weight(1f)) {
                            target = tf.name; single.launch(imageOnly)
                        }
                        OutlineButton("Remove", Modifier.weight(1f), color = SellRed) { vm.removeImage(tf) }
                    }
                }
            }
        }

        if (shots.size < 5) {
            WarningBox("Only ${shots.size}/5 uploaded. With missing charts the analysis should come back as WAIT.")
        }
        GoldButton("Continue to ChatGPT prompt", enabled = shots.isNotEmpty(), onClick = onNext)
        if (shots.isNotEmpty()) {
            OutlineButton("Clear all screenshots (new session)", Modifier.fillMaxWidth(), color = TextMuted) { vm.clearShots() }
        }
    }
}
