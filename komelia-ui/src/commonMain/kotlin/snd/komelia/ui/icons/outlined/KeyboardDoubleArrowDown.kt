package snd.komelia.ui.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

@Suppress("CheckReturnValue")
val AppIcons.Outlined.KeyboardDoubleArrowDown: ImageVector
    get() {
        if (_keyboard_double_arrow_down != null) {
            return _keyboard_double_arrow_down!!
        }
        _keyboard_double_arrow_down =
            ImageVector.Builder(
                name = "keyboard_double_arrow_down",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(12f, 19f)
                        lineTo(6f, 13f)
                        lineTo(7.4f, 11.6f)
                        lineTo(12f, 16.18f)
                        lineTo(16.6f, 11.6f)
                        lineTo(18f, 13f)
                        lineToRelative(-6f, 6f)
                        close()
                        moveToRelative(0f, -6f)
                        lineTo(6f, 7f)
                        lineTo(7.4f, 5.6f)
                        lineTo(12f, 10.17f)
                        lineTo(16.6f, 5.6f)
                        lineTo(18f, 7f)
                        lineToRelative(-6f, 6f)
                        close()
                    }
                }
                .build()
        return _keyboard_double_arrow_down!!
    }

private var _keyboard_double_arrow_down: ImageVector? = null
