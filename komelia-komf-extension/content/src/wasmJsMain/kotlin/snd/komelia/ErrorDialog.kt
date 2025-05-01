package snd.komelia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.dialogs.DialogSimpleHeader
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId

@Composable
fun ErrorDialog(
    seriesId: KomfServerSeriesId?,
    libraryId: KomfServerLibraryId?,
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
                if (seriesId == null || seriesId.value.isBlank()) Text("Failed to parse seriesId")
                if (libraryId == null || libraryId.value.isBlank()) Text("Failed to parse libraryId")
            }
        },
        onDismissRequest = { onDismissRequest() },
        contentPadding = PaddingValues(20.dp)
    )
}

@Composable
fun ErrorDialog(
    libraryId: KomfServerLibraryId?,
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
                if (libraryId == null || libraryId.value.isBlank()) Text("Failed to parse libraryId")
            }
        },
        onDismissRequest = { onDismissRequest() },
        contentPadding = PaddingValues(20.dp)
    )
}
