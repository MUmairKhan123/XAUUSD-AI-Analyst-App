package com.xauusd.analyst

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.xauusd.analyst.data.AnalysisEntity
import com.xauusd.analyst.data.AppDatabase
import com.xauusd.analyst.data.AppSettings
import com.xauusd.analyst.data.Decision
import com.xauusd.analyst.data.ImagePaths
import com.xauusd.analyst.data.SettingsStore
import com.xauusd.analyst.data.Timeframe
import com.xauusd.analyst.util.AnalysisParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class Draft(
    val text: String = "",
    val decision: Decision? = null,
    val entry: String = "",
    val sl: String = "",
    val tp1: String = "",
    val tp2: String = "",
    val tp3: String = "",
    val rr: String = "",
    val invalidation: String = ""
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val ctx get() = getApplication<Application>()
    private val dao = AppDatabase.get(app).dao()
    private val store = SettingsStore(app)

    val settings: StateFlow<AppSettings> = store.flow

    private val _shots = MutableStateFlow<Map<Timeframe, String>>(emptyMap())
    val shots: StateFlow<Map<Timeframe, String>> = _shots.asStateFlow()

    val notes = MutableStateFlow("")
    val draft = MutableStateFlow(Draft())
    val messages = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** null = still loading */
    val history: StateFlow<List<AnalysisEntity>?> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val latest: StateFlow<AnalysisEntity?> =
        dao.observeLatest().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch(Dispatchers.IO) { _shots.value = scan() }
    }

    // ---------- settings ----------
    fun saveSettings(s: AppSettings) {
        store.update(s)
        messages.tryEmit("Settings saved")
    }

    // ---------- screenshots ----------
    private fun currentDir(): File = File(ctx.filesDir, "current").apply { mkdirs() }

    private fun scan(): Map<Timeframe, String> {
        val files = currentDir().listFiles() ?: return emptyMap()
        val out = mutableMapOf<Timeframe, String>()
        for (tf in Timeframe.values()) {
            files.filter { it.name.startsWith(tf.name + "_") }
                .maxByOrNull { it.lastModified() }
                ?.let { out[tf] = it.absolutePath }
        }
        return out
    }

    private fun copyIn(tf: Timeframe, uri: Uri): Boolean = try {
        val type = ctx.contentResolver.getType(uri) ?: ""
        val ext = when {
            type.contains("png") -> "png"
            type.contains("webp") -> "webp"
            else -> "jpg"
        }
        val dest = File(currentDir(), "${tf.name}_${System.currentTimeMillis()}.$ext")
        val input = ctx.contentResolver.openInputStream(uri) ?: throw IllegalStateException("no stream")
        input.use { i -> dest.outputStream().use { o -> i.copyTo(o) } }
        _shots.value[tf]?.let { File(it).delete() }
        _shots.update { it + (tf to dest.absolutePath) }
        true
    } catch (e: Exception) {
        false
    }

    fun setImage(tf: Timeframe, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!copyIn(tf, uri)) messages.tryEmit("Could not read the image for ${tf.label}")
        }
    }

    /** Fills empty slots in order H4 -> M1 (or replaces from H4 if all are filled). */
    fun setImages(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val empty = Timeframe.values().filter { it !in _shots.value }
            val targets = if (empty.isEmpty()) Timeframe.values().toList() else empty
            val pairs = uris.zip(targets)
            var ok = 0
            pairs.forEach { (u, tf) -> if (copyIn(tf, u)) ok++ }
            messages.tryEmit(
                "$ok image(s) assigned to ${pairs.joinToString { it.second.label }}. Check every card is the right timeframe."
            )
        }
    }

    fun removeImage(tf: Timeframe) {
        viewModelScope.launch(Dispatchers.IO) {
            _shots.value[tf]?.let { File(it).delete() }
            _shots.update { it - tf }
        }
    }

    fun clearShots() {
        viewModelScope.launch(Dispatchers.IO) {
            currentDir().deleteRecursively()
            _shots.value = emptyMap()
            messages.tryEmit("All screenshots cleared")
        }
    }

    // ---------- analysis draft ----------
    fun detect(text: String) {
        val p = AnalysisParser.parse(text)
        draft.value = Draft(
            text = text, decision = p.decision, entry = p.entry, sl = p.sl,
            tp1 = p.tp1, tp2 = p.tp2, tp3 = p.tp3, rr = p.rr, invalidation = p.invalidation
        )
        if (p.decision == null && text.isNotBlank()) {
            messages.tryEmit("Could not detect BUY/SELL/WAIT. Please choose it manually.")
        }
    }

    fun updateDraft(f: (Draft) -> Draft) = draft.update(f)

    fun saveDraft(onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val d = draft.value
            val now = System.currentTimeMillis()
            val paths = withContext(Dispatchers.IO) {
                val dir = File(ctx.filesDir, "history/$now").apply { mkdirs() }
                val out = mutableMapOf<Timeframe, String>()
                _shots.value.forEach { (tf, path) ->
                    val src = File(path)
                    if (src.exists()) {
                        val dst = File(dir, src.name)
                        src.copyTo(dst, overwrite = true)
                        out[tf] = dst.absolutePath
                    }
                }
                out
            }
            val s = settings.value
            val id = dao.insert(
                AnalysisEntity(
                    createdAt = now,
                    decision = (d.decision ?: Decision.WAIT).name,
                    entry = d.entry, stopLoss = d.sl, tp1 = d.tp1, tp2 = d.tp2, tp3 = d.tp3,
                    rr = d.rr, invalidation = d.invalidation,
                    analysisText = d.text,
                    imagePaths = ImagePaths.encode(paths),
                    balance = s.balance, riskPercent = s.riskPercent
                )
            )
            draft.value = Draft()
            messages.tryEmit("Saved to history")
            onSaved(id)
        }
    }

    // ---------- history ----------
    suspend fun get(id: Long): AnalysisEntity? = dao.getById(id)

    fun delete(e: AnalysisEntity) {
        viewModelScope.launch {
            dao.deleteById(e.id)
            withContext(Dispatchers.IO) { File(ctx.filesDir, "history/${e.createdAt}").deleteRecursively() }
            messages.tryEmit("Analysis deleted")
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.deleteAll()
            withContext(Dispatchers.IO) { File(ctx.filesDir, "history").deleteRecursively() }
            messages.tryEmit("History cleared")
        }
    }
}
