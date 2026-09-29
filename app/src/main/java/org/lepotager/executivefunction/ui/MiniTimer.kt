package org.lepotager.executivefunction.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.lepotager.executivefunction.model.ActiveFocus
import org.lepotager.executivefunction.domain.SessionClock
import kotlinx.coroutines.delay
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun MiniTimer(active: ActiveFocus) {
    var now by remember {mutableLongStateOf(System.currentTimeMillis())}
    val lifecycle=androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(active.session.id) {lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED){while(true){now=System.currentTimeMillis();delay(1000)}}}
    val elapsed=SessionClock.elapsedMs(active.session,now)
    val target=active.session.targetDurationMs
    val duration=if(target==null) elapsed else kotlin.math.abs(target-elapsed)
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            Text(active.task.title,maxLines=2)
            Text(
                (if(target!=null&&elapsed>target) "+" else "") + FocusClockText.format(duration),
                style=MaterialTheme.typography.headlineMedium,
            )
        }
    }
}
