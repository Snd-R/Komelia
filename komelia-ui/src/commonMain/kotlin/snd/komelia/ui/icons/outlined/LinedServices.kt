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
public val AppIcons.Outlined.LinkedServices: ImageVector
    get() {
        if (_linked_services != null) {
            return _linked_services!!
        }
        _linked_services =
            ImageVector.Builder(
                name = "linked_services",
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
                        moveTo(19f, 9f)
                        quadTo(17.58f, 9f, 16.53f, 8.15f)
                        reflectiveQuadTo(15.13f, 6f)
                        horizontalLineTo(8.85f)
                        quadTo(8.58f, 7.05f, 7.81f, 7.81f)
                        reflectiveQuadTo(6f, 8.85f)
                        verticalLineToRelative(6.28f)
                        quadToRelative(1.3f, 0.35f, 2.15f, 1.4f)
                        quadTo(9f, 17.58f, 9f, 19f)
                        quadToRelative(0f, 1.65f, -1.17f, 2.82f)
                        reflectiveQuadTo(5f, 23f)
                        reflectiveQuadTo(2.18f, 21.83f)
                        reflectiveQuadTo(1f, 19f)
                        quadTo(1f, 17.58f, 1.85f, 16.52f)
                        quadTo(2.7f, 15.48f, 4f, 15.13f)
                        verticalLineTo(8.85f)
                        quadTo(2.7f, 8.5f, 1.85f, 7.45f)
                        reflectiveQuadTo(1f, 5f)
                        quadTo(1f, 3.35f, 2.18f, 2.17f)
                        reflectiveQuadTo(5f, 1f)
                        quadTo(6.4f, 1f, 7.45f, 1.85f)
                        reflectiveQuadTo(8.85f, 4f)
                        horizontalLineToRelative(6.28f)
                        quadToRelative(0.35f, -1.3f, 1.4f, -2.15f)
                        reflectiveQuadTo(19f, 1f)
                        quadToRelative(1.65f, 0f, 2.83f, 1.17f)
                        reflectiveQuadTo(23f, 5f)
                        reflectiveQuadTo(21.83f, 7.82f)
                        reflectiveQuadTo(19f, 9f)
                        close()
                        moveTo(5f, 21f)
                        quadToRelative(0.83f, 0f, 1.41f, -0.6f)
                        reflectiveQuadTo(7f, 19f)
                        quadTo(7f, 18.18f, 6.41f, 17.59f)
                        reflectiveQuadTo(5f, 17f)
                        quadTo(4.2f, 17f, 3.6f, 17.59f)
                        quadTo(3f, 18.18f, 3f, 19f)
                        quadToRelative(0f, 0.8f, 0.6f, 1.4f)
                        reflectiveQuadTo(5f, 21f)
                        close()
                        moveTo(5f, 7f)
                        quadTo(5.83f, 7f, 6.41f, 6.41f)
                        reflectiveQuadTo(7f, 5f)
                        quadTo(7f, 4.17f, 6.41f, 3.59f)
                        reflectiveQuadTo(5f, 3f)
                        quadTo(4.2f, 3f, 3.6f, 3.59f)
                        reflectiveQuadTo(3f, 5f)
                        quadTo(3f, 5.82f, 3.6f, 6.41f)
                        reflectiveQuadTo(5f, 7f)
                        close()
                        moveTo(19f, 23f)
                        quadToRelative(-1.65f, 0f, -2.82f, -1.18f)
                        reflectiveQuadTo(15f, 19f)
                        reflectiveQuadToRelative(1.18f, -2.82f)
                        reflectiveQuadTo(19f, 15f)
                        reflectiveQuadToRelative(2.83f, 1.18f)
                        reflectiveQuadTo(23f, 19f)
                        reflectiveQuadToRelative(-1.17f, 2.82f)
                        reflectiveQuadTo(19f, 23f)
                        close()
                        moveToRelative(0f, -2f)
                        quadToRelative(0.83f, 0f, 1.41f, -0.6f)
                        reflectiveQuadTo(21f, 19f)
                        quadToRelative(0f, -0.82f, -0.59f, -1.41f)
                        reflectiveQuadTo(19f, 17f)
                        quadToRelative(-0.82f, 0f, -1.41f, 0.59f)
                        quadTo(17f, 18.18f, 17f, 19f)
                        quadToRelative(0f, 0.8f, 0.59f, 1.4f)
                        reflectiveQuadTo(19f, 21f)
                        close()
                        moveTo(19f, 7f)
                        quadToRelative(0.83f, 0f, 1.41f, -0.59f)
                        reflectiveQuadTo(21f, 5f)
                        quadTo(21f, 4.17f, 20.41f, 3.59f)
                        reflectiveQuadTo(19f, 3f)
                        quadTo(18.18f, 3f, 17.59f, 3.59f)
                        reflectiveQuadTo(17f, 5f)
                        quadToRelative(0f, 0.82f, 0.59f, 1.41f)
                        reflectiveQuadTo(19f, 7f)
                        close()
                        moveTo(5f, 19f)
                        close()
                        moveTo(5f, 5f)
                        close()
                        moveTo(19f, 19f)
                        close()
                        moveTo(19f, 5f)
                        close()
                    }
                }
                .build()
        return _linked_services!!
    }

private var _linked_services: ImageVector? = null
