package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagId: ImageVector
    get() {
        if (_FlagId != null) {
            return _FlagId!!
        }
        _FlagId = ImageVector.Builder(
            name = "FlagId",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            path(fill = SolidColor(Color(0xFFE70011))) {
                moveTo(0f, 0f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(240f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(0f, 240f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(240f)
                horizontalLineTo(0f)
                close()
            }
        }.build()

        return _FlagId!!
    }

@Suppress("ObjectPropertyName")
private var _FlagId: ImageVector? = null

