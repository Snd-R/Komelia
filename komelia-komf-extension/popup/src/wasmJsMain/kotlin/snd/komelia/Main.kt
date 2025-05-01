package snd.komelia

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.window.CanvasBasedWindow


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(
        canvasElementId = "ComposeTarget",
        applyDefaultStyles = false,
    ) {
        MaterialTheme {
            val focusManager = LocalFocusManager.current
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            ) {
                val viewModel = remember { OriginSettingsViewModel() }

                OriginSettings(
                    allowedOrigins = viewModel.origins.collectAsState().value,
                    allowedOriginsError = viewModel.allowedOriginsError.collectAsState().value,
                    onOriginAdd = viewModel::onOriginAdd,
                    onOriginRemove = viewModel::onOriginRemove,
                    newOriginError = viewModel.newOriginError.collectAsState().value
                )
            }
        }
    }
}