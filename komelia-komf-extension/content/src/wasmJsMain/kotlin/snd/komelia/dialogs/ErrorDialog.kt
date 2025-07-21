package snd.komelia.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.dialogs.DialogSimpleHeader

@Composable
fun ErrorDialog(
    message: String,
    onDismissRequest: () -> Unit
) {

    AppDialog(
        modifier = Modifier.widthIn(max = 840.dp),
        header = { DialogSimpleHeader("Error") },
        content = {
            Column(
                modifier = Modifier.fillMaxSize().padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(message)
            }
        },
        onDismissRequest = { onDismissRequest() },
        contentPadding = PaddingValues(20.dp)
    )
}
