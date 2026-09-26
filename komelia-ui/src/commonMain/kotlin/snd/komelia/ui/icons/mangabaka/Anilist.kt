package snd.komelia.ui.icons.mangabaka

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.AniList: ImageVector
    get() {
        if (_AniList != null) {
            return _AniList!!
        }
        _AniList = ImageVector.Builder(
            name = "AniList",
            defaultWidth = 172.dp,
            defaultHeight = 172.dp,
            viewportWidth = 172f,
            viewportHeight = 172f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                strokeLineWidth = 1.0f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(0f, 0f)
                horizontalLineToRelative(256f)
                verticalLineToRelative(256f)
                horizontalLineToRelative(-256f)
                close()
            }

            path(
                fill = SolidColor(Color(0xFF02A9FF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(111.32f, 111.16f)
                lineTo(111.32f, 41.03f)
                curveTo(111.32f, 37.01f, 109.11f, 34.79f, 105.09f, 34.79f)
                lineTo(91.36f, 34.79f)
                curveTo(87.35f, 34.79f, 85.13f, 37.01f, 85.13f, 41.03f)
                curveTo(85.13f, 41.03f, 85.13f, 56.34f, 85.13f, 74.33f)
                curveTo(85.13f, 75.27f, 94.17f, 79.63f, 94.4f, 80.55f)
                curveTo(101.29f, 107.45f, 95.9f, 128.98f, 89.37f, 129.99f)
                curveTo(100.04f, 130.51f, 101.22f, 135.64f, 93.27f, 132.14f)
                curveTo(94.48f, 117.78f, 99.23f, 117.81f, 112.87f, 131.61f)
                curveTo(112.99f, 131.73f, 115.67f, 137.35f, 115.83f, 137.35f)
                curveTo(131.17f, 137.35f, 148.05f, 137.35f, 148.05f, 137.35f)
                curveTo(152.07f, 137.35f, 154.29f, 135.13f, 154.29f, 131.12f)
                lineTo(154.29f, 117.39f)
                curveTo(154.29f, 113.38f, 152.07f, 111.16f, 148.05f, 111.16f)
                lineTo(111.32f, 111.16f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFEFEFE)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(54.37f, 34.79f)
                lineTo(18.33f, 137.35f)
                lineTo(46.33f, 137.35f)
                lineTo(52.42f, 119.61f)
                lineTo(82.92f, 119.61f)
                lineTo(88.88f, 137.35f)
                lineTo(116.73f, 137.35f)
                lineTo(80.84f, 34.79f)
                lineTo(54.37f, 34.79f)
                close()
                moveTo(58.8f, 96.88f)
                lineTo(67.53f, 68.47f)
                lineTo(77.09f, 96.88f)
                lineTo(58.8f, 96.88f)
                close()
            }
        }.build()

        return _AniList!!
    }

@Suppress("ObjectPropertyName")
private var _AniList: ImageVector? = null

