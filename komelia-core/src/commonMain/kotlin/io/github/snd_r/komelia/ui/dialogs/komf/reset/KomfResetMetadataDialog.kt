package io.github.snd_r.komelia.ui.dialogs.komf.reset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.dialogs.DialogConfirmCancelButtons
import io.github.snd_r.komelia.ui.dialogs.DialogSimpleHeader
import kotlinx.coroutines.launch
import snd.komga.client.series.KomgaSeries

@Composable
fun KomfResetMetadataDialog(
    series: KomgaSeries,
    onDismissRequest: () -> Unit,
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getKomfResetMetadataDialogViewModel(series, onDismissRequest) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    AppDialog(
        contentPadding = PaddingValues(20.dp),
        modifier = Modifier.widthIn(max = 650.dp),
        header = { DialogSimpleHeader("Reset Series Metadata") },
        content = { DialogContent(vm.removeComicInfo, vm::removeComicInfo::set) },
        controlButtons = {
            DialogConfirmCancelButtons(
                onConfirm = {
                    coroutineScope.launch {
                        isLoading = true
                        vm.onReset()
                        onDismissRequest()
                    }
                },
                onCancel = onDismissRequest,
                isLoading = isLoading
            )
        },
        onDismissRequest = onDismissRequest
    )
}

@Composable
private fun DialogContent(
    removeComicInfo: Boolean,
    onRemoveComicInfoChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.heightIn(min = 200.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            """
                    All series metadata will be reset including field locks and thumbnails uploaded by Komf.
                     No files will be modified. Continue?
                """.trimIndent()
        )

        SwitchWithLabel(
            checked = removeComicInfo,
            onCheckedChange = onRemoveComicInfoChange,
            label = { Text("Remove ComicInfo.xml?") },
            supportingText = { Text("Requires write access to files") }
        )
    }
}