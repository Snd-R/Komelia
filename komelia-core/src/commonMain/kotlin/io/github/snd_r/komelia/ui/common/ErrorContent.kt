package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message)

        Row {
            if (onReload != null) {
                Spacer(Modifier.size(10.dp))
                Button(onClick = onReload) {
                    Text("Reload")
                }
            }

            if (onExit != null) {
                Spacer(Modifier.size(10.dp))
                Button(onClick = onExit) {
                    Text("Exit")
                }
            }
        }

    }
}