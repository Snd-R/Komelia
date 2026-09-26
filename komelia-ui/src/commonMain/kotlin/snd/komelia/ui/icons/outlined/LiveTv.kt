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
val AppIcons.Outlined.LiveTv: ImageVector
    get() {
        if (_live_tv != null) {
            return _live_tv!!
        }
        _live_tv =
            ImageVector.Builder(
                name = "live_tv",
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
                        moveTo(9.5f, 15.5f)
                        lineToRelative(7f, -4.5f)
                        lineTo(9.5f, 6.5f)
                        verticalLineToRelative(9f)
                        close()
                        moveTo(8f, 21f)
                        verticalLineTo(19f)
                        horizontalLineTo(4f)
                        quadTo(3.18f, 19f, 2.59f, 18.41f)
                        reflectiveQuadTo(2f, 17f)
                        verticalLineTo(5f)
                        quadTo(2f, 4.17f, 2.59f, 3.59f)
                        reflectiveQuadTo(4f, 3f)
                        horizontalLineTo(20f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(22f, 5f)
                        verticalLineTo(17f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(20f, 19f)
                        horizontalLineTo(16f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(8f)
                        close()
                        moveTo(4f, 17f)
                        horizontalLineTo(20f)
                        verticalLineTo(5f)
                        horizontalLineTo(4f)
                        verticalLineTo(17f)
                        close()
                        moveToRelative(0f, 0f)
                        verticalLineTo(5f)
                        verticalLineTo(17f)
                        close()
                    }
                }
                .build()
        return _live_tv!!
    }

private var _live_tv: ImageVector? = null
