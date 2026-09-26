package snd.komelia.ui.icons.mangabaka.flags

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import snd.komelia.ui.icons.AppIcons

val AppIcons.FlagPt: ImageVector
    get() {
        if (_FlagPt != null) {
            return _FlagPt!!
        }
        _FlagPt = ImageVector.Builder(
            name = "FlagPt",
            defaultWidth = 640.dp,
            defaultHeight = 480.dp,
            viewportWidth = 640f,
            viewportHeight = 480f
        ).apply {
            path(fill = SolidColor(Color.Red)) {
                moveTo(256f, 0f)
                horizontalLineToRelative(384f)
                verticalLineToRelative(480f)
                horizontalLineTo(256f)
                close()
            }
            path(fill = SolidColor(Color(0xFF006600))) {
                moveTo(0f, 0f)
                horizontalLineToRelative(256f)
                verticalLineToRelative(480f)
                horizontalLineTo(0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(339.5f, 306.2f)
                curveToRelative(-32.3f, -1f, -180f, -93.2f, -181f, -108f)
                lineToRelative(8.1f, -13.5f)
                curveToRelative(14.7f, 21.3f, 165.7f, 111f, 180.6f, 107.8f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(164.9f, 182.8f)
                curveToRelative(-2.9f, 7.8f, 38.6f, 33.4f, 88.4f, 63.8f)
                reflectiveCurveToRelative(92.9f, 49f, 96f, 46.4f)
                lineToRelative(1.5f, -2.8f)
                quadToRelative(-0.9f, 1.6f, -4.3f, 0.6f)
                curveToRelative(-13.5f, -3.9f, -48.6f, -20f, -92.1f, -46.4f)
                curveToRelative(-43.6f, -26.4f, -81.4f, -50.7f, -87.3f, -61f)
                arcToRelative(6f, 6f, 0f, isMoreThanHalf = false, isPositiveArc = true, -0.6f, -3.1f)
                horizontalLineToRelative(-0.2f)
                lineToRelative(-1.2f, 2.2f)
                close()
                moveTo(340.2f, 306.6f)
                quadToRelative(-0.7f, 1.3f, -3.5f, 0.8f)
                curveToRelative(-12f, -1.3f, -48.6f, -19.1f, -91.9f, -45f)
                curveToRelative(-50.4f, -30.2f, -92f, -57.6f, -87.4f, -64.8f)
                lineToRelative(1.2f, -2.2f)
                lineToRelative(0.2f, 0.1f)
                curveToRelative(-4f, 12.2f, 82.1f, 61.4f, 87.2f, 64.6f)
                curveToRelative(49.8f, 30.8f, 91.8f, 48.9f, 95.5f, 44.2f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(256.2f, 207.2f)
                curveToRelative(32.2f, -0.3f, 72f, -4.4f, 95f, -13.6f)
                lineToRelative(-5f, -8f)
                curveToRelative(-13.5f, 7.5f, -53.5f, 12.5f, -90.3f, 13.2f)
                curveToRelative(-43.4f, -0.4f, -74.1f, -4.5f, -89.5f, -14.8f)
                lineToRelative(-4.6f, 8.6f)
                curveToRelative(28.2f, 12f, 57.2f, 14.5f, 94.4f, 14.6f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(352.5f, 193.8f)
                curveToRelative(-0.8f, 1.3f, -15.8f, 6.4f, -37.8f, 10.2f)
                arcToRelative(381f, 381f, 0f, isMoreThanHalf = false, isPositiveArc = true, -58.6f, 4.3f)
                arcToRelative(416f, 416f, 0f, isMoreThanHalf = false, isPositiveArc = true, -56.2f, -3.6f)
                curveToRelative(-23.1f, -3.6f, -35f, -8.6f, -39.5f, -10.4f)
                lineToRelative(1.1f, -2.2f)
                curveToRelative(12.7f, 5f, 24.7f, 8f, 38.7f, 10.2f)
                arcTo(412f, 412f, 0f, isMoreThanHalf = false, isPositiveArc = false, 256f, 206f)
                arcToRelative(392f, 392f, 0f, isMoreThanHalf = false, isPositiveArc = false, 58.3f, -4.3f)
                curveToRelative(22.5f, -3.7f, 34.8f, -8.4f, 36.6f, -10.5f)
                close()
                moveTo(348.1f, 185.7f)
                curveToRelative(-2.4f, 2f, -14.6f, 6.3f, -36f, 9.7f)
                arcToRelative(388f, 388f, 0f, isMoreThanHalf = false, isPositiveArc = true, -55.8f, 4f)
                curveToRelative(-22f, 0f, -40.1f, -1.6f, -53.8f, -3.6f)
                curveToRelative(-21.8f, -2.8f, -33.4f, -8f, -37.6f, -9.4f)
                lineToRelative(1.3f, -2.2f)
                curveToRelative(3.3f, 1.7f, 14.4f, 6.2f, 36.5f, 9.3f)
                arcToRelative(385f, 385f, 0f, isMoreThanHalf = false, isPositiveArc = false, 53.6f, 3.4f)
                arcToRelative(384f, 384f, 0f, isMoreThanHalf = false, isPositiveArc = false, 55.4f, -4f)
                curveToRelative(21.5f, -3f, 33.1f, -8.4f, 34.9f, -9.8f)
                close()
                moveTo(150.3f, 246f)
                curveToRelative(19.8f, 10.7f, 63.9f, 16f, 105.6f, 16.4f)
                curveToRelative(38f, 0.1f, 87.4f, -5.8f, 105.9f, -15.6f)
                lineToRelative(-0.5f, -10.7f)
                curveToRelative(-5.8f, 9f, -58.8f, 17.7f, -105.8f, 17.4f)
                reflectiveCurveToRelative(-90.7f, -7.6f, -105.3f, -17f)
                verticalLineToRelative(9.5f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(362.8f, 244.5f)
                verticalLineToRelative(2.5f)
                curveToRelative(-2.8f, 3.4f, -20.2f, 8.4f, -42f, 12f)
                arcToRelative(434f, 434f, 0f, isMoreThanHalf = false, isPositiveArc = true, -65.4f, 4.4f)
                arcToRelative(400f, 400f, 0f, isMoreThanHalf = false, isPositiveArc = true, -62f, -4.3f)
                arcToRelative(155f, 155f, 0f, isMoreThanHalf = false, isPositiveArc = true, -44.4f, -12f)
                verticalLineToRelative(-2.9f)
                curveToRelative(9.7f, 6.4f, 35.9f, 11.2f, 44.7f, 12.6f)
                curveToRelative(15.8f, 2.4f, 36.1f, 4.2f, 61.7f, 4.2f)
                curveToRelative(26.9f, 0f, 48.4f, -1.9f, 65f, -4.4f)
                curveToRelative(15.7f, -2.3f, 38f, -8.2f, 42.4f, -12.1f)
                moveToRelative(0f, -9f)
                verticalLineToRelative(2.5f)
                curveToRelative(-2.8f, 3.3f, -20.2f, 8.3f, -42f, 11.9f)
                arcToRelative(434f, 434f, 0f, isMoreThanHalf = false, isPositiveArc = true, -65.4f, 4.5f)
                arcToRelative(414f, 414f, 0f, isMoreThanHalf = false, isPositiveArc = true, -62f, -4.3f)
                arcToRelative(155f, 155f, 0f, isMoreThanHalf = false, isPositiveArc = true, -44.4f, -12f)
                verticalLineToRelative(-3f)
                curveToRelative(9.7f, 6.5f, 36f, 11.2f, 44.7f, 12.6f)
                arcToRelative(408f, 408f, 0f, isMoreThanHalf = false, isPositiveArc = false, 61.7f, 4.3f)
                curveToRelative(26.9f, 0f, 48.5f, -2f, 65f, -4.5f)
                curveToRelative(15.7f, -2.2f, 38f, -8.1f, 42.4f, -12f)
                moveToRelative(-107f, 68.8f)
                curveToRelative(-45.6f, -0.2f, -84.7f, -12.4f, -93f, -14.4f)
                lineToRelative(6f, 9.4f)
                arcToRelative(250f, 250f, 0f, isMoreThanHalf = false, isPositiveArc = false, 87.4f, 14.3f)
                curveToRelative(34.7f, -1f, 65f, -3.7f, 86.3f, -14.1f)
                lineToRelative(6.2f, -9.8f)
                curveToRelative(-14.5f, 6.9f, -64f, 14.6f, -93f, 14.6f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveToRelative(344.9f, 297.3f)
                lineToRelative(-2.8f, 4f)
                curveToRelative(-10f, 3.6f, -26f, 7.4f, -32.6f, 8.4f)
                arcToRelative(296f, 296f, 0f, isMoreThanHalf = false, isPositiveArc = true, -53.7f, 5f)
                curveToRelative(-40.4f, -0.6f, -73.5f, -8.5f, -89f, -15.3f)
                lineToRelative(-1.3f, -2.1f)
                lineToRelative(0.2f, -0.4f)
                lineToRelative(2.1f, 0.9f)
                arcToRelative(287f, 287f, 0f, isMoreThanHalf = false, isPositiveArc = false, 88.2f, 14.5f)
                curveToRelative(18.8f, 0f, 37.5f, -2.1f, 52.6f, -4.8f)
                curveToRelative(23.2f, -4.7f, 32.6f, -8.2f, 35.5f, -9.8f)
                lineToRelative(0.7f, -0.4f)
                close()
                moveTo(350.2f, 288.5f)
                lineTo(348.2f, 292f)
                curveToRelative(-5.4f, 2f, -20f, 6.2f, -41.3f, 9.2f)
                curveToRelative(-14f, 1.9f, -22.7f, 3.8f, -50.6f, 4.3f)
                arcToRelative(347f, 347f, 0f, isMoreThanHalf = false, isPositiveArc = true, -94.2f, -14f)
                lineTo(161f, 289f)
                arcToRelative(390f, 390f, 0f, isMoreThanHalf = false, isPositiveArc = false, 95.4f, 14f)
                curveToRelative(25.5f, -0.5f, 36.4f, -2.4f, 50.3f, -4.3f)
                curveToRelative(24.8f, -3.8f, 37.3f, -8f, 41f, -9.1f)
                verticalLineToRelative(-0.2f)
                lineToRelative(2.6f, -1f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(350.8f, 237.6f)
                curveToRelative(0.1f, 30f, -15.3f, 57f, -27.6f, 68.8f)
                arcToRelative(99f, 99f, 0f, isMoreThanHalf = false, isPositiveArc = true, -67.8f, 28.2f)
                curveToRelative(-30.3f, 0.5f, -58.8f, -19.2f, -66.5f, -27.9f)
                arcToRelative(101f, 101f, 0f, isMoreThanHalf = false, isPositiveArc = true, -27.5f, -67.4f)
                curveToRelative(1.8f, -32.8f, 14.7f, -55.6f, 33.3f, -71.3f)
                arcToRelative(100f, 100f, 0f, isMoreThanHalf = false, isPositiveArc = true, 64.2f, -22.7f)
                arcToRelative(98f, 98f, 0f, isMoreThanHalf = false, isPositiveArc = true, 71f, 35.6f)
                curveToRelative(12.5f, 15.2f, 18f, 31.7f, 20.9f, 56.7f)
                moveTo(255.6f, 135f)
                arcToRelative(106f, 106f, 0f, isMoreThanHalf = false, isPositiveArc = true, 106f, 105.2f)
                arcToRelative(105.6f, 105.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -211.4f, 0f)
                curveToRelative(-0.1f, -58f, 47.3f, -105.2f, 105.4f, -105.2f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(255.9f, 134.5f)
                curveToRelative(58.2f, 0f, 105.6f, 47.4f, 105.6f, 105.6f)
                reflectiveCurveTo(314.1f, 345.7f, 256f, 345.7f)
                reflectiveCurveToRelative(-105.6f, -47.4f, -105.6f, -105.6f)
                reflectiveCurveTo(197.8f, 134.5f, 256f, 134.5f)
                close()
                moveTo(152.6f, 240f)
                curveToRelative(0f, 56.8f, 46.7f, 103.3f, 103.3f, 103.3f)
                reflectiveCurveTo(359.2f, 296.8f, 359.2f, 240f)
                reflectiveCurveToRelative(-46.7f, -103.3f, -103.3f, -103.3f)
                reflectiveCurveTo(152.6f, 183.2f, 152.6f, 240f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(256f, 143.3f)
                arcToRelative(97f, 97f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96.7f, 96.7f)
                arcToRelative(97f, 97f, 0f, isMoreThanHalf = false, isPositiveArc = true, -96.7f, 96.8f)
                curveToRelative(-53f, 0f, -96.7f, -43.6f, -96.7f, -96.8f)
                arcToRelative(97f, 97f, 0f, isMoreThanHalf = false, isPositiveArc = true, 96.7f, -96.7f)
                moveTo(161.6f, 240f)
                curveToRelative(0f, 52f, 42.6f, 94.4f, 94.4f, 94.4f)
                reflectiveCurveToRelative(94.4f, -42.5f, 94.4f, -94.4f)
                reflectiveCurveToRelative(-42.6f, -94.4f, -94.4f, -94.4f)
                arcToRelative(95f, 95f, 0f, isMoreThanHalf = false, isPositiveArc = false, -94.4f, 94.4f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(260.3f, 134f)
                horizontalLineToRelative(-9.1f)
                verticalLineToRelative(212.3f)
                horizontalLineToRelative(9f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(259.3f, 132.8f)
                horizontalLineToRelative(2.3f)
                verticalLineToRelative(214.7f)
                horizontalLineToRelative(-2.2f)
                lineTo(259.4f, 132.8f)
                close()
                moveTo(250.3f, 132.8f)
                horizontalLineToRelative(2.4f)
                verticalLineToRelative(214.7f)
                horizontalLineToRelative(-2.3f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(361.6f, 244.2f)
                verticalLineToRelative(-7.8f)
                lineToRelative(-6.4f, -6f)
                lineToRelative(-36.3f, -9.6f)
                lineToRelative(-52.2f, -5.3f)
                lineToRelative(-63f, 3.2f)
                lineToRelative(-44.8f, 10.6f)
                lineToRelative(-9f, 6.7f)
                verticalLineToRelative(7.9f)
                lineToRelative(22.9f, -10.3f)
                lineToRelative(54.4f, -8.5f)
                horizontalLineToRelative(52.3f)
                lineToRelative(38.4f, 4.2f)
                lineToRelative(26.6f, 6.4f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(256f, 223.8f)
                curveToRelative(24.9f, 0f, 49f, 2.3f, 68.3f, 6f)
                curveToRelative(19.8f, 4f, 33.7f, 9f, 38.5f, 14.5f)
                verticalLineToRelative(2.8f)
                curveToRelative(-5.8f, -7f, -24.5f, -12f, -39f, -15f)
                curveToRelative(-19f, -3.6f, -43f, -6f, -67.9f, -6f)
                curveToRelative(-26.1f, 0f, -50.5f, 2.6f, -69.3f, 6.2f)
                curveToRelative(-15f, 3f, -35.1f, 9f, -37.6f, 14.8f)
                verticalLineToRelative(-2.9f)
                curveToRelative(1.3f, -4f, 16.3f, -10f, 37.3f, -14.3f)
                curveToRelative(18.9f, -3.7f, 43.3f, -6.1f, 69.6f, -6.1f)
                close()
                moveTo(256f, 214.7f)
                arcToRelative(383f, 383f, 0f, isMoreThanHalf = false, isPositiveArc = true, 68.3f, 6f)
                curveToRelative(19.8f, 4f, 33.7f, 9f, 38.5f, 14.6f)
                verticalLineToRelative(2.7f)
                curveToRelative(-5.8f, -6.9f, -24.5f, -12f, -39f, -14.9f)
                curveToRelative(-19f, -3.7f, -43f, -6f, -67.9f, -6f)
                arcToRelative(376f, 376f, 0f, isMoreThanHalf = false, isPositiveArc = false, -69.2f, 6.2f)
                curveToRelative(-14.5f, 2.7f, -35.4f, 8.9f, -37.7f, 14.7f)
                verticalLineToRelative(-2.8f)
                curveToRelative(1.4f, -4f, 16.6f, -10.3f, 37.3f, -14.3f)
                curveToRelative(19f, -3.7f, 43.3f, -6.2f, 69.7f, -6.2f)
                moveToRelative(-0.6f, -46.2f)
                curveToRelative(39.3f, -0.2f, 73.6f, 5.5f, 89.3f, 13.5f)
                lineToRelative(5.7f, 10f)
                curveToRelative(-13.6f, -7.4f, -50.6f, -15f, -94.9f, -14f)
                curveToRelative(-36.1f, 0.3f, -74.7f, 4f, -94f, 14.4f)
                lineToRelative(6.8f, -11.4f)
                curveToRelative(15.9f, -8.3f, 53.3f, -12.5f, 87.1f, -12.5f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(256f, 176.7f)
                arcToRelative(354f, 354f, 0f, isMoreThanHalf = false, isPositiveArc = true, 61.3f, 4.3f)
                curveToRelative(16f, 3f, 31.3f, 7.4f, 33.5f, 9.8f)
                lineToRelative(1.7f, 3f)
                curveToRelative(-5.3f, -3.4f, -18.6f, -7.3f, -35.6f, -10.5f)
                reflectiveCurveToRelative(-38.7f, -4.3f, -61f, -4.2f)
                curveToRelative(-25.3f, -0.1f, -45f, 1.2f, -61.8f, 4.2f)
                arcToRelative(109f, 109f, 0f, isMoreThanHalf = false, isPositiveArc = false, -33.3f, 10.3f)
                lineToRelative(1.7f, -3.1f)
                curveToRelative(6f, -3f, 15.3f, -6.7f, 31.1f, -9.6f)
                curveToRelative(17.5f, -3.2f, 37.4f, -4.1f, 62.4f, -4.2f)
                moveToRelative(0f, -9f)
                curveToRelative(21.4f, -0.2f, 42.6f, 1f, 59.1f, 4f)
                arcToRelative(96f, 96f, 0f, isMoreThanHalf = false, isPositiveArc = true, 30.6f, 10f)
                lineToRelative(2.5f, 4f)
                curveToRelative(-4.2f, -4.7f, -20f, -9.2f, -34.1f, -11.6f)
                curveToRelative(-16.4f, -2.9f, -36.7f, -4f, -58.1f, -4.2f)
                arcToRelative(361f, 361f, 0f, isMoreThanHalf = false, isPositiveArc = false, -59.5f, 4.4f)
                arcToRelative(97f, 97f, 0f, isMoreThanHalf = false, isPositiveArc = false, -29.6f, 9.1f)
                lineToRelative(2.2f, -3.3f)
                curveToRelative(5.8f, -3f, 15.2f, -5.8f, 27f, -8.1f)
                arcToRelative(357f, 357f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.9f, -4.4f)
                close()
                moveTo(308.4f, 284f)
                arcToRelative(276f, 276f, 0f, isMoreThanHalf = false, isPositiveArc = false, -52.5f, -4f)
                curveToRelative(-65.5f, 0.8f, -86.6f, 13.5f, -89.2f, 17.3f)
                lineToRelative(-5f, -8f)
                curveToRelative(16.8f, -12f, 52.4f, -18.8f, 94.6f, -18.2f)
                quadToRelative(32.9f, 0.5f, 56.6f, 5f)
                lineToRelative(-4.5f, 8f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(255.6f, 278.9f)
                curveToRelative(18.2f, 0.3f, 36f, 1f, 53.3f, 4.2f)
                lineToRelative(-1.2f, 2.2f)
                curveToRelative(-16f, -3f, -33.2f, -4f, -52f, -4f)
                curveToRelative(-24.3f, -0.2f, -48.7f, 2.1f, -70f, 8.2f)
                curveToRelative(-6.7f, 1.9f, -17.8f, 6.2f, -19f, 9.8f)
                lineToRelative(-1.2f, -2f)
                curveToRelative(0.4f, -2.2f, 7f, -6.6f, 19.6f, -10f)
                curveToRelative(24.4f, -7f, 47.2f, -8.3f, 70.5f, -8.4f)
                moveToRelative(0.8f, -9.2f)
                arcToRelative(327f, 327f, 0f, isMoreThanHalf = false, isPositiveArc = true, 57.3f, 5f)
                lineToRelative(-1.3f, 2.3f)
                arcToRelative(299f, 299f, 0f, isMoreThanHalf = false, isPositiveArc = false, -56f, -4.9f)
                curveToRelative(-24.2f, 0f, -49.9f, 1.8f, -73.3f, 8.6f)
                curveToRelative(-7.5f, 2.2f, -20.6f, 7f, -21f, 10.7f)
                lineToRelative(-1.2f, -2.2f)
                curveToRelative(0.2f, -3.4f, 11.5f, -7.9f, 21.7f, -10.8f)
                curveToRelative(23.5f, -6.9f, 49.3f, -8.6f, 73.8f, -8.7f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveToRelative(349.4f, 290.5f)
                lineToRelative(-7.8f, 12.3f)
                lineToRelative(-22.7f, -20.1f)
                lineToRelative(-58.6f, -39.5f)
                lineToRelative(-66.2f, -36.3f)
                lineToRelative(-34.3f, -11.7f)
                lineToRelative(7.3f, -13.6f)
                lineToRelative(2.5f, -1.3f)
                lineToRelative(21.3f, 5.3f)
                lineToRelative(70.4f, 36.3f)
                lineToRelative(40.6f, 25.6f)
                lineTo(336f, 272f)
                lineToRelative(13.9f, 16f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(158.6f, 195.5f)
                curveToRelative(6f, -4f, 50.2f, 15.6f, 96.6f, 43.6f)
                curveToRelative(46.1f, 28f, 90.3f, 59.6f, 86.3f, 65.5f)
                lineToRelative(-1.3f, 2.1f)
                lineToRelative(-0.6f, 0.5f)
                curveToRelative(0.1f, -0.1f, 0.8f, -1f, 0f, -3.1f)
                curveToRelative(-2f, -6.5f, -33.4f, -31.5f, -85.3f, -62.9f)
                curveToRelative(-50.7f, -30.1f, -92.9f, -48.3f, -97f, -43.1f)
                close()
                moveTo(351f, 290.4f)
                curveToRelative(3.8f, -7.6f, -37.2f, -38.5f, -88.1f, -68.6f)
                curveToRelative(-52f, -29.5f, -89.6f, -46.9f, -96.5f, -41.7f)
                lineTo(165f, 183f)
                lineToRelative(0.4f, -0.5f)
                curveToRelative(1.2f, -1f, 3.3f, -1f, 4.2f, -1f)
                curveToRelative(11.8f, 0.2f, 45.5f, 15.7f, 92.8f, 42.8f)
                curveToRelative(20.8f, 12f, 87.6f, 55f, 87.3f, 67f)
                curveToRelative(0f, 1f, 0.1f, 1.2f, -0.3f, 1.8f)
                lineToRelative(1.7f, -2.6f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.75f
            ) {
                moveTo(192.64f, 251.77f)
                arcToRelative(62.93f, 62.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.67f, 44.48f)
                arcToRelative(62.93f, 62.93f, 99.4f, isMoreThanHalf = false, isPositiveArc = false, 44.59f, 18.77f)
                arcToRelative(62.93f, 62.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 44.8f, -18.56f)
                arcToRelative(62.93f, 62.93f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18.56f, -44.59f)
                lineToRelative(0f, -84.48f)
                lineToRelative(-126.61f, -0.21f)
                close()
            }
            path(
                fill = SolidColor(Color.Red),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(195.2f, 251.87f)
                arcToRelative(59.73f, 59.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.92f, 42.67f)
                arcToRelative(60.8f, 60.8f, 119.49f, isMoreThanHalf = false, isPositiveArc = false, 42.88f, 17.92f)
                arcToRelative(60.8f, 60.8f, 119.77f, isMoreThanHalf = false, isPositiveArc = false, 42.88f, -17.71f)
                arcToRelative(59.73f, 59.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, 17.81f, -42.67f)
                lineToRelative(0f, -82.13f)
                lineTo(195.2f, 169.95f)
                lineToRelative(0f, 81.92f)
                moveToRelative(97.07f, -57.28f)
                lineToRelative(0f, 52.16f)
                lineToRelative(-0.11f, 5.44f)
                arcToRelative(35.2f, 35.2f, 115.28f, isMoreThanHalf = false, isPositiveArc = true, -10.67f, 25.6f)
                arcToRelative(36.27f, 36.27f, 61.46f, isMoreThanHalf = false, isPositiveArc = true, -25.6f, 10.67f)
                curveToRelative(-10.03f, 0f, -18.88f, -4.27f, -25.49f, -10.88f)
                arcToRelative(36.27f, 36.27f, 85.13f, isMoreThanHalf = false, isPositiveArc = true, -10.67f, -25.6f)
                lineToRelative(0f, -57.6f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                strokeLineWidth = 0.53f
            ) {
                moveTo(202.88f, 191.39f)
                curveToRelative(0.11f, -5.87f, 4.27f, -7.25f, 4.27f, -7.25f)
                curveToRelative(0.11f, 0f, 4.59f, 1.49f, 4.59f, 7.36f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveToRelative(199.25f, 184.25f)
                lineToRelative(-0.75f, 6.72f)
                lineToRelative(4.48f, 0f)
                curveToRelative(0f, -5.55f, 4.27f, -6.4f, 4.27f, -6.4f)
                curveToRelative(0.11f, 0f, 4.27f, 1.17f, 4.37f, 6.4f)
                lineToRelative(4.48f, 0f)
                lineToRelative(-0.85f, -6.83f)
                close()
                moveTo(198.19f, 191.07f)
                lineToRelative(18.13f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, 0.75f)
                quadToRelative(0f, 0.85f, -0.64f, 0.85f)
                lineToRelative(-18.13f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.85f)
                quadToRelative(0f, -0.75f, 0.75f, -0.75f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(204.8f, 190.97f)
                curveToRelative(0f, -3.52f, 2.45f, -4.48f, 2.45f, -4.48f)
                reflectiveCurveToRelative(2.45f, 1.07f, 2.45f, 4.48f)
                lineTo(204.8f, 190.97f)
                moveToRelative(-6.19f, -9.6f)
                lineToRelative(17.39f, 0f)
                quadToRelative(0.53f, 0.11f, 0.64f, 0.85f)
                quadToRelative(0f, 0.53f, -0.64f, 0.64f)
                lineToRelative(-17.39f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.75f)
                quadToRelative(0f, -0.53f, 0.64f, -0.64f)
                close()
                moveTo(199.04f, 182.97f)
                lineTo(215.47f, 182.97f)
                quadToRelative(0.53f, 0f, 0.64f, 0.75f)
                reflectiveQuadToRelative(-0.64f, 0.75f)
                lineToRelative(-16.53f, 0f)
                quadToRelative(-0.64f, 0f, -0.64f, -0.75f)
                reflectiveQuadToRelative(0.64f, -0.75f)
                close()
                moveTo(204.37f, 171.66f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 0.85f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -0.85f)
                lineToRelative(1.39f, 0f)
                lineToRelative(0f, 0.96f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.69f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.53f)
                close()
                moveTo(209.28f, 174.54f)
                lineTo(209.6f, 181.37f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(0.32f, -6.93f)
                lineToRelative(3.95f, 0f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(203.73f, 177.74f)
                lineToRelative(0f, 3.63f)
                lineToRelative(-4.27f, 0f)
                lineToRelative(0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(215.04f, 177.74f)
                lineToRelative(0f, 3.63f)
                lineToRelative(-4.27f, 0f)
                lineToRelative(0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(198.72f, 174.97f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(-0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(210.03f, 174.97f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(-0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(206.83f, 176.67f)
                curveToRelative(0f, -0.64f, 0.96f, -0.64f, 0.96f, 0f)
                lineToRelative(0f, 1.71f)
                lineToRelative(-0.96f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(201.17f, 179.02f)
                curveToRelative(0f, -0.64f, 0.85f, -0.64f, 0.85f, 0f)
                lineToRelative(0f, 1.28f)
                lineToRelative(-0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(212.48f, 179.02f)
                curveToRelative(0f, -0.64f, 0.85f, -0.64f, 0.85f, 0f)
                lineToRelative(0f, 1.28f)
                lineToRelative(-0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                strokeLineWidth = 0.53f
            ) {
                moveTo(202.88f, 240.78f)
                curveToRelative(0.11f, -5.87f, 4.27f, -7.25f, 4.27f, -7.25f)
                curveToRelative(0.11f, 0f, 4.59f, 1.49f, 4.59f, 7.36f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveToRelative(199.25f, 233.63f)
                lineToRelative(-0.75f, 6.72f)
                lineToRelative(4.48f, 0f)
                curveToRelative(0f, -5.55f, 4.27f, -6.4f, 4.27f, -6.4f)
                curveToRelative(0.11f, 0f, 4.27f, 1.17f, 4.37f, 6.4f)
                lineToRelative(4.48f, 0f)
                lineToRelative(-0.85f, -6.83f)
                close()
                moveTo(198.19f, 240.46f)
                lineToRelative(18.13f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, 0.75f)
                quadToRelative(0f, 0.85f, -0.64f, 0.85f)
                lineToRelative(-18.13f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.85f)
                quadToRelative(0f, -0.75f, 0.75f, -0.75f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(204.8f, 240.35f)
                curveToRelative(0f, -3.52f, 2.45f, -4.48f, 2.45f, -4.48f)
                reflectiveCurveToRelative(2.45f, 1.07f, 2.45f, 4.48f)
                lineTo(204.8f, 240.35f)
                moveToRelative(-6.19f, -9.6f)
                lineToRelative(17.39f, 0f)
                quadToRelative(0.53f, 0.11f, 0.64f, 0.85f)
                quadToRelative(0f, 0.53f, -0.64f, 0.64f)
                lineToRelative(-17.39f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.75f)
                quadToRelative(0f, -0.53f, 0.64f, -0.64f)
                close()
                moveTo(199.04f, 232.35f)
                lineTo(215.47f, 232.35f)
                quadToRelative(0.53f, 0f, 0.64f, 0.75f)
                reflectiveQuadToRelative(-0.64f, 0.75f)
                lineToRelative(-16.53f, 0f)
                quadToRelative(-0.64f, 0f, -0.64f, -0.75f)
                reflectiveQuadToRelative(0.64f, -0.75f)
                close()
                moveTo(204.37f, 221.05f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 0.85f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -0.85f)
                lineToRelative(1.39f, 0f)
                lineToRelative(0f, 0.96f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.69f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.53f)
                close()
                moveTo(209.28f, 223.93f)
                lineTo(209.6f, 230.75f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(0.32f, -6.93f)
                lineToRelative(3.95f, 0f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(203.73f, 227.13f)
                lineToRelative(0f, 3.63f)
                lineToRelative(-4.27f, 0f)
                lineToRelative(0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(215.04f, 227.13f)
                lineToRelative(0f, 3.63f)
                lineToRelative(-4.27f, 0f)
                lineToRelative(0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(198.72f, 224.35f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(-0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(210.03f, 224.35f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(-0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(206.83f, 226.06f)
                curveToRelative(0f, -0.64f, 0.96f, -0.64f, 0.96f, 0f)
                lineToRelative(0f, 1.71f)
                lineToRelative(-0.96f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(201.17f, 228.41f)
                curveToRelative(0f, -0.64f, 0.85f, -0.64f, 0.85f, 0f)
                lineToRelative(0f, 1.28f)
                lineToRelative(-0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(212.48f, 228.41f)
                curveToRelative(0f, -0.64f, 0.85f, -0.64f, 0.85f, 0f)
                lineToRelative(0f, 1.28f)
                lineToRelative(-0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                strokeLineWidth = 0.53f
            ) {
                moveTo(222.13f, 292.25f)
                curveToRelative(-4.09f, -4.21f, -2.14f, -8.14f, -2.14f, -8.14f)
                curveToRelative(0.08f, -0.08f, 4.29f, -2.2f, 8.45f, 1.93f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveToRelative(214.5f, 289.79f)
                lineToRelative(4.24f, 5.26f)
                lineToRelative(3.16f, -3.18f)
                curveToRelative(-3.94f, -3.91f, -1.53f, -7.54f, -1.53f, -7.54f)
                curveToRelative(0.08f, -0.08f, 3.84f, -2.2f, 7.62f, 1.41f)
                lineToRelative(3.16f, -3.18f)
                lineToRelative(-5.45f, -4.2f)
                close()
                moveTo(218.6f, 295.36f)
                lineToRelative(12.78f, -12.87f)
                quadToRelative(0.38f, -0.38f, 0.98f, 0.07f)
                quadToRelative(0.61f, 0.6f, 0.15f, 1.06f)
                lineToRelative(-12.78f, 12.87f)
                quadToRelative(-0.38f, 0.38f, -1.06f, -0.15f)
                quadToRelative(-0.53f, -0.53f, -0f, -1.06f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(223.18f, 290.59f)
                curveToRelative(-2.5f, -2.48f, -1.45f, -4.9f, -1.45f, -4.9f)
                reflectiveCurveToRelative(2.49f, -0.99f, 4.91f, 1.42f)
                lineTo(223.18f, 290.59f)
                moveToRelative(-11.17f, -2.37f)
                lineToRelative(12.25f, -12.34f)
                quadToRelative(0.45f, -0.3f, 1.06f, 0.15f)
                quadToRelative(0.38f, 0.38f, 0f, 0.91f)
                lineToRelative(-12.25f, 12.34f)
                quadToRelative(-0.38f, 0.38f, -0.98f, -0.07f)
                quadToRelative(-0.38f, -0.38f, -0f, -0.91f)
                close()
                moveTo(213.45f, 289.04f)
                lineTo(225.02f, 277.38f)
                quadToRelative(0.38f, -0.38f, 0.98f, 0.07f)
                reflectiveQuadToRelative(0.08f, 0.98f)
                lineToRelative(-11.65f, 11.73f)
                quadToRelative(-0.45f, 0.45f, -0.98f, -0.07f)
                reflectiveQuadToRelative(-0.08f, -0.98f)
                close()
                moveTo(209.18f, 277.29f)
                lineToRelative(0.9f, -0.91f)
                lineToRelative(0.61f, 0.6f)
                lineToRelative(0.68f, -0.68f)
                lineToRelative(-0.61f, -0.6f)
                lineToRelative(0.98f, -0.98f)
                lineToRelative(0.68f, 0.68f)
                lineToRelative(0.68f, -0.68f)
                lineToRelative(-0.76f, -0.75f)
                lineToRelative(0.9f, -0.91f)
                lineToRelative(1.51f, 1.5f)
                quadToRelative(0.45f, 0.45f, 0.08f, 0.83f)
                lineToRelative(-3.31f, 3.33f)
                quadToRelative(-0.38f, 0.38f, -0.83f, 0.08f)
                close()
                moveTo(214.68f, 275.84f)
                lineTo(219.75f, 280.42f)
                lineToRelative(-3.23f, 3.25f)
                lineToRelative(-4.69f, -5.11f)
                lineToRelative(2.78f, -2.8f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(213.04f, 282.03f)
                lineToRelative(2.57f, 2.56f)
                lineToRelative(-3.01f, 3.03f)
                lineToRelative(-2.57f, -2.56f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(221.01f, 274f)
                lineToRelative(2.57f, 2.56f)
                lineToRelative(-3.01f, 3.03f)
                lineToRelative(-2.57f, -2.56f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(207.54f, 283.63f)
                lineToRelative(0.9f, -0.91f)
                lineToRelative(0.76f, 0.75f)
                lineToRelative(0.68f, -0.68f)
                lineToRelative(-0.76f, -0.75f)
                lineToRelative(0.9f, -0.91f)
                lineToRelative(0.76f, 0.75f)
                lineToRelative(0.68f, -0.68f)
                lineToRelative(-0.76f, -0.75f)
                lineToRelative(0.9f, -0.91f)
                lineToRelative(1.51f, 1.5f)
                quadToRelative(0.45f, 0.45f, 0.08f, 0.83f)
                lineToRelative(-3.23f, 3.25f)
                lineToRelative(-0.91f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(215.51f, 275.61f)
                lineToRelative(0.9f, -0.91f)
                lineToRelative(0.76f, 0.75f)
                lineToRelative(0.68f, -0.68f)
                lineToRelative(-0.76f, -0.75f)
                lineToRelative(0.9f, -0.91f)
                lineToRelative(0.76f, 0.75f)
                lineToRelative(0.68f, -0.68f)
                lineToRelative(-0.76f, -0.75f)
                lineToRelative(0.9f, -0.91f)
                lineToRelative(1.51f, 1.5f)
                quadToRelative(0.45f, 0.45f, 0.08f, 0.83f)
                lineToRelative(-3.23f, 3.25f)
                lineToRelative(-0.91f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(214.47f, 279.08f)
                curveToRelative(-0.45f, -0.45f, 0.22f, -1.13f, 0.68f, -0.68f)
                lineToRelative(1.21f, 1.2f)
                lineToRelative(-0.68f, 0.68f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(212.15f, 284.74f)
                curveToRelative(-0.45f, -0.45f, 0.15f, -1.06f, 0.6f, -0.61f)
                lineToRelative(0.91f, 0.9f)
                lineToRelative(-0.6f, 0.61f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(220.12f, 276.72f)
                curveToRelative(-0.45f, -0.45f, 0.15f, -1.06f, 0.6f, -0.61f)
                lineToRelative(0.91f, 0.9f)
                lineToRelative(-0.6f, 0.61f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                strokeLineWidth = 0.53f
            ) {
                moveTo(251.63f, 191.39f)
                curveToRelative(0.11f, -5.87f, 4.27f, -7.25f, 4.27f, -7.25f)
                curveToRelative(0.11f, 0f, 4.59f, 1.49f, 4.59f, 7.36f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveToRelative(248f, 184.25f)
                lineToRelative(-0.75f, 6.72f)
                lineToRelative(4.48f, 0f)
                curveToRelative(0f, -5.55f, 4.27f, -6.4f, 4.27f, -6.4f)
                curveToRelative(0.11f, 0f, 4.27f, 1.17f, 4.37f, 6.4f)
                lineToRelative(4.48f, 0f)
                lineToRelative(-0.85f, -6.83f)
                close()
                moveTo(246.93f, 191.07f)
                lineToRelative(18.13f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, 0.75f)
                quadToRelative(0f, 0.85f, -0.64f, 0.85f)
                lineToRelative(-18.13f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.85f)
                quadToRelative(0f, -0.75f, 0.75f, -0.75f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(253.55f, 190.97f)
                curveToRelative(0f, -3.52f, 2.45f, -4.48f, 2.45f, -4.48f)
                reflectiveCurveToRelative(2.45f, 1.07f, 2.45f, 4.48f)
                lineTo(253.55f, 190.97f)
                moveToRelative(-6.19f, -9.6f)
                lineToRelative(17.39f, 0f)
                quadToRelative(0.53f, 0.11f, 0.64f, 0.85f)
                quadToRelative(0f, 0.53f, -0.64f, 0.64f)
                lineToRelative(-17.39f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.75f)
                quadToRelative(0f, -0.53f, 0.64f, -0.64f)
                close()
                moveTo(247.79f, 182.97f)
                lineTo(264.21f, 182.97f)
                quadToRelative(0.53f, 0f, 0.64f, 0.75f)
                reflectiveQuadToRelative(-0.64f, 0.75f)
                lineToRelative(-16.53f, 0f)
                quadToRelative(-0.64f, 0f, -0.64f, -0.75f)
                reflectiveQuadToRelative(0.64f, -0.75f)
                close()
                moveTo(253.12f, 171.66f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 0.85f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -0.85f)
                lineToRelative(1.39f, 0f)
                lineToRelative(0f, 0.96f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.69f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, -0.53f)
                close()
                moveTo(258.03f, 174.54f)
                lineTo(258.35f, 181.37f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(0.32f, -6.93f)
                lineToRelative(3.95f, 0f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(252.48f, 177.74f)
                lineToRelative(0f, 3.63f)
                lineToRelative(-4.27f, 0f)
                lineToRelative(0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(263.79f, 177.74f)
                lineToRelative(0f, 3.63f)
                lineToRelative(-4.27f, 0f)
                lineToRelative(0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(247.47f, 174.97f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(-0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(258.77f, 174.97f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 1.07f)
                lineToRelative(0.96f, 0f)
                lineToRelative(0f, -1.07f)
                lineToRelative(1.28f, 0f)
                lineToRelative(0f, 2.13f)
                quadToRelative(0f, 0.64f, -0.53f, 0.64f)
                lineToRelative(-4.59f, 0f)
                lineToRelative(-0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(255.57f, 176.67f)
                curveToRelative(0f, -0.64f, 0.96f, -0.64f, 0.96f, 0f)
                lineToRelative(0f, 1.71f)
                lineToRelative(-0.96f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(249.92f, 179.02f)
                curveToRelative(0f, -0.64f, 0.85f, -0.64f, 0.85f, 0f)
                lineToRelative(0f, 1.28f)
                lineToRelative(-0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(261.23f, 179.02f)
                curveToRelative(0f, -0.64f, 0.85f, -0.64f, 0.85f, 0f)
                lineToRelative(0f, 1.28f)
                lineToRelative(-0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                strokeLineWidth = 0.53f
            ) {
                moveTo(308.91f, 191.39f)
                curveToRelative(-0.11f, -5.87f, -4.27f, -7.25f, -4.27f, -7.25f)
                curveToRelative(-0.11f, 0f, -4.59f, 1.49f, -4.59f, 7.36f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveToRelative(312.53f, 184.25f)
                lineToRelative(0.75f, 6.72f)
                lineToRelative(-4.48f, 0f)
                curveToRelative(-0f, -5.55f, -4.27f, -6.4f, -4.27f, -6.4f)
                curveToRelative(-0.11f, 0f, -4.27f, 1.17f, -4.37f, 6.4f)
                lineToRelative(-4.48f, 0f)
                lineToRelative(0.85f, -6.83f)
                close()
                moveTo(313.6f, 191.07f)
                lineToRelative(-18.13f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, 0.75f)
                quadToRelative(-0f, 0.85f, 0.64f, 0.85f)
                lineToRelative(18.13f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, -0.85f)
                quadToRelative(-0f, -0.75f, -0.75f, -0.75f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(306.99f, 190.97f)
                curveToRelative(-0f, -3.52f, -2.45f, -4.48f, -2.45f, -4.48f)
                reflectiveCurveToRelative(-2.45f, 1.07f, -2.45f, 4.48f)
                lineTo(306.99f, 190.97f)
                moveToRelative(6.19f, -9.6f)
                lineToRelative(-17.39f, 0f)
                quadToRelative(-0.53f, 0.11f, -0.64f, 0.85f)
                quadToRelative(-0f, 0.53f, 0.64f, 0.64f)
                lineToRelative(17.39f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, -0.75f)
                quadToRelative(-0f, -0.53f, -0.64f, -0.64f)
                close()
                moveTo(312.75f, 182.97f)
                lineTo(296.32f, 182.97f)
                quadToRelative(-0.53f, 0f, -0.64f, 0.75f)
                reflectiveQuadToRelative(0.64f, 0.75f)
                lineToRelative(16.53f, 0f)
                quadToRelative(0.64f, 0f, 0.64f, -0.75f)
                reflectiveQuadToRelative(-0.64f, -0.75f)
                close()
                moveTo(307.41f, 171.66f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 0.85f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -0.85f)
                lineToRelative(-1.39f, 0f)
                lineToRelative(-0f, 0.96f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 2.13f)
                quadToRelative(-0f, 0.64f, 0.53f, 0.64f)
                lineToRelative(4.69f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, -0.53f)
                close()
                moveTo(302.51f, 174.54f)
                lineTo(302.19f, 181.37f)
                lineToRelative(4.59f, 0f)
                lineToRelative(-0.32f, -6.93f)
                lineToRelative(-3.95f, 0f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(308.05f, 177.74f)
                lineToRelative(-0f, 3.63f)
                lineToRelative(4.27f, 0f)
                lineToRelative(-0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(296.75f, 177.74f)
                lineToRelative(-0f, 3.63f)
                lineToRelative(4.27f, 0f)
                lineToRelative(-0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(313.07f, 174.97f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 1.07f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 1.07f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 2.13f)
                quadToRelative(-0f, 0.64f, 0.53f, 0.64f)
                lineToRelative(4.59f, 0f)
                lineToRelative(0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(301.76f, 174.97f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 1.07f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 1.07f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 2.13f)
                quadToRelative(-0f, 0.64f, 0.53f, 0.64f)
                lineToRelative(4.59f, 0f)
                lineToRelative(0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(304.96f, 176.67f)
                curveToRelative(-0f, -0.64f, -0.96f, -0.64f, -0.96f, 0f)
                lineToRelative(-0f, 1.71f)
                lineToRelative(0.96f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(310.61f, 179.02f)
                curveToRelative(-0f, -0.64f, -0.85f, -0.64f, -0.85f, 0f)
                lineToRelative(-0f, 1.28f)
                lineToRelative(0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(299.31f, 179.02f)
                curveToRelative(-0f, -0.64f, -0.85f, -0.64f, -0.85f, 0f)
                lineToRelative(-0f, 1.28f)
                lineToRelative(0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                strokeLineWidth = 0.53f
            ) {
                moveTo(308.91f, 240.78f)
                curveToRelative(-0.11f, -5.87f, -4.27f, -7.25f, -4.27f, -7.25f)
                curveToRelative(-0.11f, 0f, -4.59f, 1.49f, -4.59f, 7.36f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveToRelative(312.53f, 233.63f)
                lineToRelative(0.75f, 6.72f)
                lineToRelative(-4.48f, 0f)
                curveToRelative(-0f, -5.55f, -4.27f, -6.4f, -4.27f, -6.4f)
                curveToRelative(-0.11f, 0f, -4.27f, 1.17f, -4.37f, 6.4f)
                lineToRelative(-4.48f, 0f)
                lineToRelative(0.85f, -6.83f)
                close()
                moveTo(313.6f, 240.46f)
                lineToRelative(-18.13f, 0f)
                quadToRelative(-0.53f, 0f, -0.64f, 0.75f)
                quadToRelative(-0f, 0.85f, 0.64f, 0.85f)
                lineToRelative(18.13f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, -0.85f)
                quadToRelative(-0f, -0.75f, -0.75f, -0.75f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(306.99f, 240.35f)
                curveToRelative(-0f, -3.52f, -2.45f, -4.48f, -2.45f, -4.48f)
                reflectiveCurveToRelative(-2.45f, 1.07f, -2.45f, 4.48f)
                lineTo(306.99f, 240.35f)
                moveToRelative(6.19f, -9.6f)
                lineToRelative(-17.39f, 0f)
                quadToRelative(-0.53f, 0.11f, -0.64f, 0.85f)
                quadToRelative(-0f, 0.53f, 0.64f, 0.64f)
                lineToRelative(17.39f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, -0.75f)
                quadToRelative(-0f, -0.53f, -0.64f, -0.64f)
                close()
                moveTo(312.75f, 232.35f)
                lineTo(296.32f, 232.35f)
                quadToRelative(-0.53f, 0f, -0.64f, 0.75f)
                reflectiveQuadToRelative(0.64f, 0.75f)
                lineToRelative(16.53f, 0f)
                quadToRelative(0.64f, 0f, 0.64f, -0.75f)
                reflectiveQuadToRelative(-0.64f, -0.75f)
                close()
                moveTo(307.41f, 221.05f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 0.85f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -0.85f)
                lineToRelative(-1.39f, 0f)
                lineToRelative(-0f, 0.96f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 2.13f)
                quadToRelative(-0f, 0.64f, 0.53f, 0.64f)
                lineToRelative(4.69f, 0f)
                quadToRelative(0.53f, 0f, 0.64f, -0.53f)
                close()
                moveTo(302.51f, 223.93f)
                lineTo(302.19f, 230.75f)
                lineToRelative(4.59f, 0f)
                lineToRelative(-0.32f, -6.93f)
                lineToRelative(-3.95f, 0f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(308.05f, 227.13f)
                lineToRelative(-0f, 3.63f)
                lineToRelative(4.27f, 0f)
                lineToRelative(-0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(296.75f, 227.13f)
                lineToRelative(-0f, 3.63f)
                lineToRelative(4.27f, 0f)
                lineToRelative(-0f, -3.63f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(313.07f, 224.35f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 1.07f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 1.07f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 2.13f)
                quadToRelative(-0f, 0.64f, 0.53f, 0.64f)
                lineToRelative(4.59f, 0f)
                lineToRelative(0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(301.76f, 224.35f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 1.07f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 1.07f)
                lineToRelative(-0.96f, 0f)
                lineToRelative(-0f, -1.07f)
                lineToRelative(-1.28f, 0f)
                lineToRelative(-0f, 2.13f)
                quadToRelative(-0f, 0.64f, 0.53f, 0.64f)
                lineToRelative(4.59f, 0f)
                lineToRelative(0.64f, -0.64f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(304.96f, 226.06f)
                curveToRelative(-0f, -0.64f, -0.96f, -0.64f, -0.96f, 0f)
                lineToRelative(-0f, 1.71f)
                lineToRelative(0.96f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(310.61f, 228.41f)
                curveToRelative(-0f, -0.64f, -0.85f, -0.64f, -0.85f, 0f)
                lineToRelative(-0f, 1.28f)
                lineToRelative(0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(299.31f, 228.41f)
                curveToRelative(-0f, -0.64f, -0.85f, -0.64f, -0.85f, 0f)
                lineToRelative(-0f, 1.28f)
                lineToRelative(0.85f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                strokeLineWidth = 0.53f
            ) {
                moveTo(289.66f, 292.25f)
                curveToRelative(4.09f, -4.21f, 2.14f, -8.14f, 2.14f, -8.14f)
                curveToRelative(-0.08f, -0.08f, -4.29f, -2.2f, -8.45f, 1.93f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveToRelative(297.28f, 289.79f)
                lineToRelative(-4.24f, 5.26f)
                lineToRelative(-3.16f, -3.18f)
                curveToRelative(3.94f, -3.91f, 1.53f, -7.54f, 1.53f, -7.54f)
                curveToRelative(-0.08f, -0.08f, -3.84f, -2.2f, -7.62f, 1.41f)
                lineToRelative(-3.16f, -3.18f)
                lineToRelative(5.45f, -4.2f)
                close()
                moveTo(293.19f, 295.36f)
                lineToRelative(-12.78f, -12.87f)
                quadToRelative(-0.38f, -0.38f, -0.98f, 0.07f)
                quadToRelative(-0.61f, 0.6f, -0.15f, 1.06f)
                lineToRelative(12.78f, 12.87f)
                quadToRelative(0.38f, 0.38f, 1.06f, -0.15f)
                quadToRelative(0.53f, -0.53f, 0f, -1.06f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(288.61f, 290.59f)
                curveToRelative(2.5f, -2.48f, 1.45f, -4.9f, 1.45f, -4.9f)
                reflectiveCurveToRelative(-2.49f, -0.99f, -4.91f, 1.42f)
                lineTo(288.61f, 290.59f)
                moveToRelative(11.17f, -2.37f)
                lineToRelative(-12.25f, -12.34f)
                quadToRelative(-0.45f, -0.3f, -1.06f, 0.15f)
                quadToRelative(-0.38f, 0.38f, -0f, 0.91f)
                lineToRelative(12.25f, 12.34f)
                quadToRelative(0.38f, 0.38f, 0.98f, -0.07f)
                quadToRelative(0.38f, -0.38f, 0f, -0.91f)
                close()
                moveTo(298.34f, 289.04f)
                lineTo(286.77f, 277.38f)
                quadToRelative(-0.38f, -0.38f, -0.98f, 0.07f)
                reflectiveQuadToRelative(-0.08f, 0.98f)
                lineToRelative(11.65f, 11.73f)
                quadToRelative(0.45f, 0.45f, 0.98f, -0.07f)
                reflectiveQuadToRelative(0.08f, -0.98f)
                close()
                moveTo(302.61f, 277.29f)
                lineToRelative(-0.9f, -0.91f)
                lineToRelative(-0.61f, 0.6f)
                lineToRelative(-0.68f, -0.68f)
                lineToRelative(0.61f, -0.6f)
                lineToRelative(-0.98f, -0.98f)
                lineToRelative(-0.68f, 0.68f)
                lineToRelative(-0.68f, -0.68f)
                lineToRelative(0.76f, -0.75f)
                lineToRelative(-0.9f, -0.91f)
                lineToRelative(-1.51f, 1.5f)
                quadToRelative(-0.45f, 0.45f, -0.08f, 0.83f)
                lineToRelative(3.31f, 3.33f)
                quadToRelative(0.38f, 0.38f, 0.83f, 0.08f)
                close()
                moveTo(297.11f, 275.84f)
                lineTo(292.04f, 280.42f)
                lineToRelative(3.23f, 3.25f)
                lineToRelative(4.69f, -5.11f)
                lineToRelative(-2.78f, -2.8f)
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(298.74f, 282.03f)
                lineToRelative(-2.57f, 2.56f)
                lineToRelative(3.01f, 3.03f)
                lineToRelative(2.57f, -2.56f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(290.78f, 274f)
                lineToRelative(-2.57f, 2.56f)
                lineToRelative(3.01f, 3.03f)
                lineToRelative(2.57f, -2.56f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(304.24f, 283.63f)
                lineToRelative(-0.9f, -0.91f)
                lineToRelative(-0.76f, 0.75f)
                lineToRelative(-0.68f, -0.68f)
                lineToRelative(0.76f, -0.75f)
                lineToRelative(-0.9f, -0.91f)
                lineToRelative(-0.76f, 0.75f)
                lineToRelative(-0.68f, -0.68f)
                lineToRelative(0.76f, -0.75f)
                lineToRelative(-0.9f, -0.91f)
                lineToRelative(-1.51f, 1.5f)
                quadToRelative(-0.45f, 0.45f, -0.08f, 0.83f)
                lineToRelative(3.23f, 3.25f)
                lineToRelative(0.91f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color.Yellow),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 0.53f
            ) {
                moveTo(296.28f, 275.61f)
                lineToRelative(-0.9f, -0.91f)
                lineToRelative(-0.76f, 0.75f)
                lineToRelative(-0.68f, -0.68f)
                lineToRelative(0.76f, -0.75f)
                lineToRelative(-0.9f, -0.91f)
                lineToRelative(-0.76f, 0.75f)
                lineToRelative(-0.68f, -0.68f)
                lineToRelative(0.76f, -0.75f)
                lineToRelative(-0.9f, -0.91f)
                lineToRelative(-1.51f, 1.5f)
                quadToRelative(-0.45f, 0.45f, -0.08f, 0.83f)
                lineToRelative(3.23f, 3.25f)
                lineToRelative(0.91f, 0f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(297.32f, 279.08f)
                curveToRelative(0.45f, -0.45f, -0.22f, -1.13f, -0.68f, -0.68f)
                lineToRelative(-1.21f, 1.2f)
                lineToRelative(0.68f, 0.68f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(299.64f, 284.74f)
                curveToRelative(0.45f, -0.45f, -0.15f, -1.06f, -0.6f, -0.61f)
                lineToRelative(-0.91f, 0.9f)
                lineToRelative(0.6f, 0.61f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF000001)),
                strokeLineWidth = 0.53f
            ) {
                moveTo(291.67f, 276.72f)
                curveToRelative(0.45f, -0.45f, -0.15f, -1.06f, -0.6f, -0.61f)
                lineToRelative(-0.91f, 0.9f)
                lineToRelative(0.6f, 0.61f)
                close()
            }
            path(fill = SolidColor(Color(0xFF003399))) {
                moveTo(248.11f, 242.59f)
                arcToRelative(8.53f, 8.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, 6.08f)
                arcToRelative(7.47f, 7.47f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.65f, 2.56f)
                quadToRelative(3.41f, -0.21f, 5.65f, -2.56f)
                arcToRelative(8.53f, 8.53f, 73.71f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, -6.08f)
                lineToRelative(0f, -11.52f)
                lineToRelative(-16f, 0f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(251.84f, 235.45f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(260.69f, 235.45f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(256.21f, 239.71f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(251.84f, 244.19f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(260.69f, 244.19f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color(0xFF003399))) {
                moveTo(248.11f, 214.86f)
                arcToRelative(8.53f, 8.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, 6.08f)
                arcToRelative(7.47f, 7.47f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.65f, 2.56f)
                quadToRelative(3.41f, -0.21f, 5.65f, -2.56f)
                arcToRelative(8.53f, 8.53f, 73.71f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, -6.08f)
                lineToRelative(0f, -11.52f)
                lineToRelative(-16f, 0f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(251.84f, 207.71f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(260.69f, 207.71f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(256.21f, 211.98f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(251.84f, 216.46f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(260.69f, 216.46f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color(0xFF003399))) {
                moveTo(225.92f, 242.59f)
                arcToRelative(8.53f, 8.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, 6.08f)
                arcToRelative(7.47f, 7.47f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.65f, 2.56f)
                quadToRelative(3.41f, -0.21f, 5.65f, -2.56f)
                arcToRelative(8.53f, 8.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, -6.08f)
                lineToRelative(0f, -11.52f)
                lineToRelative(-16f, 0f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(229.65f, 235.45f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(238.51f, 235.45f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(234.03f, 239.71f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(229.65f, 244.19f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(238.51f, 244.19f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color(0xFF003399))) {
                moveTo(270.29f, 242.59f)
                arcToRelative(8.53f, 8.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, 6.08f)
                arcToRelative(7.47f, 7.47f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.65f, 2.56f)
                quadToRelative(3.41f, -0.21f, 5.65f, -2.56f)
                arcToRelative(8.53f, 8.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, -6.08f)
                lineToRelative(0f, -11.52f)
                lineToRelative(-16f, 0f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(274.03f, 235.45f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(282.88f, 235.45f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(278.4f, 239.71f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(274.03f, 244.19f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(282.88f, 244.19f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color(0xFF003399))) {
                moveTo(248.11f, 270.11f)
                arcToRelative(8.53f, 8.53f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, 6.08f)
                arcToRelative(7.47f, 7.47f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.65f, 2.56f)
                quadToRelative(3.41f, -0.21f, 5.65f, -2.56f)
                arcToRelative(8.53f, 8.53f, 73.71f, isMoreThanHalf = false, isPositiveArc = false, 2.35f, -6.08f)
                lineToRelative(0f, -11.52f)
                lineToRelative(-16f, 0f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(251.84f, 262.97f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(260.69f, 262.97f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(256.21f, 267.23f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(251.84f, 271.71f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(260.69f, 271.71f)
                moveToRelative(-1.6f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 3.2f, 0f)
                arcToRelative(1.6f, 1.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, -3.2f, 0f)
            }
        }.build()

        return _FlagPt!!
    }

@Suppress("ObjectPropertyName")
private var _FlagPt: ImageVector? = null

