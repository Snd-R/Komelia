package io.github.snd_r.komelia.ui.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val colorScheme: ColorScheme,
    val type: ThemeType,
) {
    DARK(
        darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            primaryContainer = Color(red = 212, green = 212, blue = 212),
            onPrimaryContainer = Color(red = 62, green = 64, blue = 64),

            secondary = Color(red = 87, green = 131, blue = 212),
            onSecondary = Color.White,
            secondaryContainer = Color(red = 50, green = 70, blue = 120),
            onSecondaryContainer = Color(red = 230, green = 230, blue = 230),

            tertiary = Color(red = 249, green = 168, blue = 37),
            onTertiary = Color.White,
            tertiaryContainer = Color(red = 181, green = 130, blue = 49),
            onTertiaryContainer = Color.White,

            background = Color(red = 113, green = 116, blue = 118),
            onBackground = Color(red = 202, green = 196, blue = 208),

            surface = Color(red = 15, green = 15, blue = 15),
            onSurface = Color(red = 237, green = 235, blue = 235),

            surfaceVariant = Color(red = 43, green = 43, blue = 43),
            surfaceContainerLow = Color(red = 43, green = 43, blue = 43),
            surfaceContainerHighest = Color(red = 43, green = 43, blue = 43),
            onSurfaceVariant = Color(red = 202, green = 196, blue = 208),

            surfaceDim = Color(red = 32, green = 31, blue = 35),
            surfaceBright = Color(red = 113, green = 116, blue = 118),

            error = Color(red = 240, green = 70, blue = 60),
            onError = Color.White,
            errorContainer = Color(red = 140, green = 29, blue = 24),
            onErrorContainer = Color.White
        ),
        ThemeType.DARK
    ),
    LIGHT(
        lightColorScheme(
            primary = Color.Black,
            onPrimary = Color.White,
            primaryContainer = Color(red = 212, green = 212, blue = 212),
            onPrimaryContainer = Color(red = 62, green = 64, blue = 64),

            secondary = Color(red = 87, green = 131, blue = 212),
            onSecondary = Color.White,
            secondaryContainer = Color(red = 70, green = 100, blue = 160),
            onSecondaryContainer = Color.White,

            tertiary = Color(red = 232, green = 156, blue = 35),
            onTertiary = Color.White,
            tertiaryContainer = Color(red = 181, green = 130, blue = 49),
            onTertiaryContainer = Color.White,

            background = Color(red = 254, green = 247, blue = 255),
            onBackground = Color(red = 29, green = 27, blue = 32),

            surface = Color(red = 254, green = 247, blue = 255),
            onSurface = Color(red = 29, green = 27, blue = 32),

            surfaceVariant = Color(red = 231, green = 224, blue = 236),
            surfaceContainerHighest = Color(red = 230, green = 224, blue = 233),
            onSurfaceVariant = Color(red = 73, green = 69, blue = 79),

            surfaceDim = Color(red = 222, green = 216, blue = 225),
            surfaceBright = Color(red = 180, green = 180, blue = 180),

            error = Color(red = 240, green = 70, blue = 60),
            onError = Color.White,
            errorContainer = Color(red = 195, green = 65, blue = 60),
            onErrorContainer = Color.White
        ),
        ThemeType.LIGHT
    ),

    DARKER(
        darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            primaryContainer = Color.Black,
            onPrimaryContainer = Color.White,

            secondary = Color(red = 75, green = 125, blue = 205),
            onSecondary = Color.White,
            secondaryContainer = Color(red = 50, green = 70, blue = 120),
            onSecondaryContainer = Color(red = 230, green = 230, blue = 230),

            tertiary = Color(red = 193, green = 127, blue = 31),
            onTertiary = Color.White,
            tertiaryContainer = Color(red = 115, green = 84, blue = 10),
            onTertiaryContainer = Color.White,

            background = Color.Black,
            onBackground = Color.White,

            surface = Color.Black,
            onSurface = Color.White,

            surfaceVariant = Color(red = 30, green = 30, blue = 30),
            surfaceContainerHighest = Color(red = 30, green = 30, blue = 30),
            onSurfaceVariant = Color.White,

            surfaceDim = Color(red = 25, green = 25, blue = 25),
            surfaceBright = Color(red = 65, green = 65, blue = 65),

            error = Color(red = 240, green = 70, blue = 60),
            onError = Color.White,
            errorContainer = Color(red = 140, green = 29, blue = 24),
            onErrorContainer = Color.White
        ),
        ThemeType.DARK
    );

    enum class ThemeType {
        LIGHT,
        DARK
    }
}
