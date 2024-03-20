package io.github.snd_r.komelia.ui.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CustomTheme(
    windowHeight: Dp,
    windowWidth: Dp,
    content: @Composable () -> Unit,
) {
    val windowSize = WindowSize.basedOnWidth(windowWidth)

    val orientation = when {
        windowHeight.value > windowWidth.value -> Orientation.PORTRAIT
        else -> Orientation.LANDSCAPE
    }

    CompositionLocalProvider(
        LocalCustomColors provides AppTheme.CustomColors(),
        LocalWindowHeight provides windowHeight,
        LocalWindowWidth provides windowWidth,
        LocalWindowSize provides windowSize,
        LocalOrientation provides orientation
    ) {
        MaterialTheme(colorScheme = AppTheme.colors.material) {
            content()
        }
    }
}

val LocalCustomColors = staticCompositionLocalOf { AppTheme.CustomColors() }
val LocalWindowHeight = compositionLocalOf { 0.dp }
val LocalWindowWidth = compositionLocalOf { 0.dp }
val LocalWindowSize = compositionLocalOf { WindowSize.COMPACT }
val LocalOrientation = compositionLocalOf { Orientation.LANDSCAPE }

object AppTheme {
    val colors: CustomColors
        @ReadOnlyComposable
        @Composable
        get() = LocalCustomColors.current

    class CustomColors(
        private val primaryBackground: Color = Color(red = 15, green = 15, blue = 15),
        val material: ColorScheme = darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            primaryContainer = Color(red = 212, green = 212, blue = 212),
            onPrimaryContainer = Color(red = 62, green = 64, blue = 64),

//            secondary = Color(red = 120, green = 186, blue = 236),
//            onSecondary = Color(red = 0, green = 52, blue = 79),
//            secondaryContainer = Color(red = 113, green = 179, blue = 228),
//            onSecondaryContainer = Color(red = 0, green = 36, blue = 57),

            secondary = Color(red = 87, green = 131, blue = 212),
            onSecondary = Color.White,
            secondaryContainer = Color(red = 163, green = 177, blue = 217),
            onSecondaryContainer = Color(red = 0, green = 27, blue = 63),

            tertiary = Color(red = 249, green = 168, blue = 37),
            onTertiary = Color(red = 70, green = 43, blue = 0),
            tertiaryContainer = Color(red = 181, green = 130, blue = 49),
            onTertiaryContainer = Color(red = 58, green = 34, blue = 0),


            background = primaryBackground,
            onBackground = Color(red = 237, green = 235, blue = 235),

            surface = primaryBackground,
            onSurface = Color(red = 237, green = 235, blue = 235),

            surfaceVariant = Color(red = 43, green = 43, blue = 43),
            onSurfaceVariant = Color(red = 202, green = 196, blue = 208),

            error = Color(red = 240, green = 70, blue = 60),
            errorContainer = Color(red = 140, green = 29, blue = 24)
        ),

        //Unused
        private val backgroundMedium: Color = Color(red = 60, green = 63, blue = 65),
        val backgroundLight: Color = Color(0xFF4E5254),
        val backgroundLighter: Color = Color(0xFF717476),
        val primaryColor: Color = Color(red = 43, green = 43, blue = 43),
        val highlight: Color = Color(red = 249, green = 168, blue = 37),
    )
}

enum class Orientation {
    LANDSCAPE,
    PORTRAIT;
}

enum class WindowSize {
    COMPACT,
    MEDIUM,
    EXPANDED,
    FULL;

    companion object {
        fun basedOnWidth(windowWidth: Dp): WindowSize {
            return when {
                windowWidth < 600.dp -> COMPACT
                windowWidth < 840.dp -> MEDIUM
                windowWidth < 1600.dp -> EXPANDED
                else -> FULL
            }
        }
    }
}
