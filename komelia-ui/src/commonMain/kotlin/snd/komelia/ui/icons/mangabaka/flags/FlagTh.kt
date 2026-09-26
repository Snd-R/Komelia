package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagTh: ImageVector
    get() {
        if (_FlagTh != null) {
            return _FlagTh!!
        }
        _FlagTh = ImageVector.Builder(
            name = "FlagTh",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFF4F5F8)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(0f, 0f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(480f)
                horizontalLineTo(0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF2D2A4A)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(0f, 162.5f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(160f)
                horizontalLineTo(0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFA51931)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(0f, 0f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(82.5f)
                lineTo(0f, 82.5f)
                close()
                moveTo(0f, 400f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(80f)
                lineTo(0f, 480f)
                close()
            }
        }.build()

        return _FlagTh!!
    }

@Suppress("ObjectPropertyName")
private var _FlagTh: ImageVector? = null

