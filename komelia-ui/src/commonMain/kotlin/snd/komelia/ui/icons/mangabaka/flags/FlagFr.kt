package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagFr: ImageVector
    get() {
        if (_FlagFr != null) {
            return _FlagFr!!
        }
        _FlagFr = ImageVector.Builder(
            name = "FlagFr",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(0f, 0f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(480f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000091))) {
                moveTo(0f, 0f)
                horizontalLineToRelative(213.3f)
                verticalLineToRelative(480f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFE1000F))) {
                moveTo(426.7f, 0f)
                horizontalLineTo(640f)
                verticalLineToRelative(480f)
                horizontalLineTo(426.7f)
                close()
            }
        }.build()

        return _FlagFr!!
    }

@Suppress("ObjectPropertyName")
private var _FlagFr: ImageVector? = null

