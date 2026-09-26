package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagCn: ImageVector
    get() {
        if (_FlagCn != null) {
            return _FlagCn!!
        }
        _FlagCn = ImageVector.Builder(
            name = "FlagCn",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            path(fill = SolidColor(Color(0xFFEE1C25))) {
                moveTo(0f, 0f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(480f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color.Yellow)) {
                moveTo(76.8f, 177.6f)
                lineTo(120f, 48f)
                lineTo(163.2f, 177.6f)
                lineTo(48f, 98.4f)
                lineToRelative(144f, 0f)
                close()
            }
            path(fill = SolidColor(Color.Yellow)) {
                moveTo(264.17f, 50.48f)
                lineTo(219.71f, 60.34f)
                lineTo(249.37f, 25.78f)
                lineTo(246.46f, 72.29f)
                lineToRelative(-24.67f, -41.17f)
                close()
            }
            path(fill = SolidColor(Color.Yellow)) {
                moveTo(309.04f, 107.35f)
                lineTo(264.24f, 99.19f)
                lineTo(304.98f, 78.84f)
                lineTo(284.26f, 120.58f)
                lineToRelative(-6.77f, -47.52f)
                close()
            }
            path(fill = SolidColor(Color.Yellow)) {
                moveTo(302.5f, 187.12f)
                lineTo(264.93f, 161.4f)
                lineTo(310.42f, 159.43f)
                lineTo(274.48f, 189.1f)
                lineToRelative(13.2f, -46.15f)
                close()
            }
            path(fill = SolidColor(Color.Yellow)) {
                moveTo(245.99f, 239.24f)
                lineTo(221.26f, 201f)
                lineTo(263.99f, 216.76f)
                lineTo(219.38f, 230.24f)
                lineToRelative(30f, -37.47f)
                close()
            }
        }.build()

        return _FlagCn!!
    }

@Suppress("ObjectPropertyName")
private var _FlagCn: ImageVector? = null

