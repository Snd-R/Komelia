package snd.komelia.ui.reader

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import snd.komelia.ui.platform.TitleBarScope

@Composable
fun TitleBarScope.TitleBarContent(
    title: String,
    onExit: () -> Unit,
) {
    IconButton(
        onClick = onExit,
        modifier = Modifier
            .align(Alignment.Start)
            .height(32.dp)
            .widthIn(min = 32.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            "Leave",
        )
    }
    Spacer(
        Modifier.width(10.dp)
            .align(Alignment.Start)
            .nonInteractive()
    )

    Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.heightIn(max = 32.dp)
            .align(Alignment.Start)
            .nonInteractive()
            .fillMaxWidth(.7f)
    )

}