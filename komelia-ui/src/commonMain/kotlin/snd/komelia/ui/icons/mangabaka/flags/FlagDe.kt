package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagDe: ImageVector
    get() {
        if (_FlagDe != null) {
            return _FlagDe!!
        }
        _FlagDe = ImageVector.Builder(
            name = "FlagDe",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFCC00))) {
                moveTo(0f, 320f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(160f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000001))) {
                moveTo(0f, 0f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(160f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color.Red)) {
                moveTo(0f, 160f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(160f)
                horizontalLineTo(0f)
                close()
            }
        }.build()

        return _FlagDe!!
    }

@Suppress("ObjectPropertyName")
private var _FlagDe: ImageVector? = null

