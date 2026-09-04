package org.lepotager.executivefunction.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.lepotager.executivefunction.domain.SessionClock
import org.lepotager.executivefunction.model.ActiveFocus

/** Minimal timer-only composition for Android Picture-in-Picture. */
@Composable
fun PipFocusScreen(activeFocus: ActiveFocus) {
    var now by remember(activeFocus.session.id) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(activeFocus.session.id, activeFocus.session.segmentStartedAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val elapsed = SessionClock.elapsedMs(activeFocus.session, now)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = activeFocus.task.title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Text(
            text = formatPipElapsed(elapsed),
            fontSize = 26.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

private fun formatPipElapsed(elapsedMs: Long): String {
    val totalSeconds = elapsedMs.coerceAtLeast(0L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
