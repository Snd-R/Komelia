package snd.komelia.ui.color.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.ErrorContent
import snd.komelia.ui.common.components.LoadingMaxSizeIndicator
import snd.komelia.ui.platform.PlatformTitleBar
import snd.komga.client.book.KomgaBookId

class ColorCorrectionScreen(
    val bookId: KomgaBookId,
    val page: Int
) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getCurvesViewModel(bookId, page) }
        LaunchedEffect(Unit) { vm.initialize() }
        val navigator = LocalNavigator.currentOrThrow

        val coroutineScope = rememberCoroutineScope()
        Column {
            PlatformTitleBar {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            vm.onSave()
                            navigator.pop()
                        }
                    },
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
                Spacer(Modifier.width(10.dp).align(Alignment.Start).nonInteractive())
                Text(
                    text = "Color Correction",
                    modifier = Modifier.heightIn(max = 32.dp).align(Alignment.Start).nonInteractive()
                )

            }
            when (val state = vm.state.collectAsState().value) {
                LoadState.Loading, LoadState.Uninitialized -> LoadingMaxSizeIndicator()
                is LoadState.Error -> ErrorContent(state.exception, onExit = { navigator.pop() })
                is LoadState.Success<Unit> -> ColorCorrectionContent(
                    currentCurveType = vm.correctionType.collectAsState().value,
                    onCurveTypeChange = vm::onCurveTypeChange,
                    curvesState = vm.curvesState,
                    levelsState = vm.levelsState,
                    displayImage = vm.displayImage.collectAsState().value,
                    onImageMaxSizeChange = vm::onImageMaxSizeChange
                )
            }
        }
    }
}