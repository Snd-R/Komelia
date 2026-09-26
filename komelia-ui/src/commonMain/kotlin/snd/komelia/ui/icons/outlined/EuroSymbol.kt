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


val AppIcons.Outlined.EuroSymbol: ImageVector
    get() {
        if (_euro_symbol != null) {
            return _euro_symbol!!
        }
        _euro_symbol =
            ImageVector.Builder(
                name = "euro_symbol",
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
                        moveTo(15f, 21f)
                        quadTo(12.05f, 21f, 9.75f, 19.33f)
                        reflectiveQuadTo(6.5f, 15f)
                        horizontalLineTo(3f)
                        verticalLineTo(13f)
                        horizontalLineTo(6.05f)
                        quadTo(6f, 12.73f, 6f, 12.5f)
                        quadTo(6f, 12.27f, 6f, 12f)
                        reflectiveQuadTo(6f, 11.5f)
                        quadTo(6f, 11.27f, 6.05f, 11f)
                        horizontalLineTo(3f)
                        verticalLineTo(9f)
                        horizontalLineTo(6.5f)
                        quadTo(7.45f, 6.35f, 9.75f, 4.67f)
                        reflectiveQuadTo(15f, 3f)
                        quadToRelative(1.73f, 0f, 3.26f, 0.6f)
                        reflectiveQuadTo(21f, 5.3f)
                        lineTo(19.25f, 7.05f)
                        quadTo(18.38f, 6.32f, 17.29f, 5.91f)
                        reflectiveQuadTo(15f, 5.5f)
                        quadToRelative(-1.88f, 0f, -3.41f, 0.96f)
                        quadTo(10.05f, 7.43f, 9.25f, 9f)
                        horizontalLineTo(15f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(8.6f)
                        quadTo(8.55f, 11.27f, 8.53f, 11.5f)
                        reflectiveQuadTo(8.5f, 12f)
                        quadToRelative(0f, 0.27f, 0.03f, 0.5f)
                        reflectiveQuadTo(8.6f, 13f)
                        horizontalLineTo(15f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(9.25f)
                        quadToRelative(0.8f, 1.57f, 2.34f, 2.54f)
                        reflectiveQuadTo(15f, 18.5f)
                        quadToRelative(1.2f, 0f, 2.31f, -0.41f)
                        quadToRelative(1.11f, -0.41f, 1.94f, -1.14f)
                        lineTo(21f, 18.7f)
                        quadToRelative(-1.2f, 1.1f, -2.74f, 1.7f)
                        quadTo(16.73f, 21f, 15f, 21f)
                        close()
                    }
                }
                .build()
        return _euro_symbol!!
    }

private var _euro_symbol: ImageVector? = null
