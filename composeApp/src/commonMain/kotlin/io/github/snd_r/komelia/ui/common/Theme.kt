package io.github.snd_r.komelia.ui.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

object AppTheme {
    val dark: ColorScheme = darkColorScheme(
        primary = Color.White,
        onPrimary = Color.Black,
        primaryContainer = Color(red = 212, green = 212, blue = 212),
        onPrimaryContainer = Color(red = 62, green = 64, blue = 64),

        secondary = Color(red = 87, green = 131, blue = 212),
        onSecondary = Color.White,
        secondaryContainer = Color(red = 163, green = 177, blue = 217),
        onSecondaryContainer = Color(red = 0, green = 27, blue = 63),

        tertiary = Color(red = 249, green = 168, blue = 37),
        onTertiary = Color(red = 70, green = 43, blue = 0),
        tertiaryContainer = Color(red = 181, green = 130, blue = 49),
        onTertiaryContainer = Color(red = 58, green = 34, blue = 0),

//        background = Color(red = 15, green = 15, blue = 15),
//        onBackground = Color(red = 237, green = 235, blue = 235),
        background = Color(red = 113, green = 116, blue = 118),
        onBackground = Color(red = 202, green = 196, blue = 208),
//        onBackground = backgroundLighter,

        surface = Color(red = 15, green = 15, blue = 15),
        onSurface = Color(red = 237, green = 235, blue = 235),

        surfaceVariant = Color(red = 43, green = 43, blue = 43),
        onSurfaceVariant = Color(red = 202, green = 196, blue = 208),

        error = Color(red = 240, green = 70, blue = 60),
        errorContainer = Color(red = 140, green = 29, blue = 24)
    )
}
