package io.github.snd_r.komelia.ui.dialogs

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import kotlinx.coroutines.delay

@Composable
fun DialogLoadIndicator(onDismissRequest: () -> Unit) {
    var showLoadIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        showLoadIndicator = true
    }
    if (showLoadIndicator) {
        AppDialog(
            modifier = Modifier.size(500.dp),
            content = { LoadingMaxSizeIndicator() },
            onDismissRequest = onDismissRequest,
        )
    }
}
