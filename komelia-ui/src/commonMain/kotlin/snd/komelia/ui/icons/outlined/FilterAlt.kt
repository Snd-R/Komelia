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
val AppIcons.Outlined.FilterAlt: ImageVector
    get() {
        if (_filter_alt != null) {
            return _filter_alt!!
        }
        _filter_alt =
            ImageVector.Builder(
                name = "filter_alt",
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
                        moveTo(11f, 20f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(10f, 19.43f, 10f, 19f)
                        verticalLineTo(13f)
                        lineTo(4.2f, 5.6f)
                        quadTo(3.83f, 5.1f, 4.09f, 4.55f)
                        reflectiveQuadTo(5f, 4f)
                        horizontalLineTo(19f)
                        quadToRelative(0.65f, 0f, 0.91f, 0.55f)
                        reflectiveQuadTo(19.8f, 5.6f)
                        lineTo(14f, 13f)
                        verticalLineToRelative(6f)
                        quadToRelative(0f, 0.43f, -0.29f, 0.71f)
                        reflectiveQuadTo(13f, 20f)
                        horizontalLineTo(11f)
                        close()
                        moveToRelative(1f, -7.7f)
                        lineTo(16.95f, 6f)
                        horizontalLineTo(7.05f)
                        lineTo(12f, 12.3f)
                        close()
                        moveToRelative(0f, 0f)
                        close()
                    }
                }
                .build()
        return _filter_alt!!
    }

private var _filter_alt: ImageVector? = null
