package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorContent(
    exception: Throwable,
    onReload: (() -> Unit)? = null,
    onExit: (() -> Unit)? = null,
) {
    val messageString = remember(exception) {
        exception.message?.let { message -> "${exception::class.simpleName} $message" }
            ?: exception::class.simpleName ?: "Unknown Error"
    }
    ErrorContent(messageString, onReload, onExit)
}

@Composable
fun ErrorContent(
    message: String,
    onReload: (() -> Unit)? = null,
    onExit: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.widthIn(max = 1200.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SelectionContainer { Text(message) }
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                if (onReload != null) {
                    FilledTonalButton(onClick = onReload) {
                        Text("Reload")
                    }
                }

                if (onExit != null) {
                    FilledTonalButton(onClick = onExit) {
                        Text("Exit")
                    }
                }
            }

        }
    }
}