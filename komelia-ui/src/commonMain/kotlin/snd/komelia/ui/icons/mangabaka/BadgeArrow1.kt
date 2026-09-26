package snd.komelia.ui.icons.mangabaka

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.BadgeArrow1: ImageVector
    get() {
        if (_BadgeArrow1 != null) {
            return _BadgeArrow1!!
        }
        _BadgeArrow1 = ImageVector.Builder(
            name = "BadgeArrow1",
            defaultWidth = 9.dp,
            defaultHeight = 5.8.dp,
            viewportWidth = 9f,
            viewportHeight = 5.8f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(1.5f, 4.4f)
                lineToRelative(3f, -3f)
                lineToRelative(3f, 3f)
            }
        }.build()

        return _BadgeArrow1!!
    }

@Suppress("ObjectPropertyName")
private var _BadgeArrow1: ImageVector? = null

