package com.uniplan.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = TiimoPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0DB),
    onPrimaryContainer = Color(0xFF3E0A04),
    secondary = TiimoSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF003731),
    tertiary = TiimoTertiary,
    background = TiimoBackgroundLight,
    onBackground = Color(0xFF1F1B1A),
    surface = TiimoSurfaceLight,
    onSurface = Color(0xFF1F1B1A),
    surfaceVariant = Color(0xFFF3EFEA),
    onSurfaceVariant = Color(0xFF524442)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF8A80),
    onPrimary = Color(0xFF670003),
    primaryContainer = Color(0xFF930008),
    onPrimaryContainer = Color(0xFFFFDAD5),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = Color(0xFFE1BEE7),
    background = TiimoBackgroundDark,
    onBackground = Color(0xFFEDE0DE),
    surface = TiimoSurfaceDark,
    onSurface = Color(0xFFEDE0DE),
    surfaceVariant = TiimoSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFD8C2BF)
)

// Tiimo Rounded Shapes
val TiimoShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun UniPlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = TiimoShapes,
        typography = Typography,
        content = content
    )
}
