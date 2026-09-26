package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagRu: ImageVector
    get() {
        if (_FlagRu != null) {
            return _FlagRu!!
        }
        _FlagRu = ImageVector.Builder(
            name = "FlagRu",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(0f, 0f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(160f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF0039A6))) {
                moveTo(0f, 160f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(160f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFD52B1E))) {
                moveTo(0f, 320f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(160f)
                horizontalLineTo(0f)
                close()
            }
        }.build()

        return _FlagRu!!
    }

@Suppress("ObjectPropertyName")
private var _FlagRu: ImageVector? = null

