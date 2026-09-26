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
val AppIcons.Outlined.DesignServices: ImageVector
    get() {
        if (_design_services != null) {
            return _design_services!!
        }
        _design_services =
            ImageVector.Builder(
                name = "design_services",
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
                        moveTo(8.8f, 10.95f)
                        lineTo(10.95f, 8.77f)
                        lineTo(9.55f, 7.35f)
                        lineToRelative(-1.1f, 1.1f)
                        lineTo(7.05f, 7.05f)
                        lineTo(8.13f, 5.95f)
                        lineTo(7f, 4.82f)
                        lineTo(4.83f, 7f)
                        lineTo(8.8f, 10.95f)
                        close()
                        moveTo(17f, 19.18f)
                        lineTo(19.18f, 17f)
                        lineTo(18.05f, 15.88f)
                        lineToRelative(-1.1f, 1.07f)
                        lineToRelative(-1.4f, -1.4f)
                        lineToRelative(1.07f, -1.1f)
                        lineTo(15.2f, 13.05f)
                        lineTo(13.05f, 15.2f)
                        lineTo(17f, 19.18f)
                        close()
                        moveTo(17.6f, 5f)
                        lineToRelative(1.43f, 1.43f)
                        lineTo(17.6f, 5f)
                        close()
                        moveTo(7.25f, 21f)
                        horizontalLineTo(3f)
                        verticalLineTo(16.75f)
                        lineTo(7.38f, 12.38f)
                        lineTo(2f, 7f)
                        lineTo(7f, 2f)
                        lineToRelative(5.4f, 5.4f)
                        lineTo(16.18f, 3.6f)
                        quadToRelative(0.3f, -0.3f, 0.68f, -0.45f)
                        reflectiveQuadTo(17.63f, 3f)
                        quadToRelative(0.4f, 0f, 0.78f, 0.15f)
                        reflectiveQuadTo(19.08f, 3.6f)
                        lineTo(20.4f, 4.95f)
                        quadToRelative(0.3f, 0.3f, 0.45f, 0.68f)
                        reflectiveQuadTo(21f, 6.4f)
                        reflectiveQuadTo(20.85f, 7.16f)
                        reflectiveQuadTo(20.4f, 7.82f)
                        lineToRelative(-3.78f, 3.8f)
                        lineTo(22f, 17f)
                        lineToRelative(-5f, 5f)
                        lineTo(11.63f, 16.63f)
                        lineTo(7.25f, 21f)
                        close()
                        moveTo(5f, 19f)
                        horizontalLineTo(6.4f)
                        lineTo(16.2f, 9.23f)
                        lineTo(14.78f, 7.8f)
                        lineTo(5f, 17.6f)
                        verticalLineTo(19f)
                        close()
                        moveTo(15.5f, 8.52f)
                        lineTo(14.78f, 7.8f)
                        lineTo(16.2f, 9.23f)
                        lineTo(15.5f, 8.52f)
                        close()
                    }
                }
                .build()
        return _design_services!!
    }

private var _design_services: ImageVector? = null
