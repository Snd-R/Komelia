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
public val AppIcons.Outlined.TripleArrowUp: ImageVector
    get() {
        if (_tripleArrowUp != null) {
            return _tripleArrowUp!!
        }
        _tripleArrowUp =
            ImageVector.Builder(
                name = "keyboard_double_arrow_up",
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
                        pathFillType = PathFillType.NonZero,
                    ) {
                        moveTo(7.4f, 18.4f)
                        lineTo(6f, 17f)
                        lineToRelative(6f, -6f)
                        lineToRelative(6f, 6f)
                        lineToRelative(-1.4f, 1.4f)
                        lineTo(12f, 13.83f)
                        lineTo(7.4f, 18.4f)
                        close()
                        moveToRelative(0f, -6f)
                        lineTo(6f, 11f)
                        lineTo(12f, 5f)
                        lineToRelative(6f, 6f)
                        lineToRelative(-1.4f, 1.4f)
                        lineTo(12f, 7.82f)
                        lineTo(7.4f, 12.4f)
                        close()
                    }
                }
                .build()
        return _tripleArrowUp!!
    }

private var _tripleArrowUp: ImageVector? = null
