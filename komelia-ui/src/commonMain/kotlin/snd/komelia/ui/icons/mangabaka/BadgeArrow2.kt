package snd.komelia.ui.icons.mangabaka

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.BadgeArrow2: ImageVector
    get() {
        if (_BadgeArrow2 != null) {
            return _BadgeArrow2!!
        }
        _BadgeArrow2 = ImageVector.Builder(
            name = "BadgeArrow2",
            defaultWidth = 9.dp,
            defaultHeight = 9.1.dp,
            viewportWidth = 9f,
            viewportHeight = 9.1f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(1.5f, 7.7f)
                lineToRelative(3f, -3f)
                lineToRelative(3f, 3f)
            }
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

        return _BadgeArrow2!!
    }

@Suppress("ObjectPropertyName")
private var _BadgeArrow2: ImageVector? = null

