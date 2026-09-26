package snd.komelia.ui.icons.mangabaka

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.MangaUpdates: ImageVector
    get() {
        if (_MangaUpdates != null) {
            return _MangaUpdates!!
        }
        _MangaUpdates = ImageVector.Builder(
            name = "MangaUpdates",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFCBD6E8)),
                stroke = SolidColor(Color(0xFF92A0AD)),
                strokeLineWidth = 1f
            ) {
                moveToRelative(3f, 0.5f)
                horizontalLineToRelative(10f)
                curveToRelative(1.385f, 0f, 2.5f, 1.115f, 2.5f, 2.5f)
                verticalLineToRelative(10f)
                curveToRelative(0f, 1.385f, -1.115f, 2.5f, -2.5f, 2.5f)
                horizontalLineToRelative(-10f)
                curveToRelative(-1.385f, 0f, -2.5f, -1.115f, -2.5f, -2.5f)
                verticalLineToRelative(-10f)
                curveToRelative(0f, -1.385f, 1.115f, -2.5f, 2.5f, -2.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFF8C15))) {
                moveToRelative(14.22f, 12.29f)
                quadToRelative(0f, 0.333f, -0.048f, 0.681f)
                quadToRelative(-0.048f, 0.348f, -0.19f, 0.633f)
                quadToRelative(-0.142f, 0.285f, -0.396f, 0.475f)
                quadToRelative(-0.253f, 0.19f, -0.665f, 0.19f)
                quadToRelative(-0.507f, 0f, -0.776f, -0.317f)
                quadToRelative(-0.253f, -0.317f, -0.348f, -0.776f)
                quadToRelative(-0.158f, -0.76f, -0.206f, -1.52f)
                quadToRelative(-0.032f, -0.76f, -0.063f, -1.536f)
                quadToRelative(-0.016f, -0.776f, -0.079f, -1.536f)
                quadToRelative(-0.063f, -0.76f, -0.253f, -1.52f)
                quadToRelative(-0.174f, 0.602f, -0.364f, 1.204f)
                quadToRelative(-0.19f, 0.602f, -0.412f, 1.188f)
                quadToRelative(-0.174f, 0.428f, -0.348f, 0.871f)
                quadToRelative(-0.158f, 0.428f, -0.285f, 0.871f)
                quadToRelative(-0.111f, 0.364f, -0.19f, 0.744f)
                quadToRelative(-0.079f, 0.38f, -0.174f, 0.744f)
                quadToRelative(-0.032f, 0.142f, -0.095f, 0.364f)
                quadToRelative(-0.063f, 0.206f, -0.158f, 0.428f)
                quadToRelative(-0.079f, 0.222f, -0.174f, 0.412f)
                reflectiveQuadToRelative(-0.206f, 0.285f)
                quadToRelative(-0.253f, 0.222f, -0.602f, 0.301f)
                quadToRelative(-0.333f, 0.095f, -0.649f, 0.095f)
                quadToRelative(-0.507f, 0f, -0.808f, -0.364f)
                quadToRelative(-0.301f, -0.364f, -0.443f, -0.808f)
                quadToRelative(-0.111f, -0.333f, -0.174f, -0.665f)
                quadToRelative(-0.063f, -0.348f, -0.142f, -0.681f)
                quadToRelative(-0.206f, -0.903f, -0.491f, -1.774f)
                quadToRelative(-0.269f, -0.871f, -0.507f, -1.774f)
                quadToRelative(-0.317f, 0.713f, -0.57f, 1.425f)
                quadToRelative(-0.238f, 0.713f, -0.333f, 1.473f)
                quadToRelative(-0.079f, 0.554f, -0.095f, 1.124f)
                quadToRelative(0f, 0.554f, -0.142f, 1.109f)
                quadToRelative(-0.111f, 0.412f, -0.364f, 0.633f)
                quadToRelative(-0.238f, 0.238f, -0.681f, 0.238f)
                quadToRelative(-0.412f, 0f, -0.681f, -0.174f)
                quadToRelative(-0.269f, -0.158f, -0.428f, -0.428f)
                quadToRelative(-0.158f, -0.269f, -0.222f, -0.602f)
                quadToRelative(-0.048f, -0.348f, -0.048f, -0.697f)
                quadToRelative(0f, -0.744f, 0.19f, -1.584f)
                quadToRelative(0.19f, -0.855f, 0.459f, -1.71f)
                quadToRelative(0.269f, -0.855f, 0.554f, -1.695f)
                quadToRelative(0.301f, -0.839f, 0.523f, -1.568f)
                quadToRelative(0.206f, -0.713f, 0.301f, -1.409f)
                quadToRelative(0.111f, -0.697f, 0.269f, -1.409f)
                quadToRelative(0.063f, -0.301f, 0.206f, -0.586f)
                quadToRelative(0.158f, -0.301f, 0.364f, -0.523f)
                quadToRelative(0.222f, -0.222f, 0.507f, -0.364f)
                quadToRelative(0.285f, -0.142f, 0.618f, -0.142f)
                quadToRelative(0.238f, 0f, 0.491f, 0.079f)
                quadToRelative(0.253f, 0.063f, 0.459f, 0.206f)
                quadToRelative(0.206f, 0.127f, 0.333f, 0.333f)
                quadToRelative(0.127f, 0.206f, 0.127f, 0.491f)
                quadToRelative(0f, 0.206f, -0.032f, 0.412f)
                quadToRelative(-0.032f, 0.19f, -0.032f, 0.396f)
                quadToRelative(0f, 1.742f, 0.238f, 3.437f)
                quadToRelative(0.237f, 1.695f, 0.729f, 3.373f)
                quadToRelative(0.649f, -1.489f, 1.093f, -3.041f)
                quadToRelative(0.443f, -1.552f, 0.855f, -3.12f)
                quadToRelative(0.127f, -0.459f, 0.238f, -0.934f)
                quadToRelative(0.111f, -0.475f, 0.333f, -0.919f)
                quadToRelative(0.206f, -0.412f, 0.507f, -0.665f)
                quadToRelative(0.317f, -0.269f, 0.792f, -0.269f)
                quadToRelative(0.285f, 0f, 0.57f, 0.063f)
                quadToRelative(0.301f, 0.048f, 0.539f, 0.19f)
                quadToRelative(0.238f, 0.127f, 0.38f, 0.364f)
                quadToRelative(0.158f, 0.222f, 0.158f, 0.57f)
                quadToRelative(0f, 0.253f, -0.032f, 0.507f)
                quadToRelative(-0.016f, 0.238f, -0.016f, 0.491f)
                quadToRelative(0f, 1.188f, 0.142f, 2.376f)
                quadToRelative(0.158f, 1.172f, 0.428f, 2.344f)
                quadToRelative(0.222f, 0.998f, 0.364f, 1.995f)
                quadToRelative(0.158f, 0.982f, 0.158f, 1.995f)
                close()
            }
        }.build()

        return _MangaUpdates!!
    }

@Suppress("ObjectPropertyName")
private var _MangaUpdates: ImageVector? = null

