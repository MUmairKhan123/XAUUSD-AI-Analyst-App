package com.xauusd.analyst.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object Sharing {

    fun copy(ctx: Context, label: String, text: String) {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun uriFor(ctx: Context, path: String): Uri =
        FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", File(path))

    /** Shares text and/or screenshots through the Android share sheet. Returns false if nothing to share. */
    fun share(ctx: Context, text: String?, imagePaths: List<String>, chooserTitle: String): Boolean {
        val files = imagePaths.filter { File(it).exists() }
        val intent: Intent
        if (files.isEmpty()) {
            if (text.isNullOrBlank()) return false
            intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
        } else {
            val uris = ArrayList(files.map { uriFor(ctx, it) })
            intent = Intent().apply {
                if (uris.size == 1) {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uris[0])
                } else {
                    action = Intent.ACTION_SEND_MULTIPLE
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                }
                type = "image/*"
                if (!text.isNullOrBlank()) putExtra(Intent.EXTRA_TEXT, text)
                val clip = ClipData.newRawUri("charts", uris[0])
                uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
                clipData = clip
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        val chooser = Intent.createChooser(intent, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
        return true
    }

    /** Opens the ChatGPT app if installed, otherwise chatgpt.com in the browser. */
    fun openChatGpt(ctx: Context): Boolean {
        val pm = ctx.packageManager
        val launch = pm.getLaunchIntentForPackage("com.openai.chatgpt")
        return try {
            if (launch != null) {
                ctx.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            } else {
                ctx.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
