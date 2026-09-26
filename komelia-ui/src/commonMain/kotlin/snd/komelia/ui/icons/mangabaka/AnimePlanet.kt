package snd.komelia.ui.icons.mangabaka

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.AnimePlanet: ImageVector
    get() {
        if (_AnimePlanet != null) {
            return _AnimePlanet!!
        }
        _AnimePlanet = ImageVector.Builder(
            name = "AnimePlanet",
            defaultWidth = 128.dp,
            defaultHeight = 128.dp,
            viewportWidth = 4.233f,
            viewportHeight = 4.233f
        ).apply {
            path(fill = SolidColor(Color(0xFF1C3867))) {
                moveTo(0.794f, 0f)
                horizontalLineTo(3.44f)
                curveToRelative(0.44f, 0f, 0.793f, 0.354f, 0.793f, 0.794f)
                verticalLineTo(3.44f)
                curveToRelative(0f, 0.44f, -0.354f, 0.793f, -0.793f, 0.793f)
                horizontalLineTo(0.794f)
                arcTo(0.79f, 0.79f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 3.44f)
                verticalLineTo(0.794f)
                curveTo(0f, 0.354f, 0.354f, 0f, 0.794f, 0f)
            }
            path(fill = SolidColor(Color(0xFFF0574B))) {
                moveTo(2.117f, 0.926f)
                arcToRelative(1.19f, 1.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, -1.19f, 1.19f)
                arcToRelative(1f, 1f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.015f, 0.19f)
                curveToRelative(0.253f, 0.137f, 0.612f, 0.27f, 1.026f, 0.368f)
                curveToRelative(0.425f, 0.101f, 0.819f, 0.147f, 1.114f, 0.14f)
                arcToRelative(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.225f, -0.697f)
                arcTo(1.19f, 1.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.117f, 0.926f)
                moveToRelative(-1.114f, 1.61f)
                arcToRelative(1.19f, 1.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, 1.114f, 0.771f)
                arcToRelative(1.2f, 1.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0.813f, -0.32f)
                arcToRelative(6.5f, 6.5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -1.927f, -0.45f)
                close()
            }
            path(fill = SolidColor(Color(0xFFF69330))) {
                moveTo(0.935f, 1.522f)
                curveToRelative(-0.346f, 0.017f, -0.669f, 0.093f, -0.709f, 0.232f)
                curveToRelative(-0.082f, 0.286f, 0.66f, 0.695f, 1.67f, 0.936f)
                reflectiveCurveToRelative(1.941f, 0.24f, 2.024f, -0.045f)
                curveToRelative(0.04f, -0.139f, -0.203f, -0.344f, -0.49f, -0.511f)
                lineToRelative(-0.002f, 0.08f)
                curveToRelative(0.133f, 0.099f, 0.195f, 0.212f, 0.172f, 0.291f)
                curveToRelative(-0.064f, 0.222f, -0.787f, 0.238f, -1.632f, 0.037f)
                reflectiveCurveTo(0.498f, 1.997f, 0.562f, 1.775f)
                curveToRelative(0.024f, -0.081f, 0.153f, -0.149f, 0.331f, -0.174f)
                close()
            }
        }.build()

        return _AnimePlanet!!
    }

@Suppress("ObjectPropertyName")
private var _AnimePlanet: ImageVector? = null

