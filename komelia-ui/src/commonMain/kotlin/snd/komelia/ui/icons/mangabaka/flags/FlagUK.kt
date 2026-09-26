package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagUK: ImageVector
    get() {
        if (_FlagUk != null) {
            return _FlagUk!!
        }
        _FlagUk = ImageVector.Builder(
            name = "FlagUk",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            path(fill = SolidColor(Color(0xFF012169))) {
                moveTo(0f, 0f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(480f)
                horizontalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveToRelative(75f, 0f)
                lineToRelative(244f, 181f)
                lineTo(562f, 0f)
                horizontalLineToRelative(78f)
                verticalLineToRelative(62f)
                lineTo(400f, 241f)
                lineToRelative(240f, 178f)
                verticalLineToRelative(61f)
                horizontalLineToRelative(-80f)
                lineTo(320f, 301f)
                lineTo(81f, 480f)
                horizontalLineTo(0f)
                verticalLineToRelative(-60f)
                lineToRelative(239f, -178f)
                lineTo(0f, 64f)
                verticalLineTo(0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFC8102E))) {
                moveToRelative(424f, 281f)
                lineToRelative(216f, 159f)
                verticalLineToRelative(40f)
                lineTo(369f, 281f)
                close()
                moveTo(240f, 301f)
                lineTo(246f, 336f)
                lineTo(54f, 480f)
                lineTo(0f, 480f)
                close()
                moveTo(640f, 0f)
                verticalLineToRelative(3f)
                lineTo(391f, 191f)
                lineToRelative(2f, -44f)
                lineTo(590f, 0f)
                close()
                moveTo(0f, 0f)
                lineToRelative(239f, 176f)
                horizontalLineToRelative(-60f)
                lineTo(0f, 42f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(241f, 0f)
                verticalLineToRelative(480f)
                horizontalLineToRelative(160f)
                verticalLineTo(0f)
                close()
                moveTo(0f, 160f)
                verticalLineToRelative(160f)
                horizontalLineToRelative(640f)
                verticalLineTo(160f)
                close()
            }
            path(fill = SolidColor(Color(0xFFC8102E))) {
                moveTo(0f, 193f)
                verticalLineToRelative(96f)
                horizontalLineToRelative(640f)
                verticalLineToRelative(-96f)
                close()
                moveTo(273f, 0f)
                verticalLineToRelative(480f)
                horizontalLineToRelative(96f)
                verticalLineTo(0f)
                close()
            }
        }.build()

        return _FlagUk!!
    }

@Suppress("ObjectPropertyName")
private var _FlagUk: ImageVector? = null

