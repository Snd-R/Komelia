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


val AppIcons.Outlined.NewsStand: ImageVector
    get() {
        if (_newsstand != null) {
            return _newsstand!!
        }
        _newsstand =
            ImageVector.Builder(
                name = "newsstand",
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
                        moveTo(2f, 20f)
                        verticalLineTo(18f)
                        horizontalLineTo(22f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(2f)
                        close()
                        moveTo(4f, 16f)
                        verticalLineTo(8f)
                        horizontalLineTo(6f)
                        verticalLineToRelative(8f)
                        horizontalLineTo(4f)
                        close()
                        moveToRelative(4f, 0f)
                        verticalLineTo(4f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(16f)
                        horizontalLineTo(8f)
                        close()
                        moveToRelative(4f, 0f)
                        verticalLineTo(4f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(16f)
                        horizontalLineTo(12f)
                        close()
                        moveToRelative(7f, 0f)
                        lineTo(15f, 9f)
                        lineTo(16.75f, 8f)
                        lineToRelative(4f, 7f)
                        lineTo(19f, 16f)
                        close()
                    }
                }
                .build()
        return _newsstand!!
    }

private var _newsstand: ImageVector? = null
