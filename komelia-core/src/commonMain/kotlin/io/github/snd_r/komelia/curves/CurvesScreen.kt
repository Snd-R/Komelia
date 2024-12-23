package io.github.snd_r.komelia.curves

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.platform.PlatformTitleBar
import io.github.snd_r.komelia.ui.LocalViewModelFactory

class CurvesScreen : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getCurvesViewModel() }

        Column {
            PlatformTitleBar { }
            CurvesContent(
                points = vm.controlPoints.collectAsState().value,
                referenceLine = vm.referenceLine.collectAsState().value,
                selectedChannel = vm.currentChannel.collectAsState().value,
                availableChannels = vm.availableChannels,
                onChannelChange = vm::onCurveChannelChange,
                onPointsReset = vm::onPointsReset,
                colorCurve = vm.colorCurvePath.collectAsState().value,
                redCurve = vm.redCurvePath.collectAsState().value,
                greenCurve = vm.greenCurvePath.collectAsState().value,
                blueCurve = vm.blueCurvePath.collectAsState().value,
                histogram = vm.histogram,
                selectedPoint = vm.selectedPoint.collectAsState().value,
                pointType = vm.pointType.collectAsState().value,
                onPointTypeChange = vm::onPointTypeChange,
                pointerIcon = vm.pointerIcon.collectAsState().value,
                pointerPosition = vm.pointerCoordinates.collectAsState().value,
                onPointerEvent = vm::onPointerEvent,
                onCanvasSizeChange = vm::onCanvasSizeChange,
                onDensityChange = vm::onDensityChange,
                displayImage = vm.displayImage.collectAsState(null).value,
                onMaxHeightChange = vm::onImageMaxHeightChange
            )
        }
    }
}