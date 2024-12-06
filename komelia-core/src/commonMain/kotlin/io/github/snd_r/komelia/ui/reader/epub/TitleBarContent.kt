package io.github.snd_r.komelia.ui.reader.epub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.TitleBarScope

@Composable
fun TitleBarScope.TitleBarContent(
    title: String,
    onExit: () -> Unit,
) {

    Row(
        modifier = Modifier
            .align(Alignment.Start)
            .height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(onClick = onExit) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Leave webview",
                modifier = Modifier.fillMaxHeight()
            )
        }

        Text(title)
    }
}