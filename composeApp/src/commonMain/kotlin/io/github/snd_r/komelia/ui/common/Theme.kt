package io.github.snd_r.komelia.ui.common

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class AppTheme(val colorScheme: ColorScheme) {
    DARK(
        darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            primaryContainer = Color(red = 212, green = 212, blue = 212),
            onPrimaryContainer = Color(red = 62, green = 64, blue = 64),

            secondary = Color(red = 87, green = 131, blue = 212),
            onSecondary = Color.White,
            secondaryContainer = Color(red = 123, green = 147, blue = 190),
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
            surfaceContainerHighest = Color(red = 43, green = 43, blue = 43),
            onSurfaceVariant = Color(red = 202, green = 196, blue = 208),

            surfaceDim = Color(red = 32, green = 31, blue = 35),
            surfaceBright = Color(red = 113, green = 116, blue = 118),

            error = Color(red = 240, green = 70, blue = 60),
            onError = Color.White,
            errorContainer = Color(red = 140, green = 29, blue = 24),
            onErrorContainer = Color.White
        )
    ),
    LIGHT(
        lightColorScheme(
            primary = Color.Black,
            onPrimary = Color.White,
            primaryContainer = Color(red = 212, green = 212, blue = 212),
            onPrimaryContainer = Color(red = 62, green = 64, blue = 64),

            secondary = Color(red = 87, green = 131, blue = 212),
            onSecondary = Color.White,
            secondaryContainer = Color(red = 163, green = 177, blue = 217),
            onSecondaryContainer = Color.White,

            tertiary = Color(red = 249, green = 168, blue = 37),
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
        )
    );
}
