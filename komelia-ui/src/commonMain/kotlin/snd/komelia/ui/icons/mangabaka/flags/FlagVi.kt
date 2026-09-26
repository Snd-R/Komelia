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

val AppIcons.FlagVi: ImageVector
    get() {
        if (_FlagVi != null) {
            return _FlagVi!!
        }
        _FlagVi = ImageVector.Builder(
            name = "FlagVi",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0.03f, 0f)
                    lineToRelative(639.94f, 0f)
                    lineToRelative(0f, 480f)
                    lineTo(0.03f, 480f)
                    close()
                }
            ) {
                path(
                    fill = SolidColor(Color(0xFFDA251D)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(-40f, 0f)
                    lineToRelative(720f, 0f)
                    lineToRelative(0f, 480f)
                    lineToRelative(-720f, 0f)
                    close()
                }
                path(
                    fill = SolidColor(Color.Yellow),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(407.75f, 357.19f)
                    lineTo(323.75f, 294.66f)
                    lineToRelative(-83.44f, 63.09f)
                    lineTo(271.25f, 255f)
                    lineToRelative(-83.44f, -63.47f)
                    lineToRelative(103.22f, -0.94f)
                    lineToRelative(32.06f, -102.56f)
                    lineTo(355.63f, 190.31f)
                    lineToRelative(103.22f, 0.09f)
                    lineToRelative(-82.97f, 64.13f)
                    lineToRelative(31.78f, 102.75f)
                    close()
                }
            }
        }.build()

        return _FlagVi!!
    }

@Suppress("ObjectPropertyName")
private var _FlagVi: ImageVector? = null

