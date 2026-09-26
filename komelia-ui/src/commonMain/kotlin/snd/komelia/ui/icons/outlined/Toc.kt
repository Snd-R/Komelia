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

val AppIcons.Outlined.Toc: ImageVector
    get() {
        if (_toc != null) {
            return _toc!!
        }
        _toc =
            ImageVector.Builder(
                name = "toc",
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
                        moveTo(3f, 17f)
                        verticalLineTo(15f)
                        horizontalLineTo(17f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(3f, 13f)
                        verticalLineTo(11f)
                        horizontalLineTo(17f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(3f, 9f)
                        verticalLineTo(7f)
                        horizontalLineTo(17f)
                        verticalLineTo(9f)
                        horizontalLineTo(3f)
                        close()
                        moveToRelative(17f, 8f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(19f, 16.43f, 19f, 16f)
                        reflectiveQuadToRelative(0.29f, -0.71f)
                        reflectiveQuadTo(20f, 15f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(21f, 16f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(20f, 17f)
                        close()
                        moveToRelative(0f, -4f)
                        quadToRelative(-0.42f, 0f, -0.71f, -0.29f)
                        quadTo(19f, 12.43f, 19f, 12f)
                        reflectiveQuadToRelative(0.29f, -0.71f)
                        reflectiveQuadTo(20f, 11f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(21f, 12f)
                        reflectiveQuadToRelative(-0.29f, 0.71f)
                        reflectiveQuadTo(20f, 13f)
                        close()
                        moveTo(20f, 9f)
                        quadTo(19.58f, 9f, 19.29f, 8.71f)
                        reflectiveQuadTo(19f, 8f)
                        quadTo(19f, 7.57f, 19.29f, 7.29f)
                        reflectiveQuadTo(20f, 7f)
                        quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                        reflectiveQuadTo(21f, 8f)
                        quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                        reflectiveQuadTo(20f, 9f)
                        close()
                    }
                }
                .build()
        return _toc!!
    }

private var _toc: ImageVector? = null
