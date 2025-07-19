package io.github.snd_r.komelia.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.formatDecimal


@Composable
fun UpdateProgressContent(
    total: Long,
    completed: Long,
    info: String?
) {
    Column(
        Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        info?.let { Text(it) }
        if (total == 0L)
            LinearProgressIndicator(modifier = Modifier.fillMaxSize())
        else {
            LinearProgressIndicator(
                progress = { completed / total.toFloat() },
                modifier = Modifier.fillMaxSize()
            )

            val totalMb = remember(total) {
                (total.toFloat() / 1024 / 1024).formatDecimal(2)
            }
            val completedMb = remember(completed) {
                (completed.toFloat() / 1024 / 1024).formatDecimal(2)
            }
            Text("${completedMb}MiB / ${totalMb}MiB")
        }
    }
}
