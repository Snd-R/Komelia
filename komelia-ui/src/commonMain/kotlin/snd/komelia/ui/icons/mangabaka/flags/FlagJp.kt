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

val AppIcons.FlagJp: ImageVector
    get() {
        if (_FlagJp != null) {
            return _FlagJp!!
        }
        _FlagJp = ImageVector.Builder(
            name = "FlagJp",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(640f)
                    verticalLineToRelative(480f)
                    lineTo(0f, 480f)
                    close()
                }
            ) {
                path(
                    fill = SolidColor(Color.White),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(-40f, 0f)
                    horizontalLineToRelative(720f)
                    verticalLineToRelative(480f)
                    horizontalLineToRelative(-720f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFFBC002D)),
                    pathFillType = PathFillType.EvenOdd
                ) {
                    moveTo(320.05f, 240.02f)
                    moveToRelative(-149.2f, 0f)
                    arcToRelative(149.2f, 149.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, 298.41f, 0f)
                    arcToRelative(149.2f, 149.2f, 0f, isMoreThanHalf = true, isPositiveArc = true, -298.41f, 0f)
                }
            }
        }.build()

        return _FlagJp!!
    }

@Suppress("ObjectPropertyName")
private var _FlagJp: ImageVector? = null

