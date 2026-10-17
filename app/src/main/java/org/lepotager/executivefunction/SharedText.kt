package org.lepotager.executivefunction

import android.content.Intent

/** Bounded local-only sharesheet text. The user confirms the note before saving. */
internal object SharedText {
    const val MAX_CHARS = 4_000
    fun from(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return null
        return intent.getCharSequenceExtra(Intent.EXTRA_TEXT)
            ?.toString()?.trim()?.take(MAX_CHARS)?.takeIf { it.isNotBlank() }
    }
}
