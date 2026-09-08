package org.lepotager.executivefunction.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.lepotager.executivefunction.R

/**
 * Capture dialog used when the home-screen widget opens the app directly into
 * capture mode. Kept independent from companion artwork and from the current
 * foreground screen.
 */
@Composable
fun WidgetCaptureDialog(
    onDismiss: () -> Unit,
    onCapture: (String) -> Unit,
) {
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.quick_capture_title)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.capture_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCapture(value) },
                enabled = value.isNotBlank(),
            ) {
                Text(stringResource(R.string.capture_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
