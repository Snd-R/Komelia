package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagKo: ImageVector
    get() {
        if (_FlagKo != null) {
            return _FlagKo!!
        }
        _FlagKo = ImageVector.Builder(
            name = "FlagKo",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(-0.01f, 0.03f)
                    lineToRelative(640.03f, 0f)
                    lineToRelative(0f, 480f)
                    lineTo(-0.01f, 480.02f)
                    close()
                }
            ) {
                path(
                    fill = SolidColor(Color.White),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(-0.01f, 0.03f)
                    lineTo(640.11f, 0.03f)
                    lineToRelative(0f, 480f)
                    lineTo(-0.01f, 480.02f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFF000001)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(70.12f, 145.81f)
                    lineTo(136.7f, 45.98f)
                    lineToRelative(16.64f, 11.1f)
                    lineTo(86.76f, 156.91f)
                    close()
                    moveTo(95.08f, 162.46f)
                    lineTo(161.66f, 62.63f)
                    lineToRelative(16.64f, 11.1f)
                    lineTo(111.72f, 173.56f)
                    close()
                    moveTo(120.04f, 179.1f)
                    lineTo(186.62f, 79.27f)
                    lineToRelative(16.64f, 11.1f)
                    lineTo(136.67f, 190.2f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFF000001)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(436.18f, 389.95f)
                    lineTo(502.76f, 290.11f)
                    lineToRelative(16.64f, 11.1f)
                    lineTo(452.82f, 401.04f)
                    close()
                    moveTo(461.14f, 406.59f)
                    lineTo(527.72f, 306.76f)
                    lineToRelative(16.64f, 11.1f)
                    lineTo(477.78f, 417.69f)
                    close()
                    moveTo(486.1f, 423.24f)
                    lineTo(552.68f, 323.4f)
                    lineToRelative(16.64f, 11.1f)
                    lineTo(502.73f, 434.33f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Black),
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1f,
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(461.15f, 334.48f)
                    lineToRelative(83.2f, 55.48f)
                }
                path(
                    fill = SolidColor(Color(0xFFCD2E3A)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(219.88f, 173.58f)
                    arcToRelative(120f, 120f, 79.89f, isMoreThanHalf = false, isPositiveArc = true, 199.67f, 133.16f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFF0047A0)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(219.88f, 173.58f)
                    arcToRelative(120f, 120f, 79.89f, isMoreThanHalf = false, isPositiveArc = false, 199.67f, 133.16f)
                    arcTo(60f, 60f, 80.67f, isMoreThanHalf = false, isPositiveArc = false, 319.72f, 240.16f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFFCD2E3A)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(269.8f, 206.87f)
                    moveToRelative(-33.29f, 49.92f)
                    arcToRelative(60f, 60f, 73.14f, isMoreThanHalf = true, isPositiveArc = true, 66.58f, -99.83f)
                    arcToRelative(60f, 60f, 73.14f, isMoreThanHalf = true, isPositiveArc = true, -66.58f, 99.83f)
                }
                path(
                    fill = SolidColor(Color(0xFF000001)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(136.98f, 434.37f)
                    lineTo(70.39f, 334.54f)
                    lineToRelative(16.64f, -11.1f)
                    lineTo(153.61f, 423.27f)
                    close()
                    moveTo(161.93f, 417.73f)
                    lineTo(95.35f, 317.89f)
                    lineToRelative(16.64f, -11.1f)
                    lineTo(178.57f, 406.63f)
                    close()
                    moveTo(186.89f, 401.08f)
                    lineTo(120.31f, 301.25f)
                    lineToRelative(16.64f, -11.1f)
                    lineTo(203.53f, 389.98f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFF000001)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(503.04f, 190.24f)
                    lineTo(436.45f, 90.41f)
                    lineToRelative(16.64f, -11.1f)
                    lineTo(519.67f, 179.14f)
                    close()
                    moveTo(527.99f, 173.59f)
                    lineTo(461.41f, 73.76f)
                    lineToRelative(16.64f, -11.1f)
                    lineTo(544.63f, 162.5f)
                    close()
                    moveTo(552.95f, 156.95f)
                    lineTo(486.37f, 57.11f)
                    lineToRelative(16.64f, -11.1f)
                    lineTo(569.59f, 145.85f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Black),
                    stroke = SolidColor(Color.White),
                    strokeLineWidth = 1f,
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(124.48f, 370.58f)
                    lineToRelative(24.96f, -16.65f)
                    moveTo(461.42f, 145.87f)
                    lineToRelative(29.12f, -19.42f)
                    moveToRelative(24.96f, -16.65f)
                    lineToRelative(24.96f, -16.65f)
                }
            }
        }.build()

        return _FlagKo!!
    }

@Suppress("ObjectPropertyName")
private var _FlagKo: ImageVector? = null

