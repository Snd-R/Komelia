package io.github.snd_r.komelia.ui.color.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.EXPANDED
import io.github.snd_r.komelia.platform.WindowSizeClass.FULL
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.color.ColorCorrectionType
import io.github.snd_r.komelia.ui.color.CurvesState
import io.github.snd_r.komelia.ui.color.LevelsState
import kotlin.math.roundToInt

@Composable
fun ColorCorrectionContent(
    currentCurveType: ColorCorrectionType,
    onCurveTypeChange: (ColorCorrectionType) -> Unit,
    curvesState: CurvesState,
    levelsState: LevelsState,
    displayImage: ImageBitmap?,
    onImageMaxSizeChange: (IntSize) -> Unit
) {
    val width = LocalWindowWidth.current
    when (width) {
        COMPACT, MEDIUM ->
            Box {
                val scrollState = rememberScrollState()
                Column(
                    Modifier.fillMaxSize()
                        .onGloballyPositioned {
                            onImageMaxSizeChange(IntSize(width = it.size.width, height = it.size.height))
                        }
                        .padding(horizontal = 10.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (displayImage == null) {
                        Spacer(Modifier.fillMaxSize())
                    } else {
                        EditorContent(
                            currentCurveType = currentCurveType,
                            onCurveTypeChange = onCurveTypeChange,
                            curvesState = curvesState,
                            levelsState = levelsState,
                            modifier = Modifier.heightIn(min = 600.dp)
                        )
                    }
                    if (displayImage != null) {
                        with(LocalDensity.current) {
                            Image(
                                displayImage,
                                null,
                                modifier = Modifier
                                    .width(displayImage.width.toDp())
                                    .heightIn(displayImage.height.toDp())
                                    .padding(top = 20.dp)
                            )
                        }
                    }
                }
                VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
            }

        EXPANDED, FULL -> Column {
            Row(
                modifier = Modifier.fillMaxSize()
                    .onGloballyPositioned {
                        onImageMaxSizeChange(
                            IntSize(
                                width = (it.size.width / 2.0).roundToInt(),
                                height = it.size.height
                            )
                        )
                    }
                    .padding(start = 20.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (displayImage == null) {
                    Spacer(Modifier.weight(1f))
                } else {
                    EditorContent(
                        currentCurveType = currentCurveType,
                        onCurveTypeChange = onCurveTypeChange,
                        curvesState = curvesState,
                        levelsState = levelsState,
                        modifier = Modifier.weight(1f).padding(bottom = 20.dp)
                    )
                }
                Spacer(Modifier.width(20.dp))

                if (displayImage != null) {
                    with(LocalDensity.current) {
                        Image(
                            displayImage,
                            null,
                            modifier = Modifier
                                .width(displayImage.width.toDp())
                                .heightIn(displayImage.height.toDp())
                                .padding(top = 20.dp)
                        )
                    }
                }
            }

        }
    }

}

@Composable
private fun EditorContent(
    currentCurveType: ColorCorrectionType,
    onCurveTypeChange: (ColorCorrectionType) -> Unit,
    curvesState: CurvesState,
    levelsState: LevelsState,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SecondaryTabRow(selectedTabIndex = currentCurveType.ordinal) {
            Tab(
                selected = currentCurveType.ordinal == 0,
                onClick = { onCurveTypeChange(ColorCorrectionType.entries[0]) },
                modifier = Modifier.heightIn(min = 40.dp).pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text("Curves")
            }
            Tab(
                selected = currentCurveType.ordinal == 1,
                onClick = { onCurveTypeChange(ColorCorrectionType.entries[1]) },
                modifier = Modifier.heightIn(min = 40.dp).pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text("Levels")
            }
        }

        when (currentCurveType) {
            ColorCorrectionType.COLOR_CURVES -> {
                ColorCurvesContent(
                    curvePathData = curvesState.curvePathData.collectAsState().value,
                    histogramPathData = curvesState.histogramPaths.collectAsState().value,
                    selectedChannel = curvesState.currentChannel.collectAsState().value,
                    onChannelChange = curvesState::onCurveChannelChange,
                    onChannelReset = curvesState::onPointsReset,
                    onAllChannelsReset = curvesState::onAllPointsReset,
                    selectedPoint = curvesState.selectedPoint.collectAsState().value,
                    currentPointOffset = curvesState.selectedPointOffset255.collectAsState().value,
                    onPointChange = curvesState::onSelectedPointOffsetChange,
                    pointType = curvesState.pointType.collectAsState().value,
                    onPointTypeChange = curvesState::onPointTypeChange,
                    pointerIcon = curvesState.pointerIcon.collectAsState().value,
                    curvePointerPosition = curvesState.displayPointerCoordinates.collectAsState().value,
                    onKeyEvent = curvesState::onKeyEvent,
                    onPointerEvent = curvesState::onPointerEvent,
                    onCanvasSizeChange = curvesState::onCanvasSizeChange,
                    onDensityChange = curvesState::onDensityChange,
                    presetsState = curvesState.presetsState,
                )
            }

            ColorCorrectionType.COLOR_LEVELS -> ColorLevelContent(levelsState)
        }
    }
}


