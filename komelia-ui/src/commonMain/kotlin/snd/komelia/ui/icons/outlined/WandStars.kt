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
val AppIcons.Outlined.WandStars: ImageVector
    get() {
        if (_wand_stars != null) {
            return _wand_stars!!
        }
        _wand_stars =
            ImageVector.Builder(
                name = "wand_stars",
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
                        moveTo(4.4f, 21f)
                        lineTo(3f, 19.6f)
                        lineToRelative(7.53f, -7.55f)
                        lineTo(6f, 10.93f)
                        lineTo(10.95f, 7.85f)
                        lineTo(10.53f, 2f)
                        lineTo(15f, 5.77f)
                        lineToRelative(5.4f, -2.2f)
                        lineTo(18.23f, 9f)
                        lineTo(22f, 13.45f)
                        lineToRelative(-5.85f, -0.4f)
                        lineTo(13.05f, 18f)
                        lineTo(11.93f, 13.48f)
                        lineTo(4.4f, 21f)
                        close()
                        moveTo(5f, 8f)
                        lineTo(3f, 6f)
                        lineTo(5f, 4f)
                        lineTo(7f, 6f)
                        lineTo(5f, 8f)
                        close()
                        moveToRelative(8.88f, 4.92f)
                        lineToRelative(1.2f, -1.97f)
                        lineToRelative(2.33f, 0.18f)
                        lineTo(15.9f, 9.35f)
                        lineTo(16.78f, 7.2f)
                        lineTo(14.63f, 8.07f)
                        lineTo(12.85f, 6.6f)
                        lineToRelative(0.17f, 2.3f)
                        lineToRelative(-1.97f, 1.23f)
                        lineToRelative(2.25f, 0.55f)
                        lineToRelative(0.57f, 2.25f)
                        close()
                        moveTo(18f, 21f)
                        lineTo(16f, 19f)
                        lineToRelative(2f, -2f)
                        lineToRelative(2f, 2f)
                        lineToRelative(-2f, 2f)
                        close()
                        moveTo(14.23f, 9.75f)
                        close()
                    }
                }
                .build()
        return _wand_stars!!
    }

private var _wand_stars: ImageVector? = null
