package snd.komelia.ui.icons.mangabaka

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.MyAnimeList: ImageVector
    get() {
        if (_MyAnimeList != null) {
            return _MyAnimeList!!
        }
        _MyAnimeList = ImageVector.Builder(
            name = "MyAnimeList",
            defaultWidth = 256.dp,
            defaultHeight = 256.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF2E51A2)),
                strokeLineWidth = 0.999999f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(0f, 0f)
                horizontalLineToRelative(256f)
                verticalLineToRelative(256f)
                horizontalLineToRelative(-256f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineWidth = 1f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(30.64f, 88.41f)
                verticalLineToRelative(68.71f)
                horizontalLineToRelative(17.76f)
                verticalLineToRelative(-41.91f)
                lineToRelative(15.47f, 19.77f)
                lineToRelative(16.68f, -19.77f)
                verticalLineToRelative(41.91f)
                horizontalLineTo(98.31f)
                verticalLineTo(88.41f)
                horizontalLineTo(80.55f)
                lineTo(63.87f, 109.82f)
                lineTo(48.4f, 88.41f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineWidth = 1f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(182.5f, 88.41f)
                verticalLineToRelative(68.71f)
                horizontalLineToRelative(39.08f)
                lineToRelative(3.78f, -14.66f)
                horizontalLineTo(200.26f)
                verticalLineTo(88.41f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                strokeLineWidth = 1f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(149.65f, 88.41f)
                curveToRelative(-21.64f, 0f, -35.07f, 10.21f, -39.37f, 25.39f)
                curveToRelative(-4.2f, 14.82f, 0.34f, 34.37f, 10.29f, 53.79f)
                lineToRelative(14.86f, -10.47f)
                curveToRelative(0f, 0f, -7.06f, -9.22f, -8.39f, -23.04f)
                horizontalLineToRelative(21.98f)
                verticalLineToRelative(23.04f)
                horizontalLineToRelative(19.73f)
                verticalLineToRelative(-51.68f)
                horizontalLineToRelative(-19.73f)
                verticalLineToRelative(14.97f)
                horizontalLineTo(130.8f)
                curveToRelative(1.72f, -11.2f, 8.3f, -17.31f, 15.47f, -17.31f)
                horizontalLineToRelative(25.82f)
                lineToRelative(-5.12f, -14.69f)
                close()
            }
        }.build()

        return _MyAnimeList!!
    }

@Suppress("ObjectPropertyName")
private var _MyAnimeList: ImageVector? = null

